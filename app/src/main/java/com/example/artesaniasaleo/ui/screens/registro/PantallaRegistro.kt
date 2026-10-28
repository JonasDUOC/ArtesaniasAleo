package com.example.artesaniasaleo.ui.screens.registro

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.RegistroUiState
import com.example.artesaniasaleo.model.REGIONES
import com.example.artesaniasaleo.ui.components.CampoSelector
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.CampoTexto
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

@Composable
fun PantallaRegistro(
    estado: RegistroUiState,
    operacion: EstadoOperacion,                // Inactivo / Cargando / Exito / Error, viene del RegistroViewModel
    onEstadoChange: (RegistroUiState) -> Unit, // recibe el estado nuevo creado con copy()
    onContinuar: () -> Unit,                   // el ViewModel valida y avanza de etapa
    onVolver: () -> Unit,
    onVerTerminos: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cargando = operacion is EstadoOperacion.Cargando || operacion is EstadoOperacion.Exito
    val mensajeError = (operacion as? EstadoOperacion.Error)?.mensaje

    // Título y texto del botón cambian según la etapa (igual que en el mockup)
    val titulo = when (estado.pasoActual) {
        1 -> "Crear cuenta"
        2 -> "Verificar código"
        else -> "Completar datos"
    }
    val textoBoton = when (estado.pasoActual) {
        1 -> "Registrarse"
        2 -> "Verificar código"
        else -> "Finalizar registro"
    }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = titulo,
                esPantallaRaiz = false, // muestra la flecha para volver
                onIconoIzquierdo = onVolver,
                onMenu = {},
                mostrarMenu = false     // las pantallas de registro no tienen menú
            )
        }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra) // evita que el contenido quede bajo la barra
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            when (estado.pasoActual) {
                1 -> PasoCuenta(estado, onEstadoChange, onVerTerminos)
                2 -> PasoCodigo(estado, onEstadoChange)
                else -> PasoDatos(estado, onEstadoChange)
            }

            // Error que no es de un campo (ej.: no se pudo guardar en la base de datos)
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
                texto = if (cargando) "Procesando..." else textoBoton,
                onClick = onContinuar,
                habilitado = estado.puedeContinuar && !cargando
            )
        }
    }
}

// Etapa 1: correo, contraseña, confirmación y términos
@Composable
private fun PasoCuenta(
    estado: RegistroUiState,
    onEstadoChange: (RegistroUiState) -> Unit,
    onVerTerminos: () -> Unit
) {
    CampoTexto(
        etiqueta = "Correo",
        valor = estado.correo,
        onValorChange = { onEstadoChange(estado.copy(correo = it)) },
        placeholder = "tucorreo@mail.com",
        mensajeError = estado.errorCorreo,
        tipoTeclado = KeyboardType.Email
    )

    Spacer(modifier = Modifier.height(12.dp))

    CampoTexto(
        etiqueta = "Contraseña",
        valor = estado.contrasena,
        onValorChange = { onEstadoChange(estado.copy(contrasena = it)) },
        placeholder = "mín. 6 caracteres",
        mensajeError = estado.errorContrasena,
        esContrasena = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    CampoTexto(
        etiqueta = "Confirmar contraseña",
        valor = estado.confirmarContrasena,
        onValorChange = { onEstadoChange(estado.copy(confirmarContrasena = it)) },
        mensajeError = estado.errorConfirmar,
        esContrasena = true
    )

    Spacer(modifier = Modifier.height(12.dp))

    Row(verticalAlignment = Alignment.CenterVertically) {
        Checkbox(
            checked = estado.aceptaTerminos,
            onCheckedChange = { onEstadoChange(estado.copy(aceptaTerminos = it)) }
        )
        Text(
            text = "Acepto los términos de política y privacidad",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Text(
        text = "Ver términos de política y privacidad",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.primary,
        textDecoration = TextDecoration.Underline,
        modifier = Modifier
            .clickable(onClick = onVerTerminos)
            .padding(start = 12.dp, top = 4.dp, bottom = 4.dp)
    )

    if (estado.errorTerminos != null) {
        Text(
            text = estado.errorTerminos,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}

// Etapa 2: código de verificación de 4 dígitos
@Composable
private fun PasoCodigo(
    estado: RegistroUiState,
    onEstadoChange: (RegistroUiState) -> Unit
) {
    Text(
        text = "Enviamos un código de verificación a ${estado.correo}.",
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )

    Spacer(modifier = Modifier.height(12.dp))

    CampoTexto(
        etiqueta = "Código de verificación",
        valor = estado.codigo,
        onValorChange = { nuevo ->
            // Solo números y máximo 4 dígitos
            if (nuevo.length <= 4 && nuevo.all { it.isDigit() }) {
                onEstadoChange(estado.copy(codigo = nuevo))
            }
        },
        placeholder = "4 dígitos",
        mensajeError = estado.errorCodigo,
        tipoTeclado = KeyboardType.Number
    )

    Spacer(modifier = Modifier.height(8.dp))

    Text(
        text = "Demo: el código enviado es 1234.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

// Etapa 3: datos personales y dirección
@Composable
private fun PasoDatos(
    estado: RegistroUiState,
    onEstadoChange: (RegistroUiState) -> Unit
) {
    CampoTexto(
        etiqueta = "RUT",
        valor = estado.rut,
        onValorChange = { onEstadoChange(estado.copy(rut = it)) },
        placeholder = "12.345.678-9",
        mensajeError = estado.errorRut
    )
    Spacer(modifier = Modifier.height(12.dp))
    CampoTexto(
        etiqueta = "Nombres",
        valor = estado.nombres,
        onValorChange = { onEstadoChange(estado.copy(nombres = it)) },
        mensajeError = estado.errorNombres
    )
    Spacer(modifier = Modifier.height(12.dp))
    CampoTexto(
        etiqueta = "Apellido",
        valor = estado.apellido,
        onValorChange = { onEstadoChange(estado.copy(apellido = it)) },
        mensajeError = estado.errorApellido
    )
    Spacer(modifier = Modifier.height(12.dp))
    CampoSelector(
        etiqueta = "Región",
        opciones = REGIONES.keys.toList(),
        seleccionada = estado.region,
        // Al cambiar la región se borra la comuna, porque pertenecía a otra región
        onSeleccionar = { onEstadoChange(estado.copy(region = it, comuna = null)) },
        mensajeError = estado.errorRegion
    )
    Spacer(modifier = Modifier.height(12.dp))
    CampoSelector(
        etiqueta = "Comuna",
        opciones = REGIONES[estado.region].orEmpty(), // solo las comunas de la región elegida
        seleccionada = estado.comuna,
        onSeleccionar = { onEstadoChange(estado.copy(comuna = it)) },
        placeholder = if (estado.region == null) "Primero elige una región" else "Selecciona una comuna",
        mensajeError = estado.errorComuna,
        habilitado = estado.region != null
    )
    Spacer(modifier = Modifier.height(12.dp))
    CampoTexto(
        etiqueta = "Calle",
        valor = estado.calle,
        onValorChange = { onEstadoChange(estado.copy(calle = it)) },
        placeholder = "Av. Siempre Viva",
        mensajeError = estado.errorCalle
    )
    Spacer(modifier = Modifier.height(12.dp))
    CampoTexto(
        etiqueta = "Número",
        valor = estado.numero,
        onValorChange = { onEstadoChange(estado.copy(numero = it)) },
        placeholder = "742",
        mensajeError = estado.errorNumero
    )
    Spacer(modifier = Modifier.height(12.dp))
    CampoTexto(
        etiqueta = "Depto / número interior (opcional)",
        valor = estado.departamento,
        onValorChange = { onEstadoChange(estado.copy(departamento = it)) },
        placeholder = "Depto 4B, casa 12, etc."
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "Podrás agregar tus datos de pago más tarde desde tu perfil.",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Preview(name = "Registro etapa 1", showBackground = true)
@Composable
private fun VistaRegistroEtapa1() {
    ArtesaniasAleoTheme {
        PantallaRegistro(
            estado = RegistroUiState(
                correo = "maxi@mail.com",
                contrasena = "123",
                errorContrasena = "Debe tener al menos 6 caracteres",
                errorTerminos = "Debes aceptar los términos para continuar"
            ),
            operacion = EstadoOperacion.Inactivo,
            onEstadoChange = {},
            onContinuar = {},
            onVolver = {},
            onVerTerminos = {}
        )
    }
}

@Preview(name = "Registro etapa 2", showBackground = true)
@Composable
private fun VistaRegistroEtapa2() {
    ArtesaniasAleoTheme {
        PantallaRegistro(
            estado = RegistroUiState(pasoActual = 2, correo = "maxi@mail.com", codigo = "12"),
            operacion = EstadoOperacion.Inactivo,
            onEstadoChange = {}, onContinuar = {}, onVolver = {}, onVerTerminos = {}
        )
    }
}

@Preview(name = "Registro etapa 3", showBackground = true, heightDp = 1100)
@Composable
private fun VistaRegistroEtapa3() {
    ArtesaniasAleoTheme {
        PantallaRegistro(
            estado = RegistroUiState(pasoActual = 3, region = "Región del Biobío", errorRut = "RUT inválido (verifica el dígito verificador)"),
            operacion = EstadoOperacion.Inactivo,
            onEstadoChange = {}, onContinuar = {}, onVolver = {}, onVerTerminos = {}
        )
    }
}