package com.example.artesaniasaleo.ui.screens.seguimiento

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.EstadoPedido
import com.example.artesaniasaleo.model.PEDIDOS_DEMO
import com.example.artesaniasaleo.model.PedidoUi
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.Etiqueta
import com.example.artesaniasaleo.ui.components.FilaLista
import com.example.artesaniasaleo.ui.components.formatearPrecio
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Lista de pedidos del cliente con su estado
@Composable
fun PantallaSeguimiento(
    pedidos: List<PedidoUi>,
    onPedidoClick: (Int) -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Seguimiento", esPantallaRaiz = true, onIconoIzquierdo = onInicio, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(horizontal = 16.dp)
        ) {
            if (pedidos.isEmpty()) {
                Text(
                    "No tienes pedidos realizados.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
            pedidos.forEach { pedido ->
                FilaLista(texto = "Pedido #${pedido.id}", onClick = { onPedidoClick(pedido.id) }) {
                    Etiqueta(pedido.estado.etiqueta)
                }
            }
        }
    }
}

// Detalle del pedido: los 3 estados como puntos y el resumen de productos
@Composable
fun PantallaPedido(
    pedido: PedidoUi,
    onVolver: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Pedido #${pedido.id}", esPantallaRaiz = false, onIconoIzquierdo = onVolver, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(16.dp)
        ) {
            LineaEstados(pedido.estado)
            Text(
                text = pedido.lineas.joinToString { "${it.nombre} x${it.cantidad}" } +
                        " — Total ${formatearPrecio(pedido.total)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Estados anteriores en verde salvia, el actual en terracota y los siguientes en gris
@Composable
private fun LineaEstados(actual: EstadoPedido) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        EstadoPedido.entries.forEach { estado ->
            val color = when {
                estado.ordinal < actual.ordinal -> MaterialTheme.colorScheme.secondary
                estado == actual -> MaterialTheme.colorScheme.primary
                else -> MaterialTheme.colorScheme.outline
            }
            Column(modifier = Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .background(color, CircleShape)
                )
                Text(
                    estado.etiqueta,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }
    }
}

@Preview(name = "Seguimiento", showBackground = true)
@Composable
private fun VistaSeguimiento() {
    ArtesaniasAleoTheme {
        PantallaSeguimiento(pedidos = PEDIDOS_DEMO, onPedidoClick = {}, onInicio = {}, onMenu = {})
    }
}

@Preview(name = "Pedido", showBackground = true)
@Composable
private fun VistaPedido() {
    ArtesaniasAleoTheme {
        PantallaPedido(pedido = PEDIDOS_DEMO[1], onVolver = {}, onMenu = {})
    }
}
