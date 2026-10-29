package com.example.artesaniasaleo.ui.components

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

// Muestra la foto de una artesanía (Guía 13: componente de imagen reutilizable).
// Sirve para fotos guardadas por la app (ruta de archivo) y para fotos recién elegidas
// de la galería o la cámara (Uri). Si no hay imagen, muestra el degradado del mockup.
@Composable
fun ImagenArtesania(
    imagen: Uri?,
    modifier: Modifier = Modifier,
    descripcion: String? = null
) {
    val contexto = LocalContext.current
    // Cargar una foto demora: se hace fuera del hilo principal y la pantalla se redibuja al terminar
    val bitmap by produceState<ImageBitmap?>(initialValue = null, imagen) {
        value = imagen?.let { withContext(Dispatchers.IO) { cargarImagen(contexto, it) } }
    }
    val actual = bitmap
    if (actual != null) {
        Image(
            bitmap = actual,
            contentDescription = descripcion,
            contentScale = ContentScale.Crop, // llena el espacio recortando los bordes
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier.background(
                Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.secondary))
            )
        )
    }
}

// Las fotos guardadas en la BD se convierten a Uri para mostrarlas. Hay dos tipos:
// - "android.resource://..." → foto de los datos semilla, viene dentro del APK (res/drawable-nodpi)
// - "/data/.../files/productos/x.jpg" → foto que el admin agregó (archivo copiado por ProductoRepository)
fun rutaAUri(ruta: String?): Uri? = when {
    ruta == null -> null
    ruta.startsWith("android.resource://") -> Uri.parse(ruta)
    else -> Uri.fromFile(File(ruta))
}

// Lee la imagen reducida (las fotos de cámara pueden pesar varios MB y llenar la memoria)
private fun cargarImagen(contexto: Context, uri: Uri, ladoMaximo: Int = 800): ImageBitmap? = try {
    val medidas = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, medidas) }
    var escala = 1
    while (medidas.outWidth / escala > ladoMaximo || medidas.outHeight / escala > ladoMaximo) escala *= 2
    val opciones = BitmapFactory.Options().apply { inSampleSize = escala }
    contexto.contentResolver.openInputStream(uri)?.use { BitmapFactory.decodeStream(it, null, opciones) }
        ?.asImageBitmap()
} catch (e: Exception) {
    null // si el archivo no existe o no se puede leer, se muestra el degradado
}
