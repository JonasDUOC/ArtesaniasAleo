package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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

// Panel del menú lateral (se abre con el ícono de menú de la barra superior)
@Composable
fun MenuLateral(opciones: List<OpcionMenu>, modifier: Modifier = Modifier) {
    ModalDrawerSheet(
        modifier = modifier,
        drawerContainerColor = MaterialTheme.colorScheme.surface
    ) {
        Spacer(Modifier.height(16.dp))
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
