package com.example.artesaniasaleo.ui.screens.catalogo

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.R
import com.example.artesaniasaleo.model.CatalogoUiState
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.PRODUCTOS_DEMO
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.TarjetaProducto
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Pantalla "Inicio" del mockup: buscador y grilla de productos en 2 columnas
@Composable
fun PantallaCatalogo(
    estado: CatalogoUiState,
    carga: EstadoOperacion,           // Cargando / Exito / Error, viene del CatalogoViewModel
    onBusquedaChange: (String) -> Unit,
    onProductoClick: (Int) -> Unit,   // recibe el id del producto
    onFavoritoClick: (Int) -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
    onEscanear: () -> Unit = {},     // abre la cámara para leer un QR
    columnas: Int = 2 // 2, 3 o 4 según WindowSizeUtils (Guía 9)
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = "Inicio",
                esPantallaRaiz = true, // muestra la casa, no la flecha
                onIconoIzquierdo = onInicio,
                onMenu = onMenu
            )
        }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            // Buscador y, a su derecha, el botón para escanear un QR (como en el mockup)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.weight(1f)) {
                    Buscador(valor = estado.busqueda, onValorChange = onBusquedaChange)
                }
                FilledTonalIconButton(onClick = onEscanear, modifier = Modifier.padding(start = 8.dp).size(56.dp)) {
                    Icon(painterResource(R.drawable.ic_qr), contentDescription = "Escanear QR")
                }
            }
            // Separación FIJA bajo el buscador (igual a la de arriba: 12 dp). Va fuera de la grilla:
            // si fuera contentPadding de la grilla, se iría con el scroll y las tarjetas
            // quedarían pegadas al buscador
            Spacer(Modifier.height(12.dp))

            when {
                carga is EstadoOperacion.Cargando -> Centrado { CircularProgressIndicator() }
                carga is EstadoOperacion.Error -> Centrado {
                    Text(carga.mensaje, color = MaterialTheme.colorScheme.error)
                }
                estado.productosFiltrados.isEmpty() -> Text(
                    text = "Sin resultados",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                // Lazy: solo dibuja las tarjetas visibles, aunque haya muchos productos
                else -> LazyVerticalGrid(
                    columns = GridCells.Fixed(columnas),
                    contentPadding = PaddingValues(bottom = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(estado.productosFiltrados, key = { it.id }) { producto ->
                        TarjetaProducto(
                            nombre = producto.nombre,
                            precio = producto.precio,
                            esFavorito = producto.esFavorito,
                            imagenUrl = producto.imagenUrl,
                            onClick = { onProductoClick(producto.id) },
                            onFavoritoClick = { onFavoritoClick(producto.id) }
                        )
                    }
                }
            }
        }
    }
}

// Buscador del mockup: fondo color chip, lupa a la izquierda, sin línea inferior
@Composable
private fun Buscador(valor: String, onValorChange: (String) -> Unit) {
    TextField(
        value = valor,
        onValueChange = onValorChange,
        modifier = Modifier.fillMaxWidth(),
        placeholder = { Text("Buscar artesanías…") },
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        singleLine = true,
        shape = RoundedCornerShape(14.dp), // border-radius del buscador en el mockup
        colors = TextFieldDefaults.colors(
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent
        )
    )
}

// Centra un contenido en el espacio que queda bajo el buscador
@Composable
private fun Centrado(contenido: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        contenido()
    }
}

@Preview(name = "Catálogo", showBackground = true)
@Composable
private fun VistaCatalogo() {
    ArtesaniasAleoTheme {
        PantallaCatalogo(
            estado = CatalogoUiState(productos = PRODUCTOS_DEMO),
            carga = EstadoOperacion.Exito,
            onBusquedaChange = {}, onProductoClick = {}, onFavoritoClick = {}, onInicio = {}, onMenu = {}
        )
    }
}

@Preview(name = "Catálogo sin resultados", showBackground = true)
@Composable
private fun VistaCatalogoSinResultados() {
    ArtesaniasAleoTheme {
        PantallaCatalogo(
            estado = CatalogoUiState(busqueda = "mesa", productos = PRODUCTOS_DEMO),
            carga = EstadoOperacion.Exito,
            onBusquedaChange = {}, onProductoClick = {}, onFavoritoClick = {}, onInicio = {}, onMenu = {}
        )
    }
}
