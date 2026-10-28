package com.example.artesaniasaleo.ui.components

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File

// Galería y cámara para las imágenes de un producto (CU-32, Guía 13).
// No guarda nada: entrega los Uri al formulario y el ViewModel los guarda al presionar "Guardar".
@Composable
fun SelectorImagenes(
    imagenes: List<Uri>,
    mensajeError: String?,
    onAgregar: (List<Uri>) -> Unit,
    onQuitar: (Uri) -> Unit,
    modifier: Modifier = Modifier
) {
    val contexto = LocalContext.current

    // Galería: permite elegir varias imágenes. Si el usuario cancela llega una lista vacía.
    val galeria = rememberLauncherForActivityResult(ActivityResultContracts.GetMultipleContents()) { uris ->
        if (uris.isNotEmpty()) onAgregar(uris)
    }

    // Cámara: TakePicture guarda la foto en un Uri que creamos antes con FileProvider.
    // rememberSaveable: si Android cierra la app mientras la cámara está abierta, el Uri no se pierde.
    var uriFoto by rememberSaveable { mutableStateOf<Uri?>(null) }
    val camara = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { exito ->
        val uri = uriFoto
        if (exito && uri != null) onAgregar(listOf(uri)) // si cancela, exito = false y no pasa nada
    }
    val abrirCamara: () -> Unit = {
        val uri = crearUriFoto(contexto)
        uriFoto = uri
        camara.launch(uri)
    }

    // Permiso de cámara: se pide en tiempo de ejecución la primera vez
    val permisoCamara = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) abrirCamara()
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text("Imágenes (${imagenes.size})", style = MaterialTheme.typography.titleSmall)
        Spacer(Modifier.height(8.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            BotonSecundario(
                texto = "Galería",
                onClick = { galeria.launch("image/*") },
                modifier = Modifier.weight(1f)
            )
            BotonSecundario(
                texto = "Cámara",
                onClick = {
                    val tienePermiso = ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                        PackageManager.PERMISSION_GRANTED
                    if (tienePermiso) abrirCamara() else permisoCamara.launch(Manifest.permission.CAMERA)
                },
                modifier = Modifier.weight(1f)
            )
        }
        if (imagenes.isNotEmpty()) {
            Spacer(Modifier.height(8.dp))
            // Miniaturas en el orden del carrusel, cada una con una X para quitarla
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(imagenes) { uri ->
                    Box {
                        ImagenArtesania(
                            imagen = uri,
                            modifier = Modifier.size(84.dp).clip(MaterialTheme.shapes.small)
                        )
                        IconButton(
                            onClick = { onQuitar(uri) },
                            modifier = Modifier.align(Alignment.TopEnd).padding(2.dp).size(24.dp),
                            colors = IconButtonDefaults.iconButtonColors(containerColor = Color.White.copy(alpha = 0.85f))
                        ) {
                            Icon(Icons.Filled.Close, contentDescription = "Quitar imagen", modifier = Modifier.size(14.dp))
                        }
                    }
                }
            }
        }
        if (mensajeError != null) {
            Text(
                text = mensajeError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Archivo temporal para la foto, en la carpeta caché declarada en res/xml/file_paths.xml
private fun crearUriFoto(contexto: Context): Uri {
    val carpeta = File(contexto.cacheDir, "camara").apply { mkdirs() }
    val archivo = File(carpeta, "foto_${System.currentTimeMillis()}.jpg")
    return FileProvider.getUriForFile(contexto, "${contexto.packageName}.fileprovider", archivo)
}
