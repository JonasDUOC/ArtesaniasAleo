package com.example.artesaniasaleo.ui.screens.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.PERFIL_DEMO
import com.example.artesaniasaleo.model.PerfilUiState
import com.example.artesaniasaleo.model.REGIONES
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.BotonSecundario
import com.example.artesaniasaleo.ui.components.CampoSelector
import com.example.artesaniasaleo.ui.components.CampoTexto
import com.example.artesaniasaleo.ui.components.FormularioPago
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

private val PESTANAS = listOf("Personal", "Dirección", "Pago")

@OptIn(ExperimentalMaterial3Api::class) // PrimaryTabRow puede estar marcado como experimental
@Composable
fun PantallaPerfil(
    estado: PerfilUiState,
    onEstadoChange: (PerfilUiState) -> Unit,
    onEditar: (Int) -> Unit,    // recibe la pestaña (0, 1 o 2)
    onGuardar: (Int) -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pestaña visible: es solo visual, por eso vive en la pantalla (sobrevive a la rotación)
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    val editando = estado.seccionEditando == pestana

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Mi perfil", esPantallaRaiz = true, onIconoIzquierdo = onInicio, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
        ) {
            PrimaryTabRow(selectedTabIndex = pestana, containerColor = MaterialTheme.colorScheme.background) {
                PESTANAS.forEachIndexed { indice, titulo ->
                    Tab(selected = pestana == indice, onClick = { pestana = indice }, text = { Text(titulo) })
                }
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                when (pestana) {
                    0 -> SeccionPersonal(estado, onEstadoChange, editando)
                    1 -> SeccionDireccion(estado, onEstadoChange, editando)
                    else -> FormularioPago(
                        pago = estado.pago,
                        onPagoChange = { onEstadoChange(estado.copy(pago = it)) },
                        habilitado = editando
                    )
                }
                Spacer(Modifier.height(8.dp))
                // "Editar" habilita los campos; "Guardar cambios" valida y vuelve a bloquearlos
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    BotonSecundario(texto = "Editar", onClick = { onEditar(pestana) }, modifier = Modifier.weight(1f))
                    BotonPrincipal(
                        texto = "Guardar cambios",
                        onClick = { onGuardar(pestana) },
                        habilitado = editando,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
private fun SeccionPersonal(estado: PerfilUiState, onEstadoChange: (PerfilUiState) -> Unit, editando: Boolean) {
    // RUT, nombres y apellido identifican a la persona: no se editan
    CampoTexto(etiqueta = "RUT", valor = estado.rut, onValorChange = {}, habilitado = false)
    CampoTexto(etiqueta = "Nombres", valor = estado.nombres, onValorChange = {}, habilitado = false)
    CampoTexto(etiqueta = "Apellido", valor = estado.apellido, onValorChange = {}, habilitado = false)
    CampoTexto(
        etiqueta = "Teléfono",
        valor = estado.telefono,
        onValorChange = { onEstadoChange(estado.copy(telefono = it)) },
        placeholder = "+56 9 1234 5678",
        mensajeError = estado.errorTelefono,
        tipoTeclado = KeyboardType.Phone,
        habilitado = editando
    )
    CampoTexto(
        etiqueta = "Correo electrónico",
        valor = estado.correo,
        onValorChange = { onEstadoChange(estado.copy(correo = it)) },
        mensajeError = estado.errorCorreo,
        tipoTeclado = KeyboardType.Email,
        habilitado = editando
    )
}

@Composable
private fun SeccionDireccion(estado: PerfilUiState, onEstadoChange: (PerfilUiState) -> Unit, editando: Boolean) {
    CampoSelector(
        etiqueta = "Región",
        opciones = REGIONES.keys.toList(),
        seleccionada = estado.region,
        onSeleccionar = { onEstadoChange(estado.copy(region = it, comuna = null)) }, // la comuna depende de la región
        mensajeError = estado.errorRegion,
        habilitado = editando
    )
    CampoSelector(
        etiqueta = "Comuna",
        opciones = REGIONES[estado.region].orEmpty(),
        seleccionada = estado.comuna,
        onSeleccionar = { onEstadoChange(estado.copy(comuna = it)) },
        mensajeError = estado.errorComuna,
        habilitado = editando && estado.region != null
    )
    CampoTexto(
        etiqueta = "Calle",
        valor = estado.calle,
        onValorChange = { onEstadoChange(estado.copy(calle = it)) },
        mensajeError = estado.errorCalle,
        habilitado = editando
    )
    CampoTexto(
        etiqueta = "Número",
        valor = estado.numero,
        onValorChange = { onEstadoChange(estado.copy(numero = it)) },
        mensajeError = estado.errorNumero,
        tipoTeclado = KeyboardType.Number,
        habilitado = editando
    )
    CampoTexto(
        etiqueta = "Depto / número interior (opcional)",
        valor = estado.departamento,
        onValorChange = { onEstadoChange(estado.copy(departamento = it)) },
        placeholder = "Depto 4B, casa 12, etc.",
        habilitado = editando
    )
}

@Preview(name = "Perfil", showBackground = true, heightDp = 900)
@Composable
private fun VistaPerfil() {
    ArtesaniasAleoTheme {
        PantallaPerfil(estado = PERFIL_DEMO, onEstadoChange = {}, onEditar = {}, onGuardar = {}, onInicio = {}, onMenu = {})
    }
}
