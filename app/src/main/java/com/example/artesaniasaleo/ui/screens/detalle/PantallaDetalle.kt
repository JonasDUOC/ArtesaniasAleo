package com.example.artesaniasaleo.ui.screens.detalle

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
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.ui.Alignment
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.PRODUCTOS_DEMO
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.BotonSecundario
import com.example.artesaniasaleo.ui.components.formatearPrecio
import com.example.artesaniasaleo.ui.components.ImagenArtesania
import com.example.artesaniasaleo.ui.components.rutaAUri
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

@Composable
fun PantallaDetalle(
    producto: ProductoUi,
    onFavoritoClick: () -> Unit,
    onAgregarCarrito: () -> Unit,
    onVolver: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(titulo = producto.nombre, esPantallaRaiz = false, onIconoIzquierdo = onVolver, onMenu = onMenu)
        }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .verticalScroll(rememberScrollState())
        ) {
            // CU-16: carrusel de imágenes (se desliza con el dedo). Sin fotos: degradado del mockup
            val imagenes = producto.imagenes
            if (imagenes.size > 1) {
                val estadoCarrusel = rememberPagerState(pageCount = { imagenes.size })
                Box {
                    HorizontalPager(
                        state = estadoCarrusel,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(260.dp)
                    ) { pagina ->
                        ImagenArtesania(
                            imagen = rutaAUri(imagenes[pagina]),
                            descripcion = producto.nombre,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    Text(
                        text = "${estadoCarrusel.currentPage + 1} / ${imagenes.size}",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(8.dp)
                            .background(MaterialTheme.colorScheme.primary, MaterialTheme.shapes.small)
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            } else {
                ImagenArtesania(
                    imagen = rutaAUri(imagenes.firstOrNull()),
                    descripcion = producto.nombre,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Text(producto.nombre, style = MaterialTheme.typography.titleLarge)
                Text(
                    text = formatearPrecio(producto.precio),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.padding(vertical = 8.dp)
                )
                Text(
                    producto.descripcion,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotonSecundario(
                        texto = if (producto.esFavorito) "En favoritos" else "Favorito",
                        onClick = onFavoritoClick,
                        modifier = Modifier.weight(1f)
                    )
                    BotonPrincipal(
                        texto = "Agregar al carrito",
                        onClick = onAgregarCarrito,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Preview(name = "Detalle", showBackground = true)
@Composable
private fun VistaDetalle() {
    ArtesaniasAleoTheme {
        PantallaDetalle(producto = PRODUCTOS_DEMO[0], onFavoritoClick = {}, onAgregarCarrito = {}, onVolver = {}, onMenu = {})
    }
}
