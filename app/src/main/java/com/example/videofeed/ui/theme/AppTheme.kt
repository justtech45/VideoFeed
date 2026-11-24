package com.example.videofeed.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/* ── Palette ─────────────────────────────────────────────────────────── */

val Mint = Color(0xFF4FE1B4)
val MintBright = Color(0xFF7BF3D2)
val MintDeep = Color(0xFF17A57F)
val MintSoft = Color(0x264FE1B4)

val TrueBlack = Color(0xFF000000)
val DarkSurface = Color(0xFF0B0E0D)
val DarkCard = Color(0xFF111514)
val DarkElevated = Color(0xFF181D1C)
val DarkElevatedHigh = Color(0xFF212827)

val TextPrimary = Color(0xFFF7FAF9)
val TextSecondary = Color(0xFF9AA5A1)
val TextTertiary = Color(0xFF6C7773)
val TextOnMint = Color(0xFF00110B)

val MintBorder = Mint.copy(alpha = 0.2f)
val Hairline = Color(0x1AFFFFFF)

/* ── Gradients ───────────────────────────────────────────────────────── */

val MintGradient = Brush.linearGradient(listOf(MintBright, Mint))
val MintGlow = Brush.radialGradient(listOf(Mint.copy(alpha = 0.16f), Color.Transparent))
val CardScrim = Brush.verticalGradient(
    listOf(Color.Transparent, Color.Black.copy(alpha = 0.72f))
)

/* ── Color scheme ────────────────────────────────────────────────────── */

private val AppColors = darkColorScheme(
    primary = Mint,
    onPrimary = TextOnMint,
    primaryContainer = DarkElevated,
    onPrimaryContainer = Mint,
    secondary = TextSecondary,
    onSecondary = TrueBlack,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkElevated,
    onSurfaceVariant = TextSecondary,
    background = TrueBlack,
    onBackground = TextPrimary,
    outline = MintBorder,
    outlineVariant = DarkElevated,
    error = Color(0xFFFF5C5C),
    onError = TrueBlack
)

/* ── Type scale ──────────────────────────────────────────────────────── */

private val AppTypography = Typography(
    displaySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 34.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.6).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 28.sp,
        lineHeight = 34.sp,
        letterSpacing = (-0.4).sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        lineHeight = 30.sp,
        letterSpacing = (-0.3).sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Bold,
        fontSize = 20.sp,
        lineHeight = 27.sp,
        letterSpacing = (-0.2).sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 22.sp
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 21.sp
    ),
    bodySmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 17.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 18.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 16.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.SansSerif,
        fontWeight = FontWeight.Normal,
        fontSize = 11.sp,
        lineHeight = 14.sp
    )
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(16.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun VideoFeedTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppColors,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
