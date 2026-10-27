package com.example.artesaniasaleo.ui.screens.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.EstadoPedido
import com.example.artesaniasaleo.model.PEDIDOS_DEMO
import com.example.artesaniasaleo.model.PedidoUi
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.BotonSecundario
import com.example.artesaniasaleo.ui.components.Etiqueta
import com.example.artesaniasaleo.ui.components.FilaLista
import com.example.artesaniasaleo.ui.components.formatearPrecio
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Todos los pedidos, el más reciente primero
@Composable
fun PantallaNotificaciones(
    pedidos: List<PedidoUi>,
    onPedidoClick: (Int) -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Notificaciones", esPantallaRaiz = true, onIconoIzquierdo = onInicio, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
        ) {
            if (pedidos.isEmpty()) {
                Text("No hay pedidos.", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
            }
            pedidos.sortedByDescending { it.id }.forEach { pedido ->
                FilaLista(texto = "Pedido #${pedido.id} — ${pedido.cliente}", onClick = { onPedidoClick(pedido.id) }) {
                    Etiqueta(pedido.estado.etiqueta)
                }
            }
        }
    }
}

// Detalle para despachar: datos del cliente, dirección, productos y botones de estado
@Composable
fun PantallaPedidoAdmin(
    pedido: PedidoUi,
    onCambiarEstado: (EstadoPedido) -> Unit,
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
                .verticalScroll(rememberScrollState())
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(
                        Brush.linearGradient(listOf(MaterialTheme.colorScheme.secondary, MaterialTheme.colorScheme.primary))
                    )
            )
            Column(modifier = Modifier.padding(16.dp)) {
                Seccion("Datos del cliente", "${pedido.cliente} · ${pedido.correo} · ${pedido.telefono}")
                Seccion("Dirección", pedido.direccion)
                Seccion(
                    "Pedido",
                    pedido.lineas.joinToString { "${it.nombre} x${it.cantidad}" } + " — Total ${formatearPrecio(pedido.total)}"
                )
                Spacer(Modifier.height(16.dp))
                // Cada botón solo se habilita en el estado anterior que le corresponde
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotonSecundario(
                        texto = "En proceso",
                        onClick = { onCambiarEstado(EstadoPedido.EN_PREPARACION) },
                        habilitado = pedido.estado == EstadoPedido.CONFIRMACION_PAGO,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPrincipal(
                        texto = "Enviado",
                        onClick = { onCambiarEstado(EstadoPedido.ENVIADO) },
                        habilitado = pedido.estado == EstadoPedido.EN_PREPARACION,
                        modifier = Modifier.weight(1f)
                    )
                }
                Text(
                    "Estado actual: ${pedido.estado.etiqueta}",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 12.dp)
                )
            }
        }
    }
}

@Composable
private fun Seccion(titulo: String, texto: String) {
    Text(titulo, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp))
    Text(texto, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Preview(name = "Notificaciones", showBackground = true)
@Composable
private fun VistaNotificaciones() {
    ArtesaniasAleoTheme {
        PantallaNotificaciones(pedidos = PEDIDOS_DEMO, onPedidoClick = {}, onInicio = {}, onMenu = {})
    }
}

@Preview(name = "Pedido (admin)", showBackground = true)
@Composable
private fun VistaPedidoAdmin() {
    ArtesaniasAleoTheme {
        PantallaPedidoAdmin(pedido = PEDIDOS_DEMO[0], onCambiarEstado = {}, onVolver = {}, onMenu = {})
    }
}
