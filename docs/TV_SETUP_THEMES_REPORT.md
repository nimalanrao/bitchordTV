# BitChord TV Setup, Themes & Built-in Keyboard Architecture Report

**Version:** 0.01 (Build 1)  
**TV Platform Lead & UX Architect:** Nyxcore  
**Target Hardware:** Android TV OS, Google TV, Amazon Fire TV

---

## 1. Executive Summary

BitChord TV features a remote-first **TV Onboarding & Customization System** that allows users on first run to configure:
1. A **Local Nickname** for personalized living room greetings using a built-in virtual D-pad keyboard.
2. A **Visual Theme** with live preview cards across three tailored palettes (**Dynamic Artwork**, **Midnight**, and **Pure Black OLED**).

Personalization choices persist atomically to local device preferences and can be reconfigured at any time from **Settings → Personalization**.

---

## 2. Architecture & Components

```
app/src/main/java/com/music/bitchord/ui/tv/
  ├── onboarding/
  │   └── TvSetupScreen.kt           # First-run onboarding flow (Welcome, Nickname, Theme, Ready)
  ├── keyboard/
  │   └── TvKeyboard.kt              # Built-in 5-way D-pad virtual keyboard (Letters, Symbols, Emoji)
  ├── personalization/
  │   └── PersonalizationModels.kt   # AppThemeOption, TvThemePalette, and Unicode NicknamePolicy
  └── dialogs/
      └── TvDialogs.kt               # TvThemeDialog & TvNicknameDialog for in-app Settings customization
```

---

## 3. Visual Themes

| Theme | Background | Accent | Target Environment |
|---|---|---|---|
| **Dynamic Artwork** *(Default)* | `#08080B` Deep Canvas | Dynamic / `#FA2D48` | Apple Music dark aesthetic with album-derived highlights. |
| **Midnight** | `#0A0E17` Charcoal Navy | `#7C5CFC` Sapphire Violet | Modern cool dark mode for home theaters. |
| **Pure Black (OLED)** | `#000000` True Pitch Black | `#FA2D48` Crimson | Maximum contrast and zero pixel emission for OLED displays. |

---

## 4. Built-in TV Keyboard & Nickname Policy

- **Multi-Page Layout**:
  - **Letters**: Full QWERTY with Shift & Caps Lock toggle state.
  - **Numbers & Symbols**: Digits `0-9` and punctuation (`!`, `@`, `#`, `$`, `%`, `&`, `*`, `+`, `=`, etc.).
  - **Emoji**: Curated popular music and expressive emoji (✨, 🎵, 🎧, ⚡, 🔥, 💫, 🌟, 💜, ❤️, 🚀, etc.).
- **Grapheme-Safe Editing**:
  - Implements ICU `java.text.BreakIterator.getCharacterInstance()` to ensure Backspace and cursor navigation never corrupt surrogate pairs, multi-codepoint emojis, or skin tone modifiers.
  - 1–24 grapheme character limit.
- **Hardware Keyboard Support**:
  - Automatically intercepts physical USB/Bluetooth keyboard inputs while keeping the on-screen keyboard synchronized.
- **Privacy First**:
  - Nicknames are strictly local to the device and never logged, transmitted over network, or synced to cloud servers.

---

## 5. First-Run Routing & Settings Integration

- **First-Run Trigger**: On clean install or when `tvSetupVersionCompleted == 0`, `TvApp` displays `TvSetupScreen` before the home feed.
- **Safe Defaults**: A "Use Defaults" action allows instant bypass to `Listener` nickname and `Dynamic Artwork` theme.
- **Live In-App Editing**:
  - **Settings → Personalization → TV Nickname**: Opens `TvNicknameDialog` with the built-in keyboard.
  - **Settings → Personalization → Visual Theme**: Opens `TvThemeDialog` with instant theme switching without activity recreation.
  - **Settings → Personalization → Run Setup Assistant Again**: Re-launches the onboarding wizard with existing selections pre-filled.
