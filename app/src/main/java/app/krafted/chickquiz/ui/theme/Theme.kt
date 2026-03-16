package app.krafted.chickquiz.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val ChickQuizColorScheme = lightColorScheme(
    primary = ChickYellow,
    onPrimary = OnLight,
    primaryContainer = ChickYellowLight,
    onPrimaryContainer = OnLight,
    secondary = BarnRed,
    onSecondary = OnDark,
    secondaryContainer = BarnRedLight,
    onSecondaryContainer = OnDark,
    tertiary = GrassGreen,
    onTertiary = OnDark,
    tertiaryContainer = GrassGreenLight,
    onTertiaryContainer = OnLight,
    background = CoopCream,
    onBackground = OnLight,
    surface = CoopCream,
    onSurface = OnLight,
    surfaceVariant = CoopBrownLight,
    onSurfaceVariant = OnDark,
    error = WrongRed,
    onError = OnDark
)

@Composable
fun ChickQuizTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = ChickQuizColorScheme,
        typography = Typography,
        content = content
    )
}
