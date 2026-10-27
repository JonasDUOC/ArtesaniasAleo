package com.example.artesaniasaleo.ui.screens.categorias

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.CATEGORIAS_DEMO
import com.example.artesaniasaleo.model.CategoriaUi
import com.example.artesaniasaleo.model.PRODUCTOS_DEMO
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.FilaLista
import com.example.artesaniasaleo.ui.components.GrillaProductos
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Lista de categorías; al tocar una se abre la grilla filtrada
@Composable
fun PantallaCategorias(
    categorias: List<CategoriaUi>,
    onCategoriaClick: (Int) -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Categorías", esPantallaRaiz = true, onIconoIzquierdo = onInicio, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(horizontal = 16.dp)
        ) {
            categorias.forEach { categoria ->
                FilaLista(texto = categoria.nombre, onClick = { onCategoriaClick(categoria.id) }) {
                    Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                }
            }
        }
    }
}

// Productos de una sola categoría (pantalla hija: flecha para volver)
@Composable
fun PantallaCategoria(
    nombre: String,
    productos: List<ProductoUi>,
    onProductoClick: (Int) -> Unit,
    onFavoritoClick: (Int) -> Unit,
    onVolver: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = nombre, esPantallaRaiz = false, onIconoIzquierdo = onVolver, onMenu = onMenu) }
    ) { espacioBarra ->
        GrillaProductos(
            productos = productos,
            mensajeVacio = "No hay artesanías en esta categoría.",
            onProductoClick = onProductoClick,
            onFavoritoClick = onFavoritoClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(horizontal = 16.dp)
        )
    }
}

@Preview(name = "Categorías", showBackground = true)
@Composable
private fun VistaCategorias() {
    ArtesaniasAleoTheme {
        PantallaCategorias(categorias = CATEGORIAS_DEMO, onCategoriaClick = {}, onInicio = {}, onMenu = {})
    }
}

@Preview(name = "Categoría filtrada", showBackground = true)
@Composable
private fun VistaCategoria() {
    ArtesaniasAleoTheme {
        PantallaCategoria(
            nombre = "Cerámica",
            productos = PRODUCTOS_DEMO.filter { it.categoriaId == 1 },
            onProductoClick = {}, onFavoritoClick = {}, onVolver = {}, onMenu = {}
        )
    }
}
