package dev.twov9ru.ui.theme

import androidx.compose.ui.graphics.Color

// ── Core Surfaces ──────────────────────────────────────────────────────────
val Void        = Color(0xFF080808) // deepest black background
val Obsidian    = Color(0xFF0F0F0F) // card surfaces
val Graphite    = Color(0xFF1A1A1A) // pill / dock bg
val Slate       = Color(0xFF2A2A2A) // dividers / inactive
val Smoke       = Color(0xFF3D3D3D) // secondary containers

// ── Text ───────────────────────────────────────────────────────────────────
val Snow        = Color(0xFFF5F5F5) // primary text
val Ash         = Color(0xFFAAAAAA) // secondary text
val Dust        = Color(0xFF666666) // tertiary / placeholders

// ── Brand Accent (neon chartreuse — brutalist, distinct) ───────────────────
val Nexus       = Color(0xFFCBFF47) // 2V9RU mark accent / active state
val NexusDim    = Color(0xFF8AAA30) // pressed / dimmed accent
val NexusGlow   = Color(0x402AF000) // ambient glow overlay

// ── Playback Accent (warm amber — audiophile warmth) ──────────────────────
val WaveAmber   = Color(0xFFFFBB33) // seek bar / progress
val WaveAmberDim = Color(0xFFAA7A22)

// ── Semantic ───────────────────────────────────────────────────────────────
val ErrorRed    = Color(0xFFFF4545)
val HeartPink   = Color(0xFFFF6B9D) // favorites

// ── Dynamic Theming Defaults (overridden by Palette API) ──────────────────
val DefaultVibrant  = Color(0xFF1A1A2E)
val DefaultMuted    = Color(0xFF16213E)
val DefaultDark     = Color(0xFF0F3460)
