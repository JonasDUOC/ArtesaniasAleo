package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.DatosPagoUiState
import com.example.artesaniasaleo.model.TIPOS_TARJETA

// Campos de la tarjeta. Se reutiliza en "Datos de pago" y en la pestaña Pago del perfil
@Composable
fun FormularioPago(
    pago: DatosPagoUiState,
    onPagoChange: (DatosPagoUiState) -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true
) {
    Column(modifier = modifier) {
        CampoSelector(
            etiqueta = "Tipo de tarjeta",
            opciones = TIPOS_TARJETA,
            seleccionada = pago.tipo,
            onSeleccionar = { onPagoChange(pago.copy(tipo = it)) },
            habilitado = habilitado
        )
        Spacer(Modifier.height(8.dp))
        CampoTexto(
            etiqueta = "Número de tarjeta",
            valor = pago.numero,
            // Solo dígitos y máximo 16
            onValorChange = { onPagoChange(pago.copy(numero = it.filter(Char::isDigit).take(16))) },
            placeholder = "16 dígitos",
            mensajeError = pago.errorNumero,
            tipoTeclado = KeyboardType.Number,
            habilitado = habilitado
        )
        Spacer(Modifier.height(8.dp))
        CampoTexto(
            etiqueta = "Vencimiento",
            valor = pago.vencimiento,
            onValorChange = { onPagoChange(pago.copy(vencimiento = it.take(5))) },
            placeholder = "MM/AA",
            mensajeError = pago.errorVencimiento,
            habilitado = habilitado
        )
        Spacer(Modifier.height(8.dp))
        CampoTexto(
            etiqueta = "CVV",
            valor = pago.cvv,
            onValorChange = { onPagoChange(pago.copy(cvv = it.filter(Char::isDigit).take(4))) },
            placeholder = "123",
            mensajeError = pago.errorCvv,
            tipoTeclado = KeyboardType.Number,
            habilitado = habilitado
        )
    }
}
