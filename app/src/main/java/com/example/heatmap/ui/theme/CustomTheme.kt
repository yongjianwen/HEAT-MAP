package com.example.heatmap.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class CustomColors(
    val content: Color,
    val component: Color,
    val background: Color
)

val LocalBrandColorsLight = staticCompositionLocalOf {
    CustomColors(
        content = Color.Black,
        component = Color.Black,
        background = Color(0xFFFFCA28)
    )
}

val LocalBrandColorsDark = staticCompositionLocalOf {
    CustomColors(
        content = Color.White,
        component = Color.White,
        background = Color(0x11FFCA28)
    )
}

val LocalBackgroundColorsLight = staticCompositionLocalOf {
    CustomColors(
        content = Color.Black,
        component = Color.Black,
        background = Color(0xFFEEEEEE)
    )
}

val LocalBackgroundColorsDark = staticCompositionLocalOf {
    CustomColors(
        content = Color.White,
        component = Color.White,
        background = Color(0xFF222222)
    )
}

object CustomTheme {
    val brandColors: CustomColors
        @Composable
        get() = if (isSystemInDarkTheme())
            LocalBrandColorsDark.current
        else
            LocalBrandColorsLight.current
    val backgroundColors: CustomColors
        @Composable
        get() = if (isSystemInDarkTheme())
            LocalBackgroundColorsDark.current
        else
            LocalBackgroundColorsLight.current
}
