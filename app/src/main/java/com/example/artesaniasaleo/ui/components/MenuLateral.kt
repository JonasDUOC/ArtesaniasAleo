package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

// Una opción del menú: texto, ícono, qué hace al tocarla y un contador opcional
data class OpcionMenu(
    val texto: String,
    val icono: ImageVector,
    val onClick: () -> Unit,
    val contador: Int = 0   // 0 = sin número
)

// Ancho del panel. Material usa hasta 360 dp (casi toda la pantalla de un celular);
// 240 dp alcanza para la opción más larga ("Gestión de contenido") con su ícono
private val ANCHO_MENU = 240.dp

// Panel del menú lateral (se abre con el ícono de menú de la barra superior)
@Composable
fun MenuLateral(opciones: List<OpcionMenu>, modifier: Modifier = Modifier) {
    ModalDrawerSheet(
        modifier = modifier.width(ANCHO_MENU),
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        // Mismo espacio arriba y abajo (16 dp). verticalScroll: si las opciones del admin
        // no caben (pantalla chica o letra grande), el menú se desplaza en vez de cortarse
        Column(
            modifier = Modifier
                .verticalScroll(rememberScrollState())
                .padding(vertical = 16.dp)
        ) {
            opciones.forEach { opcion ->
                NavigationDrawerItem(
                    label = { Text(opcion.texto) },
                    icon = { Icon(opcion.icono, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                    badge = { if (opcion.contador > 0) Badge { Text("${opcion.contador}") } },
                    selected = false,
                    onClick = opcion.onClick,
                    modifier = Modifier.padding(horizontal = 12.dp)
                )
            }
        }
    }
}
