package com.example.artesaniasaleo.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.unit.dp

// Esquinas redondeadas tomadas del mockup
val Formas = Shapes(
    extraSmall = RoundedCornerShape(8.dp),  // campos de texto
    small = RoundedCornerShape(8.dp),       // chips y menús
    medium = RoundedCornerShape(10.dp),     // tarjetas de producto
    large = RoundedCornerShape(16.dp),      // paneles inferiores
    extraLarge = RoundedCornerShape(28.dp)  // diálogos
)