package de.meinerezepte.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val Green = Color(0xFF2E7D32)
private val Cream = Color(0xFFFFF8F0)
private val DarkBrown = Color(0xFF3D2C1E)

private val LightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = Color.White,
    secondary = DarkBrown,
    onSecondary = Color.White,
    background = Cream,
    onBackground = DarkBrown,
    surface = Color.White,
    onSurface = DarkBrown,
)

@Composable
fun MeineRezepteTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        content = content,
    )
}
