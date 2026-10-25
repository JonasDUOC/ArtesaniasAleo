package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme
import java.text.NumberFormat
import java.util.Locale

// Tarjeta del catálogo: imagen, botón de favorito, nombre y precio
@Composable
fun TarjetaProducto(
    nombre: String,
    precio: Int,
    esFavorito: Boolean,
    onClick: () -> Unit,
    onFavoritoClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier,
        shape = MaterialTheme.shapes.medium, // 10 dp, definido en Shape.kt
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant // color chip del mockup
        )
    ) {
        Box {
            // Imagen provisional: degradado terracota a salvia, como en el mockup
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        Brush.linearGradient(
                            listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary)
                        )
                    )
            )
            IconButton(
                onClick = onFavoritoClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(4.dp)
                    .size(32.dp),
                colors = IconButtonDefaults.iconButtonColors(
                    containerColor = Color.White.copy(alpha = 0.85f)
                )
            ) {
                Icon(
                    imageVector = if (esFavorito) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (esFavorito) "Quitar de favoritos" else "Agregar a favoritos",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
        Text(
            text = nombre,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            minLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 6.dp)
        )
        Text(
            text = formatearPrecio(precio),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 8.dp, end = 8.dp, top = 2.dp, bottom = 8.dp)
        )
    }
}

// Formato de pesos chilenos: 12990 -> "$12.990"
fun formatearPrecio(precio: Int): String =
    "$" + NumberFormat.getIntegerInstance(Locale.forLanguageTag("es-CL")).format(precio)

@Preview(name = "Tarjetas", showBackground = true, widthDp = 360)
@Composable
private fun VistaTarjetas() {
    ArtesaniasAleoTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Row(
                modifier = Modifier.padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TarjetaProducto(
                    nombre = "Cuenco de cerámica",
                    precio = 12990,
                    esFavorito = false,
                    onClick = {},
                    onFavoritoClick = {},
                    modifier = Modifier.weight(1f)
                )
                TarjetaProducto(
                    nombre = "Tapiz de lana tejido a mano",
                    precio = 34500,
                    esFavorito = true,
                    onClick = {},
                    onFavoritoClick = {},
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}