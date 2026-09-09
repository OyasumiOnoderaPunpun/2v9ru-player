package dev.twov9ru.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

val TwoV9RUShapes = Shapes(
    // Floating pill dock, full-radius buttons
    extraLarge = RoundedCornerShape(percent = 50),
    // Album art, large cards
    large = RoundedCornerShape(24.dp),
    // Standard cards, sheets
    medium = RoundedCornerShape(16.dp),
    // Chips, tags
    small = RoundedCornerShape(10.dp),
    // Minimal rounding for grid items
    extraSmall = RoundedCornerShape(8.dp)
)
