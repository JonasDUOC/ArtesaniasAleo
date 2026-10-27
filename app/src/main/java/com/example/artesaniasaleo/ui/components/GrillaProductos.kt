package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.ProductoUi

// Grilla de 2 columnas con tarjetas de producto; si la lista está vacía muestra un mensaje
@Composable
fun GrillaProductos(
    productos: List<ProductoUi>,
    mensajeVacio: String,
    onProductoClick: (Int) -> Unit,
    onFavoritoClick: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    if (productos.isEmpty()) {
        Text(
            text = mensajeVacio,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = modifier.padding(top = 12.dp)
        )
    } else {
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            modifier = modifier,
            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(productos, key = { it.id }) { producto ->
                TarjetaProducto(
                    nombre = producto.nombre,
                    precio = producto.precio,
                    esFavorito = producto.esFavorito,
                    onClick = { onProductoClick(producto.id) },
                    onFavoritoClick = { onFavoritoClick(producto.id) }
                )
            }
        }
    }
}
