package com.example.artesaniasaleo.ui.screens.carrito

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.PRODUCTOS_DEMO
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.formatearPrecio
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

@Composable
fun PantallaCarrito(
    items: List<Pair<ProductoUi, Int>>,      // producto y cantidad
    onCambiarCantidad: (Int, Int) -> Unit,   // id del producto y +1 o -1
    onConfirmarCompra: () -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // El total se calcula a partir de los items, no se guarda aparte
    val total = items.sumOf { (producto, cantidad) -> producto.precio * cantidad }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Carrito", esPantallaRaiz = true, onIconoIzquierdo = onInicio, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (items.isEmpty()) {
                Text(
                    "Tu carrito está vacío",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 32.dp)
                )
            } else {
                items.forEach { (producto, cantidad) ->
                    FilaCarrito(producto, cantidad, onCambiarCantidad)
                }
                Text(
                    text = "Total: ${formatearPrecio(total)}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.End,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                )
                BotonPrincipal(texto = "Confirmar compra", onClick = onConfirmarCompra)
            }
        }
    }
}

// Fila: miniatura, nombre y precio, y los botones - cantidad +
@Composable
private fun FilaCarrito(producto: ProductoUi, cantidad: Int, onCambiarCantidad: (Int, Int) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(MaterialTheme.shapes.small)
                .background(
                    Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
                )
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(producto.nombre, style = MaterialTheme.typography.bodyMedium)
            Text(
                formatearPrecio(producto.precio),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        BotonCantidad("-") { onCambiarCantidad(producto.id, -1) }
        Text("$cantidad", style = MaterialTheme.typography.bodyMedium)
        BotonCantidad("+") { onCambiarCantidad(producto.id, 1) }
    }
    HorizontalDivider(color = MaterialTheme.colorScheme.outline)
}

@Composable
private fun BotonCantidad(texto: String, onClick: () -> Unit) {
    FilledIconButton(
        onClick = onClick,
        modifier = Modifier.size(28.dp),
        colors = IconButtonDefaults.filledIconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        Text(texto)
    }
}

@Preview(name = "Carrito", showBackground = true)
@Composable
private fun VistaCarrito() {
    ArtesaniasAleoTheme {
        PantallaCarrito(
            items = listOf(PRODUCTOS_DEMO[0] to 1, PRODUCTOS_DEMO[1] to 2),
            onCambiarCantidad = { _, _ -> }, onConfirmarCompra = {}, onInicio = {}, onMenu = {}
        )
    }
}

@Preview(name = "Carrito vacío", showBackground = true)
@Composable
private fun VistaCarritoVacio() {
    ArtesaniasAleoTheme {
        PantallaCarrito(items = emptyList(), onCambiarCantidad = { _, _ -> }, onConfirmarCompra = {}, onInicio = {}, onMenu = {})
    }
}
