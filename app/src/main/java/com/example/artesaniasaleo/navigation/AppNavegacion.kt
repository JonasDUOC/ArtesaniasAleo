package com.example.artesaniasaleo.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.artesaniasaleo.model.CatalogoUiState
import com.example.artesaniasaleo.model.LoginUiState
import com.example.artesaniasaleo.model.PRODUCTOS_DEMO
import com.example.artesaniasaleo.model.RegistroUiState
import com.example.artesaniasaleo.ui.screens.catalogo.PantallaCatalogo
import com.example.artesaniasaleo.ui.screens.login.PantallaLogin
import com.example.artesaniasaleo.ui.screens.registro.PantallaRegistro

// Mapa de la app: qué pantalla se muestra en cada ruta y cómo se pasa de una a otra
@Composable
fun AppNavegacion() {
    val navController = rememberNavController() // recuerda en qué pantalla estamos y el historial

    // Estados provisionales: se reemplazan por los ViewModel (con Jonas)
    var estadoLogin by remember { mutableStateOf(LoginUiState()) }
    var estadoRegistro by remember { mutableStateOf(RegistroUiState()) }
    var estadoCatalogo by remember { mutableStateOf(CatalogoUiState(productos = PRODUCTOS_DEMO)) }

    NavHost(navController = navController, startDestination = Rutas.LOGIN) {

        composable(Rutas.LOGIN) {
            PantallaLogin(
                estado = estadoLogin,
                onCorreoChange = { estadoLogin = estadoLogin.copy(correo = it) },
                onContrasenaChange = { estadoLogin = estadoLogin.copy(contrasena = it) },
                onIniciarSesion = {
                    // popUpTo: saca el login del historial para que "atrás" no vuelva a él
                    navController.navigate(Rutas.CATALOGO) {
                        popUpTo(Rutas.LOGIN) { inclusive = true }
                    }
                },
                onCrearCuenta = { navController.navigate(Rutas.REGISTRO) },
                onOlvideContrasena = {} // pendiente: pantalla de recuperar contraseña
            )
        }

        composable(Rutas.REGISTRO) {
            PantallaRegistro(
                estado = estadoRegistro,
                onEstadoChange = { estadoRegistro = it },
                onContinuar = {
                    if (estadoRegistro.pasoActual < 3) {
                        estadoRegistro = estadoRegistro.copy(pasoActual = estadoRegistro.pasoActual + 1)
                    } else {
                        navController.navigate(Rutas.CATALOGO) {
                            popUpTo(Rutas.LOGIN) { inclusive = true }
                        }
                    }
                },
                onVolver = {
                    // En la etapa 2 o 3 retrocede una etapa; en la 1 vuelve al login
                    if (estadoRegistro.pasoActual > 1) {
                        estadoRegistro = estadoRegistro.copy(pasoActual = estadoRegistro.pasoActual - 1)
                    } else {
                        navController.popBackStack()
                    }
                },
                onVerTerminos = {} // pendiente: pantalla de términos
            )
        }

        composable(Rutas.CATALOGO) {
            PantallaCatalogo(
                estado = estadoCatalogo,
                onBusquedaChange = { estadoCatalogo = estadoCatalogo.copy(busqueda = it) },
                onProductoClick = {}, // pendiente: pantalla de detalle
                onFavoritoClick = { id ->
                    // Invierte el favorito solo del producto tocado
                    estadoCatalogo = estadoCatalogo.copy(
                        productos = estadoCatalogo.productos.map {
                            if (it.id == id) it.copy(esFavorito = !it.esFavorito) else it
                        }
                    )
                },
                onInicio = {},        // ya estamos en Inicio
                onMenu = {}           // pendiente: menú lateral
            )
        }
    }
}
