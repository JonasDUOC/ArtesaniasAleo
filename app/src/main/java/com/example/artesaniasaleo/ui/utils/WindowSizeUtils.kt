package com.example.artesaniasaleo.ui.utils

import android.app.Activity
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

// Guía 9 (Window Size Classes): calcula el tamaño de la pantalla para toda la app.
// Compact = teléfono vertical, Medium = teléfono horizontal o tablet chica, Expanded = tablet.
@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Composable
fun obtenerWindowSizeClass(): WindowSizeClass {
    val activity = LocalContext.current as Activity
    return calculateWindowSizeClass(activity)
}

// Columnas de la grilla de productos según el ancho: más espacio → más tarjetas por fila
fun columnasGrilla(ancho: WindowWidthSizeClass): Int = when (ancho) {
    WindowWidthSizeClass.Compact -> 2
    WindowWidthSizeClass.Medium -> 3
    else -> 4 // Expanded
}
