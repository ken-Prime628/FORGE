package com.kennedy.forge.ui.theme

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush

val Purple80 = Color(0xFFD0BCFF)
val PurpleGrey80 = Color(0xFFCCC2DC)
val Pink80 = Color(0xFFEFB8C8)

val Purple40 = Color(0xFF6650a4)
val PurpleGrey40 = Color(0xFF625b71)
val Pink40 = Color(0xFF7D5260)






// 🌿 BACKGROUNDS
val BackgroundMain = Color(0xFFF5F2EC)
val BackgroundSecondary = Color(0xFFEDE7DD)
val CardBackground = Color(0xFFFFFFFF)

// 🌑 DARK (Hero / Highlight)
val DarkSurface = Color(0xFF121212)
val DarkCard = Color(0xFF1C1C1C)

// 🥇 GOLD (Primary Brand)
val GoldPrimary = Color(0xFFC89B3C)
val GoldAccent = Color(0xFFE6B85C)
val GoldDeep = Color(0xFFA67C2E)

// 🎨 SOFT ACCENTS (Friendly UI)
val SoftGreen = Color(0xFF7FBF9F)
val SoftBlue = Color(0xFF7DAED3)
val SoftPeach = Color(0xFFE8A87C)
val SoftOlive = Color(0xFFB5A27A)

// 📝 TEXT
val TextPrimary = Color(0xFF1A1A1A)
val TextSecondary = Color(0xFF6F6F6F)
val TextOnDark = Color(0xFFFFFFFF)
val TextGold = Color(0xFFC89B3C)

// 🚦 STATUS
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFA726)
val Error = Color(0xFFE53935)

// 🌈 GRADIENTS
val GoldGradient = Brush.linearGradient(
    colors = listOf(GoldAccent, GoldPrimary)
)

val DarkGradient = Brush.linearGradient(
    colors = listOf(DarkCard, DarkSurface)
)