package com.example.artesaniasaleo.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.Etiqueta
import com.example.artesaniasaleo.ui.components.FilaLista
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Alturas de las barras del mockup (0 a 1). Son de ejemplo: no hay datos reales de visitas
private val VISITAS = listOf(0.4f, 0.6f, 0.35f, 0.8f, 0.55f)
private val VENTAS = listOf(0.5f, 0.3f, 0.7f, 0.45f, 0.65f)

// Pantalla de inicio del administrador
@Composable
fun PantallaResumen(
    pedidosPendientes: Int,
    onPendientes: () -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Resumen", esPantallaRaiz = true, onIconoIzquierdo = onInicio, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(16.dp)
        ) {
            Text("Resumen del día", style = MaterialTheme.typography.titleLarge)
            GraficoBarras("Visitas hoy", VISITAS)
            GraficoBarras("Ventas hoy", VENTAS)
            FilaLista(texto = "Pedidos pendientes", onClick = onPendientes) {
                Etiqueta("$pedidosPendientes")
            }
        }
    }
}

// Gráfico simple: cada barra es un Box cuya altura es una fracción del alto total
@Composable
private fun GraficoBarras(titulo: String, valores: List<Float>) {
    Text(
        titulo,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(top = 12.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(80.dp)
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.Bottom
    ) {
        valores.forEach { valor ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(valor)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(topStart = 3.dp, topEnd = 3.dp))
            )
        }
    }
}

@Preview(name = "Resumen", showBackground = true)
@Composable
private fun VistaResumen() {
    ArtesaniasAleoTheme {
        PantallaResumen(pedidosPendientes = 1, onPendientes = {}, onInicio = {}, onMenu = {})
    }
}
