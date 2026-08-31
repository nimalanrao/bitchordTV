# BitChord TV Release & Store Publication Checklist

**Release:** Version 0.01 (Build 1)  
**TV Platform Lead & Architect:** Nyxcore  
**License:** GNU General Public License v3.0 (GPLv3)

---

## 1. Android TV Core Quality Checklist

- [x] **Universal Manifest Declarations**:
  - `android.software.leanback` marked `android:required="false"`.
  - `android.hardware.touchscreen` marked `android:required="false"`.
  - Transitive hardware features (camera, telephony, microphone) marked `android:required="false"`.
- [x] **Dedicated TV Launcher Channel & Banner Assets**:
  - `android:banner="@drawable/tv_banner"` included on `<application>` and `<activity android:name=".TvActivity">`.
  - 16:9 banner PNGs (320 × 180 px xhdpi, 240 × 135 px hdpi, 160 × 90 px mdpi) placed across density qualifiers to guarantee display on Google TV & Android TV "Apps" home rows.
- [x] **Dedicated TV Activity**:
  - `TvActivity` registered with `MAIN` + `LEANBACK_LAUNCHER`.
  - `screenOrientation="landscape"` and `launchMode="singleTask"`.
- [x] **Remote-First Usability & Focus**:
  - 100% of app features operable with standard 5-way D-pad remote and Back key.
  - Clear visual focus indicator on every interactive control (1.05x scale + halo).
  - No touch-only gestures required.
- [x] **Cinematic Media Playback & Lyrics**:
  - Active `MediaSession` supporting standard transport controls (Play, Pause, Skip, Seek).
  - Media session activity correctly resolves to `TvActivity` on TV devices.
  - Millisecond-precise synchronized word-by-word and line-by-line lyrics engine with auto-follow.
  - Audio continues playback during ambient mode and screensaver transitions.

---

## 2. Native & Packaging Compliance

- [x] **Multi-Architecture Support**:
  - `arm64-v8a` (64-bit ARM)
  - `armeabi-v7a` (32-bit ARM for streaming sticks and budget TV hardware)
  - `x86_64` (Emulators)
- [x] **16 KB Memory Page-Size Alignment**:
  - NDK r28+ / modern toolchain compliance for Android 15+ 16 KB page-size kernel support.
- [x] **Android App Bundle (AAB)**:
  - Generates signed/distributable `.aab` for Google Play Console TV track.

---

## 3. Privacy, Licensing & Legal Compliance

- [x] **GNU General Public License v3.0 (GPLv3)**:
  - Full license file intact in root repository (`LICENSE`).
  - Source code available and matching release builds.
- [x] **Third-Party Disclaimers**:
  - Clear notice that BitChord is an independent third-party client not affiliated with Google or YouTube.
- [x] **Credential Security**:
  - No remote token logging or unencrypted credential caching.
