package com.example.artesaniasaleo.ui.theme

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Muestra de prueba para revisar colores y fuentes del tema (no se usa en la app)
@Composable
private fun MuestraTema() {
    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Artesanías Ale y Leo", style = MaterialTheme.typography.headlineMedium)
            Text("Texto normal de la app", style = MaterialTheme.typography.bodyLarge)
            Text(
                "Texto secundario",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Button(onClick = {}) { Text("Iniciar sesión") }
            OutlinedTextField(value = "", onValueChange = {}, label = { Text("Correo") })
            Card { Text("Tarjeta de producto", modifier = Modifier.padding(16.dp)) }
        }
    }
}

@Preview(name = "Claro", showBackground = true)
@Composable
private fun VistaClara() {
    ArtesaniasAleoTheme(darkTheme = false) { MuestraTema() }
}

@Preview(name = "Oscuro", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun VistaOscura() {
    ArtesaniasAleoTheme(darkTheme = true) { MuestraTema() }
}