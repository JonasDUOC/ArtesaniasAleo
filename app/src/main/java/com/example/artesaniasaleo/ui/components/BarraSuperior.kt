package com.example.artesaniasaleo.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Barra superior del mockup: ícono izquierdo (Inicio o Volver), título centrado y menú
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    esPantallaRaiz: Boolean,        // true: muestra Inicio; false: muestra Volver
    onIconoIzquierdo: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier,
    mostrarMenu: Boolean = true
) {
    Column(modifier = modifier) {
        CenterAlignedTopAppBar(
            title = { Text(titulo, style = MaterialTheme.typography.titleMedium) },
            navigationIcon = {
                IconoCircular(
                    icono = if (esPantallaRaiz) Icons.Filled.Home else Icons.AutoMirrored.Filled.ArrowBack,
                    descripcion = if (esPantallaRaiz) "Inicio" else "Volver",
                    onClick = onIconoIzquierdo
                )
            },
            actions = {
                if (mostrarMenu) {
                    IconoCircular(icono = Icons.Filled.Menu, descripcion = "Menú", onClick = onMenu)
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        )
        // Línea inferior de la barra (border-bottom del mockup)
        HorizontalDivider(color = MaterialTheme.colorScheme.outline)
    }
}

// Ícono dentro de un círculo color chip, como en el mockup
@Composable
private fun IconoCircular(icono: ImageVector, descripcion: String, onClick: () -> Unit) {
    IconButton(
        onClick = onClick,
        colors = IconButtonDefaults.iconButtonColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Icon(imageVector = icono, contentDescription = descripcion)
    }
}

@Preview(name = "Barra - inicio", showBackground = true)
@Composable
private fun VistaBarraInicio() {
    ArtesaniasAleoTheme(darkTheme = false) {
        BarraSuperior(titulo = "Inicio", esPantallaRaiz = true, onIconoIzquierdo = {}, onMenu = {})
    }
}

@Preview(name = "Barra - detalle", showBackground = true)
@Composable
private fun VistaBarraDetalle() {
    ArtesaniasAleoTheme(darkTheme = false) {
        BarraSuperior(titulo = "Detalle", esPantallaRaiz = false, onIconoIzquierdo = {}, onMenu = {})
    }
}