package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Pantalla completa con un indicador de carga (ej.: al abrir la app, mientras se revisa la sesión)
@Composable
fun PantallaCarga(modifier: Modifier = Modifier) {
    Surface(modifier = modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Box(contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun VistaPantallaCarga() {
    ArtesaniasAleoTheme { PantallaCarga() }
}
