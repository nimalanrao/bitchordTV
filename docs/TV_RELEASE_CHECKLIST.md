# BitChord TV Release & Store Publication Checklist

## 1. Android TV Core Quality Checklist

- [x] **Universal Manifest Declarations**:
  - `android.software.leanback` marked `android:required="false"`.
  - `android.hardware.touchscreen` marked `android:required="false"`.
  - Transitive hardware features (camera, telephony, microphone) marked `android:required="false"`.
- [x] **TV Launcher & Banner Assets**:
  - `android:banner="@drawable/tv_banner"` included on `<application>` and `<activity android:name=".TvActivity">`.
  - 16:9 banner (320 × 180 px) with legible branding on light and dark TV home screens.
- [x] **Dedicated TV Activity**:
  - `TvActivity` registered with `MAIN` + `LEANBACK_LAUNCHER`.
  - `screenOrientation="landscape"` and `launchMode="singleTask"`.
- [x] **Remote-First Usability**:
  - 100% of app features operable with standard 5-way D-pad remote and Back key.
  - Clear visual focus indicator on every interactive control.
  - No touch-only gestures required (no swipe-to-dismiss, long-press only, or pinch-to-zoom).
- [x] **Media Playback & Session Integration**:
  - Active `MediaSession` supporting standard transport controls (Play, Pause, Skip, Seek).
  - Media session activity correctly resolves to `TvActivity` on TV devices.
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
