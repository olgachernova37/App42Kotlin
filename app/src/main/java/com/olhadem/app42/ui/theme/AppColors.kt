package com.olhadem.app42.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

/**
 * Кольори екранів, які самі підлаштовуються під світлу/темну тему.
 *
 * Замість того, щоб писати Color(0xFF...) прямо в кожному екрані, ми
 * питаємо appColors() - і той сам вирішує, яку версію кольору повернути,
 * залежно від теми, обраної користувачем у налаштуваннях (⋮ → Тема).
 */
data class AppPalette(
    val screenBackground: Color,
    val cardBackground: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val textFaint: Color
)

private val LightPalette = AppPalette(
    screenBackground = Color(0xFFE8E8E8),
    cardBackground = Color.White,
    textPrimary = Color(0xFF1F1F1F),
    textSecondary = Color(0xFF5A5A5A),
    textTertiary = Color(0xFF8A8A8A),
    textFaint = Color(0xFFB0B0B0)
)

private val DarkPalette = AppPalette(
    screenBackground = Color(0xFF121212),
    cardBackground = Color(0xFF1E1E1E),
    textPrimary = Color(0xFFF2F2F2),
    textSecondary = Color(0xFFC2C2C2),
    textTertiary = Color(0xFF9A9A9A),
    textFaint = Color(0xFF7A7A7A)
)

/**
 * CompositionLocal - спосіб "прокинути" значення згори вниз через усе
 * дерево composable-функцій, не передаючи його явним параметром у кожну
 * функцію по черзі. Тут через LocalDarkTheme передається лише одне -
 * true/false, чи зараз активна темна тема. Значення виставляється один раз,
 * у MainActivity, на основі вибору користувача (і збереженого раніше в
 * ThemePreferences).
 */
val LocalDarkTheme = staticCompositionLocalOf { false }

@Composable
fun appColors(): AppPalette = if (LocalDarkTheme.current) DarkPalette else LightPalette
