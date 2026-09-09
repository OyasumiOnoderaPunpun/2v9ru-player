# 2V9RU — Avant-Garde Android Music Player

> *Local-first. Lossless. Kinetic. Brutally beautiful.*

[![Android](https://img.shields.io/badge/Platform-Android%208.0%2B-green?logo=android)](https://developer.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-7F52FF?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-4285F4?logo=jetpack-compose)](https://developer.android.com/jetpack/compose)
[![Media3](https://img.shields.io/badge/Audio-Media3%2FExoPlayer-FF6D00)](https://developer.android.com/guide/topics/media/media3)

---

## What is 2V9RU?

**2V9RU** is a premium local music player for Android that completely breaks the mold of traditional player UIs. It fuses Zen Browser's floating minimalism with Pixel Play's tactile expressiveness — while remaining fundamentally "potato-friendly": 60fps on 2GB RAM devices.

---

## Core Features

### 🎛️ The 2V9RU Interactive Nexus
The brand mark lives in the floating dock as a **functional gesture hub**:
- **Tap** → Expands to the immersive "No-Peak" Now Playing view
- **Swipe Up** → Opens the Hardware Equalizer (DSP) & Crossfade settings
- **Long Press** → Haptic pulse + Global Search / Raw Folder Browser

### 🔊 Audiophile-Grade Audio Engine
- **Bit-perfect AAudio output** — bypasses OS audio resampling (SRC)
- **32-bit float processing** — prevents clipping on high-dynamic-range material (Radiohead, RADWIMPS)
- **Gapless & Crossfade** — configurable 1–5s cross-fade using 32-bit float volume ramping
- **Hardware-offloaded DSP** — 10-band EQ, Bass Boost, Virtualizer via `AudioEffects` API

### 🎨 Dynamic Theming
- **Palette API** extracts album art colors on a background thread
- Generates a smooth **mesh-gradient background** that crossfades between tracks
- No real-time blur — pure GPU-accelerated color transitions

### 📐 Universal Adaptive Geometry
| Device | Navigation | Grid |
|---|---|---|
| Phones (portrait) | Floating bottom pill | 2 cols |
| Large phones / landscape | Auto-adapt | 3 cols |
| Tablets / foldables | Floating left vertical rail | 4–5 cols |

### 🌊 Squiggly Seek Bar
A GPU-accelerated Canvas seek bar rendered as a flowing sine wave:
- Played portion: warm amber, full amplitude
- Unplayed portion: muted grey, softer wave
- Continuously animated at 60fps via Compose Canvas

---

## Architecture

```
app/
├── audio/
│   ├── AAudioSinkFactory.kt     # Bit-perfect ExoPlayer configuration
│   ├── CrossfadeProcessor.kt    # 32-bit float crossfade audio processor
│   └── PlaybackService.kt       # Media3 MediaSessionService
├── data/
│   ├── MediaStoreScanner.kt     # Background MediaStore indexer
│   ├── AppDatabase.kt           # Room DB + Smart Playlist DAOs
│   └── SmartPlaylistRepo.kt     # Recently Added / Most Played / Favorites
├── theme/
│   └── PaletteExtractor.kt      # Async Palette API → mesh gradient
└── ui/
    ├── components/
    │   ├── NexusDock.kt         # 2V9RU Interactive Nexus (gesture hub)
    │   └── SquigglySeekBar.kt   # GPU Canvas seek bar
    ├── navigation/
    │   └── NavigationLayout.kt  # Adaptive Phone ↔ Tablet routing
    ├── screens/
    │   ├── LibraryScreen.kt     # Adaptive grid library
    │   ├── NowPlayingScreen.kt  # No-Peak immersive view
    │   ├── DspSheet.kt          # Hardware EQ bottom sheet
    │   └── FolderBrowserScreen.kt # Raw folder browser
    ├── theme/
    │   ├── Color.kt             # Design system tokens
    │   ├── Type.kt              # Space Grotesk + Inter typography
    │   ├── Shape.kt             # Shape tokens
    │   └── Theme.kt             # MaterialTheme wrapper
    └── viewmodel/
        └── PlayerViewModel.kt   # Shared playback + DSP state
```

---

## Performance Targets
- **Memory**: <30MB while scrolling large libraries (Coil 20MB cap, 128px thumbnails)
- **Frame rate**: 60fps on 2GB RAM devices (GPU Canvas, no CPU-heavy blur)
- **Startup**: Cold start <1.5s (lazy Room init, background MediaStore scan)

---

## Build

```bash
# Requires JDK 17 + Android SDK 26+
./gradlew assembleDebug
```

---

## Tech Stack
- **Language**: Kotlin
- **UI**: Jetpack Compose + Material3
- **Audio**: Media3 / ExoPlayer (AAudio → OpenSL ES fallback)
- **Database**: Room + KSP
- **Image loading**: Coil (bitmap pooling, downsampling)
- **Theming**: Palette API + Compose `animateColorAsState`
- **Navigation**: Navigation3

---

## Roadmap
- [x] Phase 1: Foundation, Brand Shell & Adaptive Geometry
- [ ] Phase 2: Audio Engine, MediaStore Scanner, Room DB, Hardware EQ binding
- [ ] Phase 3: Palette → Mesh Gradient, 60fps Squiggly Seek Bar, LRC Lyrics
- [ ] Phase 4: Memory profiling, Sleep Timer, Notification polish
