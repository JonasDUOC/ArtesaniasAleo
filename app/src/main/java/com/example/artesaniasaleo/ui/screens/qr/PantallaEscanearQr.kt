package com.example.artesaniasaleo.ui.screens.qr

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.artesaniasaleo.model.EstadoQr
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.BotonSecundario
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors

// Escanear QR (mockup "CU-12 · Catálogo / Escanear QR"): cámara a pantalla completa,
// recuadro guía y aviso "Código no reconocido". Lo que significa el código lo decide QrViewModel.
@Composable
fun PantallaEscanearQr(
    estado: EstadoQr,
    onCodigoLeido: (String) -> Unit,
    onReintentar: () -> Unit,
    onVolver: () -> Unit,
    onMenu: () -> Unit
) {
    val contexto = LocalContext.current

    // Permiso de cámara: si ya estaba concedido no se vuelve a preguntar
    var tienePermiso by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(contexto, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
        )
    }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) {
        tienePermiso = it
    }

    Scaffold(
        topBar = {
            BarraSuperior(titulo = "Escanear QR", esPantallaRaiz = false, onIconoIzquierdo = onVolver, onMenu = onMenu)
        }
    ) { espacioBarra ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .background(Color.Black)
        ) {
            if (tienePermiso) {
                VistaCamara(
                    escaneando = estado == EstadoQr.Escaneando,
                    onCodigoLeido = onCodigoLeido
                )
                Guia()
            } else {
                SinPermiso(onPedir = { pedirPermiso.launch(Manifest.permission.CAMERA) })
            }

            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (estado == EstadoQr.NoReconocido) {
                    AvisoNoReconocido(onReintentar)
                }
                // Fondo claro detrás del botón para que se lea sobre la imagen de la cámara
                BotonSecundario(
                    texto = "Cancelar",
                    onClick = onVolver,
                    modifier = Modifier.background(MaterialTheme.colorScheme.surface, MaterialTheme.shapes.small)
                )
            }
        }
    }
}

// Vista previa de CameraX + análisis de cada cuadro con ML Kit
@OptIn(ExperimentalGetImage::class)
@Composable
private fun VistaCamara(escaneando: Boolean, onCodigoLeido: (String) -> Unit) {
    val contexto = LocalContext.current
    val ciclo = LocalLifecycleOwner.current
    // rememberUpdatedState: el analizador (creado una vez) siempre ve los valores actuales
    val buscar by rememberUpdatedState(escaneando)
    val avisar by rememberUpdatedState(onCodigoLeido)

    // Solo QR: ML Kit no pierde tiempo buscando códigos de barras de otros tipos
    val lector = remember {
        BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder().setBarcodeFormats(Barcode.FORMAT_QR_CODE).build()
        )
    }
    // Hilo aparte para analizar imágenes, así la interfaz no se congela
    val hilo = remember { Executors.newSingleThreadExecutor() }

    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            val vistaPrevia = PreviewView(ctx)
            val proveedorFuturo = ProcessCameraProvider.getInstance(ctx)
            proveedorFuturo.addListener({
                val proveedor = proveedorFuturo.get()
                val preview = Preview.Builder().build().also { it.setSurfaceProvider(vistaPrevia.surfaceProvider) }
                // KEEP_ONLY_LATEST: si ML Kit va lento, descarta cuadros viejos en vez de acumularlos
                val analisis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .build()
                analisis.setAnalyzer(hilo) { cuadro ->
                    val imagen = cuadro.image
                    if (imagen == null || !buscar) {
                        cuadro.close()
                        return@setAnalyzer
                    }
                    val entrada = InputImage.fromMediaImage(imagen, cuadro.imageInfo.rotationDegrees)
                    lector.process(entrada)
                        .addOnSuccessListener { codigos ->
                            codigos.firstOrNull()?.rawValue?.let { avisar(it) }
                        }
                        // Siempre cerrar el cuadro: si no, CameraX deja de enviar imágenes
                        .addOnCompleteListener { cuadro.close() }
                }
                proveedor.unbindAll()
                // Ligada al ciclo de vida: la cámara se apaga sola al salir de la pantalla
                proveedor.bindToLifecycle(ciclo, CameraSelector.DEFAULT_BACK_CAMERA, preview, analisis)
            }, ContextCompat.getMainExecutor(ctx))
            vistaPrevia
        }
    )

    // Al salir de la pantalla se liberan el lector y el hilo
    DisposableEffect(Unit) {
        onDispose {
            lector.close()
            hilo.shutdown()
        }
    }
}

// Texto "Apunta al código QR de la pieza" y recuadro guía del mockup
@Composable
private fun Guia() {
    Column(
        modifier = Modifier.fillMaxSize().padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Apunta al código QR de la pieza",
            color = Color.White,
            modifier = Modifier
                .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(20.dp))
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )
        Spacer(Modifier.height(32.dp))
        Box(
            modifier = Modifier
                .size(240.dp)
                .border(BorderStroke(2.dp, Color.White), RoundedCornerShape(16.dp))
        )
    }
}

@Composable
private fun AvisoNoReconocido(onReintentar: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Código no reconocido", style = MaterialTheme.typography.titleMedium)
            Text(
                "Prueba con el código QR de una pieza de Ale y Leo.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BotonPrincipal(texto = "Intentar de nuevo", onClick = onReintentar)
        }
    }
}

@Composable
private fun SinPermiso(onPedir: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            "Para escanear necesitamos usar la cámara.",
            color = Color.White,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        BotonPrincipal(texto = "Permitir cámara", onClick = onPedir)
    }
}
