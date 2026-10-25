package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Campo del mockup: etiqueta arriba, campo con borde y error en rojo debajo.
// No guarda el texto: lo recibe en "valor" y avisa los cambios con "onValorChange".
@Composable
fun CampoTexto(
    etiqueta: String,
    valor: String,
    onValorChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    mensajeError: String? = null, // null = sin error
    esContrasena: Boolean = false,
    tipoTeclado: KeyboardType = KeyboardType.Text,
    habilitado: Boolean = true
) {
    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        OutlinedTextField(
            value = valor,
            onValueChange = onValorChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder) },
            isError = mensajeError != null,
            supportingText = if (mensajeError != null) {
                { Text(mensajeError) }
            } else null,
            // Oculta los caracteres si es contraseña
            visualTransformation = if (esContrasena) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(
                keyboardType = if (esContrasena) KeyboardType.Password else tipoTeclado
            ),
            singleLine = true,
            enabled = habilitado
        )
    }
}

@Preview(name = "Campos", showBackground = true)
@Composable
private fun VistaCampos() {
    ArtesaniasAleoTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoTexto(etiqueta = "Correo", valor = "", onValorChange = {}, placeholder = "tucorreo@mail.com")
                CampoTexto(etiqueta = "Contraseña", valor = "1234", onValorChange = {}, esContrasena = true)
                CampoTexto(
                    etiqueta = "Correo",
                    valor = "maxi@",
                    onValorChange = {},
                    mensajeError = "Ingresa un correo válido"
                )
            }
        }
    }
}