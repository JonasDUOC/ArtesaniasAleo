package com.example.artesaniasaleo.ui.screens.recuperar

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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.RecuperarUiState
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.CampoTexto
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// CU-07 en dos etapas: 1 correo → 2 código + nueva contraseña (mismo estilo que el registro)
@Composable
fun PantallaRecuperar(
    estado: RecuperarUiState,
    operacion: EstadoOperacion,          // Cargando / Error, viene del RecuperarViewModel
    onCorreoChange: (String) -> Unit,
    onCodigoChange: (String) -> Unit,
    onNuevaChange: (String) -> Unit,
    onConfirmarChange: (String) -> Unit,
    onContinuar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cargando = operacion is EstadoOperacion.Cargando || operacion is EstadoOperacion.Exito
    val mensajeError = (operacion as? EstadoOperacion.Error)?.mensaje

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = if (estado.paso == 1) "Recuperar contraseña" else "Nueva contraseña",
                esPantallaRaiz = false,
                onIconoIzquierdo = onVolver,
                onMenu = {},
                mostrarMenu = false
            )
        }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            if (estado.paso == 1) {
                Text(
                    "Ingresa tu correo y te enviaremos un código para restablecer tu contraseña.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(16.dp))
                CampoTexto(
                    etiqueta = "Correo",
                    valor = estado.correo,
                    onValorChange = onCorreoChange,
                    placeholder = "tucorreo@mail.com",
                    mensajeError = estado.errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )
            } else {
                Text(
                    "Enviamos un código a ${estado.correo}.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(12.dp))
                CampoTexto(
                    etiqueta = "Código de verificación",
                    valor = estado.codigo,
                    onValorChange = onCodigoChange,
                    placeholder = "4 dígitos",
                    mensajeError = estado.errorCodigo,
                    tipoTeclado = KeyboardType.Number
                )
                Text(
                    "Demo: el código enviado es 1234.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(Modifier.height(12.dp))
                CampoTexto(
                    etiqueta = "Nueva contraseña",
                    valor = estado.nueva,
                    onValorChange = onNuevaChange,
                    placeholder = "mín. 6 caracteres",
                    mensajeError = estado.errorNueva,
                    esContrasena = true
                )
                Spacer(Modifier.height(12.dp))
                CampoTexto(
                    etiqueta = "Confirmar contraseña",
                    valor = estado.confirmar,
                    onValorChange = onConfirmarChange,
                    mensajeError = estado.errorConfirmar,
                    esContrasena = true
                )
            }

            if (mensajeError != null) {
                Spacer(Modifier.height(8.dp))
                Text(mensajeError, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.error)
            }

            Spacer(Modifier.height(16.dp))
            BotonPrincipal(
                texto = when {
                    cargando -> "Procesando..."
                    estado.paso == 1 -> "Enviar código"
                    else -> "Cambiar contraseña"
                },
                onClick = onContinuar,
                habilitado = estado.puedeContinuar && !cargando
            )
        }
    }
}

@Preview(name = "Recuperar: correo", showBackground = true)
@Composable
private fun VistaRecuperarCorreo() {
    ArtesaniasAleoTheme {
        PantallaRecuperar(
            estado = RecuperarUiState(),
            operacion = EstadoOperacion.Inactivo,
            onCorreoChange = {}, onCodigoChange = {}, onNuevaChange = {}, onConfirmarChange = {},
            onContinuar = {}, onVolver = {}
        )
    }
}

@Preview(name = "Recuperar: nueva contraseña", showBackground = true)
@Composable
private fun VistaRecuperarNueva() {
    ArtesaniasAleoTheme {
        PantallaRecuperar(
            estado = RecuperarUiState(paso = 2, correo = "ana@mail.com", codigo = "12", errorNueva = "Debe tener al menos 6 caracteres"),
            operacion = EstadoOperacion.Inactivo,
            onCorreoChange = {}, onCodigoChange = {}, onNuevaChange = {}, onConfirmarChange = {},
            onContinuar = {}, onVolver = {}
        )
    }
}
