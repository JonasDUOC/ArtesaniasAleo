package com.example.artesaniasaleo.ui.screens.favoritos

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.PRODUCTOS_DEMO
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.GrillaProductos
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

@Composable
fun PantallaFavoritos(
    favoritos: List<ProductoUi>,
    onProductoClick: (Int) -> Unit,
    onFavoritoClick: (Int) -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Favoritos", esPantallaRaiz = true, onIconoIzquierdo = onInicio, onMenu = onMenu) }
    ) { espacioBarra ->
        GrillaProductos(
            productos = favoritos,
            mensajeVacio = "Aún no tienes favoritos.",
            onProductoClick = onProductoClick,
            onFavoritoClick = onFavoritoClick,
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(horizontal = 16.dp)
        )
    }
}

@Preview(name = "Favoritos", showBackground = true)
@Composable
private fun VistaFavoritos() {
    ArtesaniasAleoTheme {
        PantallaFavoritos(
            favoritos = PRODUCTOS_DEMO.filter { it.esFavorito },
            onProductoClick = {}, onFavoritoClick = {}, onInicio = {}, onMenu = {}
        )
    }
}
