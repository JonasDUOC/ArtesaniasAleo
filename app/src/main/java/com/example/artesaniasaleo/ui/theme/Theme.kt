package com.example.artesaniasaleo.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TerracotaOscuro,
    onPrimary = Color.White,
    secondary = SalviaOscuro,
    onSecondary = FondoOscuro,
    background = FondoOscuro,
    onBackground = TextoOscuro,
    surface = TarjetaOscuro,
    onSurface = TextoOscuro,
    // Tarjetas, menús, diálogos y barra inferior usan estos roles
    surfaceContainerLowest = TarjetaOscuro,
    surfaceContainerLow = TarjetaOscuro,
    surfaceContainer = TarjetaOscuro,
    surfaceContainerHigh = TarjetaOscuro,
    surfaceContainerHighest = TarjetaOscuro,

    surfaceVariant = ChipOscuro,
    onSurfaceVariant = TextoSecundarioOscuro,
    outline = LineaOscuro,
    error = RojoError,
    onError = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = Terracota,
    onPrimary = Color.White,
    secondary = Salvia,
    onSecondary = Color.White,
    background = Fondo,
    onBackground = Texto,
    surface = Tarjeta,
    onSurface = Texto,
    // Tarjetas, menús, diálogos y barra inferior usan estos roles
    surfaceContainerLowest = Tarjeta,
    surfaceContainerLow = Tarjeta,
    surfaceContainer = Tarjeta,
    surfaceContainerHigh = Tarjeta,
    surfaceContainerHighest = Tarjeta,

    surfaceVariant = Chip,
    onSurfaceVariant = TextoSecundario,
    outline = Linea,
    error = RojoError,
    onError = Color.White
)

@Composable
fun ArtesaniasAleoTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}