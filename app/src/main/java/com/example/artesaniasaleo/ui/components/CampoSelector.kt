package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuAnchorType
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Selector del mockup (<select>): etiqueta arriba, lista desplegable y error abajo
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CampoSelector(
    etiqueta: String,
    opciones: List<String>,
    seleccionada: String?,             // null = todavía no se elige nada
    onSeleccionar: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Selecciona una opción",
    mensajeError: String? = null,
    habilitado: Boolean = true
) {
    // Estado solo visual (menú abierto o cerrado): puede vivir dentro del componente
    var expandido by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            text = etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(bottom = 4.dp)
        )
        ExposedDropdownMenuBox(
            expanded = expandido,
            onExpandedChange = { if (habilitado) expandido = it }
        ) {
            OutlinedTextField(
                value = seleccionada ?: "",
                onValueChange = {},
                readOnly = true, // no se escribe: solo se elige de la lista
                placeholder = { Text(placeholder) },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandido) },
                isError = mensajeError != null,
                supportingText = if (mensajeError != null) {
                    { Text(mensajeError) }
                } else null,
                singleLine = true,
                enabled = habilitado,
                modifier = Modifier
                    .menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable)
                    .fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = expandido,
                onDismissRequest = { expandido = false }
            ) {
                opciones.forEach { opcion ->
                    DropdownMenuItem(
                        text = { Text(opcion) },
                        onClick = {
                            onSeleccionar(opcion)
                            expandido = false
                        }
                    )
                }
            }
        }
    }
}

@Preview(name = "Selector", showBackground = true)
@Composable
private fun VistaSelector() {
    ArtesaniasAleoTheme(darkTheme = false) {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                CampoSelector(
                    etiqueta = "Región",
                    opciones = listOf("Metropolitana", "Valparaíso", "Biobío"),
                    seleccionada = "Metropolitana",
                    onSeleccionar = {}
                )
                CampoSelector(
                    etiqueta = "Comuna",
                    opciones = emptyList(),
                    seleccionada = null,
                    onSeleccionar = {},
                    mensajeError = "Selecciona una comuna"
                )
            }
        }
    }
}