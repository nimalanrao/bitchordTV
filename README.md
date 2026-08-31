<div align="center">

<br/>
<br/>

<img src="Logo.png" alt="BitChord app icon" width="180" />

# BitChord TV

### Universal YouTube Music Client for Android & Android TV / Google TV

<br/>

[![Latest release](https://img.shields.io/badge/Release-v1.5_TV_Edition-FA2D48?style=for-the-badge&labelColor=0d1117)](https://github.com/nimalanrao/bitchordTV/releases)
[![License: GPL-3.0](https://img.shields.io/badge/License-GPL--3.0-blue?style=for-the-badge&labelColor=0d1117)](LICENSE)
[![Platform](https://img.shields.io/badge/Platform-Android_%7C_Android_TV-4CAF50?style=for-the-badge&labelColor=0d1117)](#tv-compatibility)

<br/>

[**TV Experience**](#android-tv-features) · [**Mobile Features**](#mobile-features) · [**Compatibility**](#tv-compatibility) · [**Documentation**](#documentation) · [**License**](#license)

</div>

> [!WARNING]
> BitChord is an independent open-source project and is not affiliated with, endorsed by, or connected to YouTube, Google LLC, or Apple in any way.

---

<div align="center">

<img src="Banner.png" alt="BitChord banner" width="100%" />

</div>

## Overview

**BitChord** is a high-quality universal Android music streaming application inspired by Apple Music's design language. It brings lossless audio streaming, intelligent DJ transitions, synchronized lyrics, and modern dark luxury aesthetics to both **smartphones** and **Android TV / Google TV** screens from a single unified application.

---

## 📺 Android TV & Google TV Experience

BitChord features a dedicated **remote-first television interface** built from the ground up for living rooms and home audio setups:

- **100% 5-Way D-Pad Remote Navigation**: Intuitive spatial focus system designed for TV remotes, gamepads, and keyboards.
- **10-Foot 16:9 Landscape Layouts**: Optimized for 720p, 1080p, and 4K displays with safe-margin protection against overscan.
- **Collapsible Navigation Rail**: Fast, remote-friendly access to Home, Search, Library, and Settings.
- **16:9 Two-Pane Now Playing**:
  - Dominant album artwork with dynamic mesh backdrop.
  - D-pad Seekbar with ±10s stepping and acceleration.
  - Full transport controls, Repeat, Shuffle, and Favorite toggles.
  - Side-panel for **Synchronized Word/Line Lyrics** with auto-follow and manual browsing.
  - Side-panel for **Up Next Queue** management.
  - In-app **Stats for Nerds** (audio codec, sample rate, bit depth, stream source).
- **Television Search with System IME**: Search songs, albums, artists, and playlists with remote input and category chips.
- **Ambient Mode & Background Playback**: Music keeps playing uninterrupted during screensaver and background transitions.
- **Universal MediaSession Integration**: Home-screen media controls and voice assistant playback commands.

---

## 📱 Mobile Features

The mobile phone experience retains 100% of BitChord's signature polish:

- **Hi-Res Lossless Audio**: Stream FLAC/ALAC from pluggable module sources with YouTube Music fallback.
- **Gapless Playback & Crossfade**: Smooth 0–12s audio transitions between tracks.
- **Automix [Beta]**: On-device AI/DSP beat-matching and tempo transitions powered by ONNX Runtime.
- **Synchronized Lyrics**: Word- and syllable-level highlighting across multiple sources.
- **Offline Downloads & Local Library**: Save tracks with embedded tags; browse local device audio.
- **Frosted Glass Aesthetic**: Modern translucent bars and album-derived color palettes.
- **Rich Integrations**: Discord Rich Presence, Last.fm, and ListenBrainz scrobbling.

---

## 🧭 Navigation & Remote Controls

| Input | TV Action |
|---|---|
| **D-Pad Up / Down / Left / Right** | Move visual focus across cards, buttons, and shelves |
| **D-Pad Center / Enter** | Select focused item / Play track |
| **D-Pad Left / Right (on Player Seekbar)** | Step backward / forward by ±10 seconds |
| **Media Play / Pause / Next / Prev** | Global playback control regardless of active screen |
| **Back Button** | Close overlays/dialogs -> return to previous screen -> exit |

---

## 🌐 TV Compatibility

| Platform | Target Devices | Status |
|---|---|---|
| **Google TV** | Chromecast with Google TV, Sony Bravia, TCL, Hisense, Philips Google TVs | **Full Support** |
| **Android TV OS** | NVIDIA Shield TV, Xiaomi Mi Box, Mecool, generic Android TV boxes | **Full Support** |
| **Amazon Fire TV** | Fire TV Stick 4K / Max, Fire TV Cube, Fire OS 7+ televisions | **Compatible** |
| **Non-Android TVs** | Samsung Tizen, LG webOS, Roku OS, Apple tvOS | *Out of Scope* |

---

## 📚 Technical Documentation

Explore the complete architecture and release documentation in [`docs/`](docs/):

- 📄 [**TV Implementation Report**](docs/TV_IMPLEMENTATION_REPORT.md) – Comprehensive architecture, security analysis, and design system decisions.
- 📄 [**TV Compatibility Matrix**](docs/TV_COMPATIBILITY.md) – Device matrix, ABI details, and 16 KB page-size compliance.
- 📄 [**TV Quality & Test Matrix**](docs/TV_TEST_MATRIX.md) – Remote traversal verification, display scaling, and playback checks.
- 📄 [**TV Release Checklist**](docs/TV_RELEASE_CHECKLIST.md) – Store distribution and packaging guide.

---

## 🛠️ Architecture & Tech Stack

- **UI Framework**: Jetpack Compose (Material 3 for mobile, `androidx.tv.material3` for TV).
- **Media Engine**: Media3 / ExoPlayer 1.11.0 with single-player `MediaSessionService`.
- **DSP & ML**: Beat This! ONNX beat analyzer (`onnxruntime-android`) & C++ native analyzer.
- **Script VM**: QuickJS Kotlin runtime (`quickjs-kt-android`) for stream module plugins.
- **Networking**: Ktor 3.0 + OkHttp + kotlinx.serialization.
- **Image Pipeline**: Coil 3 with AndroidX Palette dynamic color extraction.

---

## ⚖️ License & Disclaimer

BitChord is free and open-source software licensed under the **GNU General Public License v3.0 (GPLv3)**. See the [LICENSE](LICENSE) file for complete details.

* **No Media Hosting:** BitChord does not host or store copyrighted audio. It functions as a client for public, user-authenticated, or local device media.
* **API Usage:** Created for research and educational purposes. Users are responsible for complying with relevant local terms and copyright laws.
