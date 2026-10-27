package com.example.artesaniasaleo.ui.screens.pago

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.DatosPagoUiState
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.FormularioPago
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Se abre al confirmar la compra si el usuario aún no tiene tarjeta guardada
@Composable
fun PantallaDatosPago(
    pago: DatosPagoUiState,
    onPagoChange: (DatosPagoUiState) -> Unit,
    onGuardarYConfirmar: () -> Unit,
    onVolver: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Datos de pago", esPantallaRaiz = false, onIconoIzquierdo = onVolver, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            Text(
                "No tienes una tarjeta guardada. Ingrésala para continuar tu compra (datos ficticios).",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(12.dp))
            FormularioPago(pago = pago, onPagoChange = onPagoChange)
            Spacer(Modifier.height(16.dp))
            BotonPrincipal(texto = "Guardar y confirmar compra", onClick = onGuardarYConfirmar)
        }
    }
}

@Preview(name = "Datos de pago", showBackground = true)
@Composable
private fun VistaDatosPago() {
    ArtesaniasAleoTheme {
        PantallaDatosPago(
            pago = DatosPagoUiState(numero = "1234", errorNumero = "Debe tener 16 dígitos"),
            onPagoChange = {}, onGuardarYConfirmar = {}, onVolver = {}, onMenu = {}
        )
    }
}
