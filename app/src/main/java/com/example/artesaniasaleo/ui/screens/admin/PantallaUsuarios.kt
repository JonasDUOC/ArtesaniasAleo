package com.example.artesaniasaleo.ui.screens.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.USUARIOS_DEMO
import com.example.artesaniasaleo.model.UsuarioUi
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonSecundario
import com.example.artesaniasaleo.ui.components.Etiqueta
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Lista de usuarios con su rol y estado
@Composable
fun PantallaUsuarios(
    usuarios: List<UsuarioUi>,
    onEditar: (Int) -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Control de usuarios", esPantallaRaiz = true, onIconoIzquierdo = onInicio, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(horizontal = 16.dp)
        ) {
            usuarios.forEach { usuario ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(usuario.nombre, style = MaterialTheme.typography.bodyMedium)
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp), modifier = Modifier.padding(top = 4.dp)) {
                            Etiqueta(if (usuario.esAdmin) "Admin" else "Cliente")
                            Etiqueta(if (usuario.bloqueado) "Bloqueado" else "Activo")
                        }
                    }
                    IconButton(onClick = { onEditar(usuario.id) }) {
                        Icon(Icons.Filled.Edit, contentDescription = "Editar")
                    }
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            }
        }
    }
}

// Acciones sobre un usuario; cada una pide confirmación antes de aplicarse
@Composable
fun PantallaEditarUsuario(
    usuario: UsuarioUi,
    onAlternarBloqueo: () -> Unit,
    onHacerAdmin: () -> Unit,
    onVolver: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Qué acción espera confirmación ("bloqueo" o "admin"); null = ningún diálogo abierto
    var accionPendiente by remember { mutableStateOf<String?>(null) }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Editar usuario", esPantallaRaiz = false, onIconoIzquierdo = onVolver, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(16.dp)
        ) {
            Text(usuario.nombre, style = MaterialTheme.typography.titleMedium)
            Text(
                (if (usuario.esAdmin) "Administrador" else "Cliente") + " · " + (if (usuario.bloqueado) "Bloqueado" else "Activo"),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(16.dp))
            // Bloquear va en rojo porque impide el acceso; habilitar usa el color principal
            Button(
                onClick = { accionPendiente = "bloqueo" },
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = if (usuario.bloqueado) ButtonDefaults.buttonColors()
                else ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(if (usuario.bloqueado) "Habilitar cuenta" else "Bloquear cuenta")
            }
            if (!usuario.esAdmin) {
                Spacer(Modifier.height(8.dp))
                BotonSecundario(texto = "Hacer administrador", onClick = { accionPendiente = "admin" })
            }
        }
    }

    // Diálogo de confirmación (el confirm() del mockup)
    accionPendiente?.let { accion ->
        val mensaje = when {
            accion == "admin" -> "¿Confirmas otorgar rol de administrador a ${usuario.nombre}?"
            usuario.bloqueado -> "¿Confirmas habilitar la cuenta de ${usuario.nombre}?"
            else -> "¿Confirmas bloquear la cuenta de ${usuario.nombre}?"
        }
        AlertDialog(
            onDismissRequest = { accionPendiente = null },
            text = { Text(mensaje) },
            confirmButton = {
                TextButton(onClick = {
                    if (accion == "admin") onHacerAdmin() else onAlternarBloqueo()
                    accionPendiente = null
                }) { Text("Confirmar") }
            },
            dismissButton = { TextButton(onClick = { accionPendiente = null }) { Text("Cancelar") } }
        )
    }
}

@Preview(name = "Usuarios", showBackground = true)
@Composable
private fun VistaUsuarios() {
    ArtesaniasAleoTheme {
        PantallaUsuarios(usuarios = USUARIOS_DEMO, onEditar = {}, onInicio = {}, onMenu = {})
    }
}

@Preview(name = "Editar usuario", showBackground = true)
@Composable
private fun VistaEditarUsuario() {
    ArtesaniasAleoTheme {
        PantallaEditarUsuario(usuario = USUARIOS_DEMO[0], onAlternarBloqueo = {}, onHacerAdmin = {}, onVolver = {}, onMenu = {})
    }
}
