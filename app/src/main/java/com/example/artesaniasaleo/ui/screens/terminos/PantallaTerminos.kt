package com.example.artesaniasaleo.ui.screens.terminos

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Textos del mockup: qué datos se recolectan y para qué
private val PARRAFOS = listOf(
    "Ale y Leo SpA recolecta únicamente los datos necesarios para gestionar tu cuenta, tu carrito y tus solicitudes de compra dentro de esta aplicación: correo, RUT, nombre, dirección y, opcionalmente, datos de pago.",
    "Tus datos no se comparten con terceros y se utilizan exclusivamente para procesar y hacer seguimiento de tus pedidos. Puedes actualizar o eliminar tu información desde la sección de perfil en cualquier momento.",
    "Esta aplicación corresponde a un prototipo académico (MVP): todos los datos utilizados son sintéticos o ficticios y no existe procesamiento financiero real.",
    "Al marcar la casilla de aceptación, confirmas que has leído y estás de acuerdo con estos términos."
)

@Composable
fun PantallaTerminos(onVolver: () -> Unit, modifier: Modifier = Modifier) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = "Términos y privacidad",
                esPantallaRaiz = false,
                onIconoIzquierdo = onVolver,
                onMenu = {},
                mostrarMenu = false // se ve antes de iniciar sesión
            )
        }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Términos de política y privacidad", style = MaterialTheme.typography.titleLarge)
            PARRAFOS.forEach {
                Text(it, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Preview(name = "Términos", showBackground = true)
@Composable
private fun VistaTerminos() {
    ArtesaniasAleoTheme { PantallaTerminos(onVolver = {}) }
}
