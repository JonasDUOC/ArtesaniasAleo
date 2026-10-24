package com.example.artesaniasaleo.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Fuentes del mockup: títulos con serifa (Georgia), textos sin serifa (Arial)
val FuenteTitulos = FontFamily.Serif
val FuenteTextos = FontFamily.SansSerif

// Tamaños por defecto de Material 3; solo cambiamos la fuente
private val Base = Typography()

val Typography = Typography(
    // Títulos grandes
    displayLarge = Base.displayLarge.copy(fontFamily = FuenteTitulos),
    displayMedium = Base.displayMedium.copy(fontFamily = FuenteTitulos),
    displaySmall = Base.displaySmall.copy(fontFamily = FuenteTitulos),
    headlineLarge = Base.headlineLarge.copy(fontFamily = FuenteTitulos),
    headlineMedium = Base.headlineMedium.copy(fontFamily = FuenteTitulos),
    headlineSmall = Base.headlineSmall.copy(fontFamily = FuenteTitulos),
    titleLarge = Base.titleLarge.copy(fontFamily = FuenteTitulos),
    // Barra superior y subtítulos
    titleMedium = Base.titleMedium.copy(fontFamily = FuenteTextos, fontWeight = FontWeight.Bold),
    titleSmall = Base.titleSmall.copy(fontFamily = FuenteTextos, fontWeight = FontWeight.Bold),
    // Textos, campos y botones
    bodyLarge = Base.bodyLarge.copy(fontFamily = FuenteTextos),
    bodyMedium = Base.bodyMedium.copy(fontFamily = FuenteTextos),
    bodySmall = Base.bodySmall.copy(fontFamily = FuenteTextos),
    labelLarge = Base.labelLarge.copy(fontFamily = FuenteTextos),
    labelMedium = Base.labelMedium.copy(fontFamily = FuenteTextos),
    labelSmall = Base.labelSmall.copy(fontFamily = FuenteTextos)
)