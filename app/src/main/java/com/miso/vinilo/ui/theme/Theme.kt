package com.miso.vinilo.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = PrincipalColor,
    onPrimary = BaseWhite,
    secondary = SecondaryColor,
    onSecondary = BaseWhite,
    background = Color.Transparent,
    onBackground = BaseWhite,
    surface = Color.Transparent,
    onSurface = BaseWhite
)

@Composable
fun ViniloTheme(content: @Composable () -> Unit) {

    val colorScheme = DarkColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colorStops = arrayOf(
                            0.0f to BaseBlack,
                            1.0f to PrincipalColor
                        )
                    )
                )
        ) {
            content()
        }
    }
}