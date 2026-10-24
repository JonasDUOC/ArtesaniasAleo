package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Botón principal del mockup: relleno terracota, texto blanco, ancho completo
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Button(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = habilitado,
        shape = MaterialTheme.shapes.small // 8 dp, definido en Shape.kt
    ) {
        Text(texto)
    }
}

// Botón secundario del mockup: fondo transparente, borde y texto terracota
@Composable
fun BotonSecundario(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    val primario = MaterialTheme.colorScheme.primary
    // Deshabilitado: el borde se atenúa, como la opacidad 0.4 del mockup
    val colorBorde = if (habilitado) primario else primario.copy(alpha = 0.4f)

    OutlinedButton(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        enabled = habilitado,
        shape = MaterialTheme.shapes.small,
        border = BorderStroke(1.dp, colorBorde)
    ) {
        Text(texto)
    }
}

@Preview(name = "Botones", showBackground = true)
@Composable
private fun VistaBotones() {
    ArtesaniasAleoTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                BotonPrincipal(texto = "Iniciar sesión", onClick = {})
                BotonSecundario(texto = "Crear cuenta", onClick = {})
                BotonPrincipal(texto = "Guardar cambios", onClick = {}, habilitado = false)
            }
        }
    }
}