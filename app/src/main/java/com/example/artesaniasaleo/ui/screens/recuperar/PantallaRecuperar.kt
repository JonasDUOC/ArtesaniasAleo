package com.example.artesaniasaleo.ui.screens.recuperar

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.RecuperarUiState
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.CampoTexto
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

@Composable
fun PantallaRecuperar(
    estado: RecuperarUiState,
    onCorreoChange: (String) -> Unit,
    onEnviar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = "Recuperar contraseña",
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
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
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
            Spacer(Modifier.height(16.dp))
            BotonPrincipal(texto = "Enviar", onClick = onEnviar, habilitado = estado.correo.isNotBlank())
        }
    }
}

@Preview(name = "Recuperar contraseña", showBackground = true)
@Composable
private fun VistaRecuperar() {
    ArtesaniasAleoTheme {
        PantallaRecuperar(estado = RecuperarUiState(), onCorreoChange = {}, onEnviar = {}, onVolver = {})
    }
}
