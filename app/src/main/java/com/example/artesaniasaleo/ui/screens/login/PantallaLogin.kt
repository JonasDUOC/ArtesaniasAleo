package com.example.artesaniasaleo.ui.screens.login

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.LoginUiState
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.BotonSecundario
import com.example.artesaniasaleo.ui.components.CampoTexto
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

@Composable
fun PantallaLogin(
    estado: LoginUiState,
    operacion: EstadoOperacion,          // Inactivo / Cargando / Exito / Error, viene del LoginViewModel
    onCorreoChange: (String) -> Unit,
    onContrasenaChange: (String) -> Unit,
    onIniciarSesion: () -> Unit,
    onCrearCuenta: () -> Unit,
    onOlvideContrasena: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Se calculan una vez desde el estado sellado y se usan abajo
    val cargando = operacion is EstadoOperacion.Cargando || operacion is EstadoOperacion.Exito
    val mensajeError = (operacion as? EstadoOperacion.Error)?.mensaje

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalArrangement = Arrangement.Center // contenido centrado en vertical, como el mockup
        ) {
            Text(
                text = "Ale y Leo",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Text(
                text = "Artesanías únicas",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            CampoTexto(
                etiqueta = "Correo",
                valor = estado.correo,
                onValorChange = onCorreoChange,
                placeholder = "tucorreo@mail.com",
                mensajeError = estado.errorCorreo,
                tipoTeclado = KeyboardType.Email,
                habilitado = !cargando
            )

            Spacer(modifier = Modifier.height(12.dp))

            CampoTexto(
                etiqueta = "Contraseña",
                valor = estado.contrasena,
                onValorChange = onContrasenaChange,
                placeholder = "Tu contraseña",
                mensajeError = estado.errorContrasena,
                esContrasena = true,
                habilitado = !cargando
            )

            // Error que no es de un campo (ej.: credenciales incorrectas)
            if (mensajeError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = mensajeError,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            BotonPrincipal(
                texto = if (cargando) "Ingresando..." else "Iniciar sesión",
                onClick = onIniciarSesion,
                habilitado = estado.camposCompletos && !cargando
            )

            Spacer(modifier = Modifier.height(8.dp))

            BotonSecundario(
                texto = "Crear cuenta",
                onClick = onCrearCuenta
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "¿Olvidaste tu contraseña?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOlvideContrasena)
                    .padding(8.dp)
            )
        }
    }
}

@Preview(name = "Login vacío", showBackground = true)
@Composable
private fun VistaLoginVacio() {
    ArtesaniasAleoTheme {
        PantallaLogin(
            estado = LoginUiState(),
            operacion = EstadoOperacion.Inactivo,
            onCorreoChange = {},
            onContrasenaChange = {},
            onIniciarSesion = {},
            onCrearCuenta = {},
            onOlvideContrasena = {}
        )
    }
}

@Preview(name = "Login con errores", showBackground = true)
@Composable
private fun VistaLoginConErrores() {
    ArtesaniasAleoTheme {
        PantallaLogin(
            estado = LoginUiState(
                correo = "maxi@",
                contrasena = "12",
                errorCorreo = "Ingresa un correo válido"
            ),
            operacion = EstadoOperacion.Error("Contraseña incorrecta"),
            onCorreoChange = {},
            onContrasenaChange = {},
            onIniciarSesion = {},
            onCrearCuenta = {},
            onOlvideContrasena = {}
        )
    }
}