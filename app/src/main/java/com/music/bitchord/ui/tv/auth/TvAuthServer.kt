package com.music.bitchord.ui.tv.auth

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.net.URLDecoder
import java.util.concurrent.atomic.AtomicBoolean

object TvAuthServer {

    private const val TAG = "TvAuthServer"
    private const val DEFAULT_PORT = 8765

    private var serverSocket: ServerSocket? = null
    private val isRunning = AtomicBoolean(false)

    fun getLocalIpAddress(): String? {
        try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            while (interfaces.hasMoreElements()) {
                val iface = interfaces.nextElement()
                if (iface.isLoopback || !iface.isUp) continue
                val addresses = iface.inetAddresses
                while (addresses.hasMoreElements()) {
                    val addr = addresses.nextElement()
                    if (addr is Inet4Address && !addr.isLoopbackAddress) {
                        val host = addr.hostAddress ?: continue
                        if (host.startsWith("192.168.") || host.startsWith("10.") || host.startsWith("172.")) {
                            return host
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Failed to resolve local IP", e)
        }
        return null
    }

    suspend fun start(
        onCookieReceived: (String) -> Unit,
    ): Pair<Int, String>? = withContext(Dispatchers.IO) {
        stop()

        val ip = getLocalIpAddress() ?: return@withContext null
        try {
            val socket = try {
                ServerSocket(DEFAULT_PORT)
            } catch (_: Exception) {
                ServerSocket(0) // dynamic fallback port
            }
            serverSocket = socket
            isRunning.set(true)
            val port = socket.localPort

            Thread {
                while (isRunning.get() && !socket.isClosed) {
                    try {
                        val client = socket.accept()
                        handleClient(client, onCookieReceived)
                    } catch (e: Exception) {
                        if (!isRunning.get()) break
                    }
                }
            }.start()

            Pair(port, ip)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to start TV auth server", e)
            null
        }
    }

    fun stop() {
        isRunning.set(false)
        try {
            serverSocket?.close()
        } catch (_: Exception) {}
        serverSocket = null
    }

    private fun handleClient(client: Socket, onCookieReceived: (String) -> Unit) {
        Thread {
            try {
                val reader = BufferedReader(InputStreamReader(client.getInputStream()))
                val writer = PrintWriter(client.getOutputStream(), true)

                val requestLine = reader.readLine() ?: return@Thread
                val parts = requestLine.split(" ")
                val method = parts.getOrNull(0) ?: "GET"
                val path = parts.getOrNull(1) ?: "/"

                var contentLength = 0
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    if (line.isNullOrBlank()) break
                    if (line!!.startsWith("Content-Length:", ignoreCase = true)) {
                        contentLength = line!!.substringAfter(":").trim().toIntOrNull() ?: 0
                    }
                }

                if (method == "POST" && path == "/submit") {
                    val bodyChars = CharArray(contentLength)
                    var read = 0
                    while (read < contentLength) {
                        val count = reader.read(bodyChars, read, contentLength - read)
                        if (count <= 0) break
                        read += count
                    }
                    val body = String(bodyChars)
                    val cookieValue = parseCookieFromBody(body)

                    if (!cookieValue.isNullOrBlank()) {
                        onCookieReceived(cookieValue)
                        sendHtmlResponse(writer, SUCCESS_HTML)
                    } else {
                        sendHtmlResponse(writer, ERROR_HTML)
                    }
                } else {
                    sendHtmlResponse(writer, LOGIN_PAGE_HTML)
                }

                client.close()
            } catch (e: Exception) {
                Log.w(TAG, "Error handling client", e)
            }
        }.start()
    }

    private fun parseCookieFromBody(body: String): String? {
        val pairs = body.split("&")
        for (pair in pairs) {
            val kv = pair.split("=", limit = 2)
            if (kv.size == 2 && kv[0] == "cookie") {
                return URLDecoder.decode(kv[1], "UTF-8").trim()
            }
        }
        return if (body.isNotBlank() && !body.contains("=")) body.trim() else null
    }

    private fun sendHtmlResponse(writer: PrintWriter, html: String) {
        writer.print("HTTP/1.1 200 OK\r\n")
        writer.print("Content-Type: text/html; charset=UTF-8\r\n")
        writer.print("Content-Length: ${html.toByteArray(Charsets.UTF_8).size}\r\n")
        writer.print("Connection: close\r\n\r\n")
        writer.print(html)
        writer.flush()
    }

    private const val LOGIN_PAGE_HTML = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>BitChord TV • Sign In</title>
<style>
  body {
    background-color: #08080B;
    color: #F2F2F7;
    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', 'Segoe UI', Roboto, sans-serif;
    margin: 0;
    padding: 24px;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
    box-sizing: border-box;
  }
  .card {
    background: #14141B;
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 20px;
    padding: 28px;
    max-width: 480px;
    width: 100%;
    box-shadow: 0 16px 40px rgba(0,0,0,0.6);
  }
  .header {
    display: flex;
    align-items: center;
    margin-bottom: 16px;
  }
  .logo-badge {
    background: #FA2D48;
    color: white;
    font-weight: 800;
    font-size: 14px;
    padding: 6px 12px;
    border-radius: 8px;
    margin-right: 12px;
  }
  h1 {
    font-size: 22px;
    margin: 0;
    font-weight: 700;
  }
  p {
    color: #8E8E93;
    font-size: 14px;
    line-height: 1.5;
    margin-top: 6px;
    margin-bottom: 20px;
  }
  textarea {
    width: 100%;
    height: 140px;
    background: #08080B;
    border: 1px solid rgba(255, 255, 255, 0.15);
    border-radius: 12px;
    padding: 14px;
    color: #FFFFFF;
    font-family: monospace;
    font-size: 13px;
    resize: none;
    box-sizing: border-box;
    outline: none;
    transition: border-color 0.2s;
  }
  textarea:focus {
    border-color: #FA2D48;
  }
  button {
    width: 100%;
    background: #FA2D48;
    color: white;
    border: none;
    border-radius: 12px;
    padding: 16px;
    font-size: 16px;
    font-weight: 700;
    cursor: pointer;
    margin-top: 16px;
    transition: transform 0.1s, background-color 0.2s;
  }
  button:hover {
    background: #E0203B;
  }
  button:active {
    transform: scale(0.98);
  }
  .info-box {
    margin-top: 20px;
    padding: 12px;
    border-radius: 10px;
    background: rgba(250, 45, 72, 0.1);
    border-left: 4px solid #FA2D48;
    font-size: 12px;
    color: #C7C7CC;
  }
</style>
</head>
<body>
<div class="card">
  <div class="header">
    <span class="logo-badge">TV CONNECT</span>
    <h1>Sign In to BitChord TV</h1>
  </div>
  <p>Paste your YouTube Music session cookie or login token below to authenticate your TV instance instantly without on-screen typing.</p>
  <form action="/submit" method="POST">
    <textarea name="cookie" placeholder="Paste SAPISID / HSID / SID cookie string here..." required></textarea>
    <button type="submit">Authorize TV</button>
  </form>
  <div class="info-box">
    🔒 <strong>Direct Local Connection:</strong> Your credentials are sent directly over your local Wi-Fi to your TV only and never touch any third-party cloud servers.
  </div>
</div>
</body>
</html>"""

    private const val SUCCESS_HTML = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Connected • BitChord TV</title>
<style>
  body {
    background-color: #08080B;
    color: #F2F2F7;
    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif;
    margin: 0;
    padding: 24px;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
  }
  .card {
    background: #14141B;
    border: 1px solid rgba(255, 255, 255, 0.1);
    border-radius: 20px;
    padding: 36px 28px;
    max-width: 420px;
    width: 100%;
    text-align: center;
    box-shadow: 0 16px 40px rgba(0,0,0,0.6);
  }
  .icon {
    font-size: 48px;
    margin-bottom: 16px;
  }
  h1 {
    font-size: 24px;
    color: #30D158;
    margin: 0 0 10px 0;
  }
  p {
    color: #8E8E93;
    font-size: 15px;
    line-height: 1.5;
    margin: 0;
  }
</style>
</head>
<body>
<div class="card">
  <div class="icon">✨</div>
  <h1>TV Authenticated!</h1>
  <p>Your account was successfully linked to your TV. You can close this browser tab and enjoy your music on BitChord TV!</p>
</div>
</body>
</html>"""

    private const val ERROR_HTML = """<!DOCTYPE html>
<html lang="en">
<head>
<meta charset="UTF-8">
<meta name="viewport" content="width=device-width, initial-scale=1.0">
<title>Error • BitChord TV</title>
<style>
  body {
    background-color: #08080B;
    color: #F2F2F7;
    font-family: -apple-system, BlinkMacSystemFont, 'SF Pro Display', sans-serif;
    display: flex;
    justify-content: center;
    align-items: center;
    min-height: 100vh;
  }
  .card {
    background: #14141B;
    border-radius: 20px;
    padding: 30px;
    max-width: 400px;
    text-align: center;
  }
  h1 { color: #FF453A; margin: 0 0 10px 0; }
  p { color: #8E8E93; font-size: 14px; }
  a { color: #FA2D48; text-decoration: none; font-weight: bold; }
</style>
</head>
<body>
<div class="card">
  <h1>Empty Cookie</h1>
  <p>No cookie data was provided. Please go back and paste a valid cookie string.</p>
  <p><a href="/">← Try Again</a></p>
</div>
</body>
</html>"""
}
