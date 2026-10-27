package com.example.artesaniasaleo.navigation

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.artesaniasaleo.model.RecuperarUiState
import com.example.artesaniasaleo.ui.components.MenuLateral
import com.example.artesaniasaleo.ui.components.OpcionMenu
import com.example.artesaniasaleo.ui.screens.carrito.PantallaCarrito
import com.example.artesaniasaleo.ui.screens.catalogo.PantallaCatalogo
import com.example.artesaniasaleo.ui.screens.categorias.PantallaCategoria
import com.example.artesaniasaleo.ui.screens.categorias.PantallaCategorias
import com.example.artesaniasaleo.ui.screens.detalle.PantallaDetalle
import com.example.artesaniasaleo.ui.screens.favoritos.PantallaFavoritos
import com.example.artesaniasaleo.ui.screens.login.PantallaLogin
import com.example.artesaniasaleo.ui.screens.pago.PantallaDatosPago
import com.example.artesaniasaleo.ui.screens.perfil.PantallaPerfil
import com.example.artesaniasaleo.ui.screens.recuperar.PantallaRecuperar
import com.example.artesaniasaleo.ui.screens.registro.PantallaRegistro
import com.example.artesaniasaleo.ui.screens.seguimiento.PantallaPedido
import com.example.artesaniasaleo.ui.screens.seguimiento.PantallaSeguimiento
import com.example.artesaniasaleo.ui.screens.terminos.PantallaTerminos
import kotlinx.coroutines.launch

// Mapa de la app: qué pantalla se muestra en cada ruta y cómo se pasa de una a otra
@Composable
fun AppNavegacion() {
    val navController = rememberNavController() // recuerda en qué pantalla estamos y el historial
    val estado = remember { EstadoDemo() }      // provisional, hasta tener los ViewModel
    val contexto = LocalContext.current
    val menu = rememberDrawerState(DrawerValue.Closed)
    val alcance = rememberCoroutineScope()      // abrir y cerrar el menú es una animación (corrutina)

    // Acciones que usan varias pantallas
    val aviso: (String) -> Unit = { Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show() }
    val abrirMenu: () -> Unit = { alcance.launch { menu.open() } }
    val volver: () -> Unit = { navController.popBackStack() }
    // Ícono de casa: vuelve al catálogo, que es la base del historial después del login
    val irAInicio: () -> Unit = { navController.popBackStack(Rutas.CATALOGO, inclusive = false) }
    // Opciones del menú: abren la pantalla dejando solo el catálogo debajo
    val irDesdeMenu: (String) -> Unit = { ruta ->
        alcance.launch { menu.close() }
        navController.navigate(ruta) {
            popUpTo(Rutas.CATALOGO)
            launchSingleTop = true // no abre dos veces la misma pantalla
        }
    }
    val finalizarCompra: () -> Unit = {
        val numero = estado.confirmarCompra()
        aviso("Compra confirmada — pedido #$numero")
        navController.navigate(Rutas.SEGUIMIENTO) { popUpTo(Rutas.CATALOGO) }
    }

    val opcionesMenu = listOf(
        OpcionMenu("Perfil", Icons.Filled.Person, { irDesdeMenu(Rutas.PERFIL) }),
        OpcionMenu("Carrito", Icons.Filled.ShoppingCart, { irDesdeMenu(Rutas.CARRITO) }, estado.carrito.values.sum()),
        OpcionMenu("Favoritos", Icons.Filled.FavoriteBorder, { irDesdeMenu(Rutas.FAVORITOS) }),
        OpcionMenu("Categorías", Icons.AutoMirrored.Filled.List, { irDesdeMenu(Rutas.CATEGORIAS) }),
        OpcionMenu("Seguimiento", Icons.Filled.LocationOn, { irDesdeMenu(Rutas.SEGUIMIENTO) }),
        OpcionMenu("Cerrar sesión", Icons.AutoMirrored.Filled.ExitToApp, {
            alcance.launch { menu.close() }
            estado.cerrarSesion()
            // Borra todo el historial: "atrás" ya no vuelve a pantallas de la sesión
            navController.navigate(Rutas.LOGIN) { popUpTo(navController.graph.id) { inclusive = true } }
        })
    )

    // El menú lateral envuelve a todas las pantallas; solo se abre con el ícono de menú
    ModalNavigationDrawer(
        drawerState = menu,
        gesturesEnabled = menu.isOpen,
        drawerContent = { MenuLateral(opcionesMenu) }
    ) {
        NavHost(navController = navController, startDestination = Rutas.LOGIN) {

            composable(Rutas.LOGIN) {
                PantallaLogin(
                    estado = estado.login,
                    onCorreoChange = { estado.login = estado.login.copy(correo = it) },
                    onContrasenaChange = { estado.login = estado.login.copy(contrasena = it) },
                    onIniciarSesion = {
                        if (estado.validarLogin()) {
                            // popUpTo: saca el login del historial para que "atrás" no vuelva a él
                            navController.navigate(Rutas.CATALOGO) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                        }
                    },
                    onCrearCuenta = { navController.navigate(Rutas.REGISTRO) },
                    onOlvideContrasena = { navController.navigate(Rutas.RECUPERAR) }
                )
            }

            composable(Rutas.REGISTRO) {
                PantallaRegistro(
                    estado = estado.registro,
                    onEstadoChange = { estado.registro = it },
                    onContinuar = {
                        val paso = estado.registro.pasoActual
                        if (estado.validarEtapaRegistro()) {
                            if (paso < 3) {
                                if (paso == 1) aviso("Código enviado a tu correo")
                                estado.registro = estado.registro.copy(pasoActual = paso + 1)
                            } else {
                                estado.finalizarRegistro()
                                aviso("Cuenta creada con éxito")
                                navController.navigate(Rutas.CATALOGO) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                            }
                        }
                    },
                    onVolver = {
                        // En la etapa 2 o 3 retrocede una etapa; en la 1 vuelve al login
                        val paso = estado.registro.pasoActual
                        if (paso > 1) estado.registro = estado.registro.copy(pasoActual = paso - 1) else volver()
                    },
                    onVerTerminos = { navController.navigate(Rutas.TERMINOS) }
                )
            }

            composable(Rutas.TERMINOS) { PantallaTerminos(onVolver = volver) }

            composable(Rutas.RECUPERAR) {
                PantallaRecuperar(
                    estado = estado.recuperar,
                    onCorreoChange = { estado.recuperar = estado.recuperar.copy(correo = it) },
                    onEnviar = {
                        if (estado.validarRecuperar()) {
                            aviso("Código enviado a tu correo")
                            estado.recuperar = RecuperarUiState()
                            volver()
                        }
                    },
                    onVolver = volver
                )
            }

            composable(Rutas.CATALOGO) {
                PantallaCatalogo(
                    estado = estado.catalogo,
                    onBusquedaChange = { estado.catalogo = estado.catalogo.copy(busqueda = it) },
                    onProductoClick = { navController.navigate(Rutas.detalle(it)) },
                    onFavoritoClick = estado::alternarFavorito,
                    onInicio = {},        // ya estamos en Inicio
                    onMenu = abrirMenu
                )
            }

            // El id viaja dentro de la ruta ("detalle/3") y se lee aquí como número
            composable(Rutas.DETALLE, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                val producto = estado.producto(id) ?: return@composable
                PantallaDetalle(
                    producto = producto,
                    onFavoritoClick = { estado.alternarFavorito(id) },
                    onAgregarCarrito = {
                        estado.agregarAlCarrito(id)
                        aviso("Agregado al carrito")
                    },
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.CATEGORIAS) {
                PantallaCategorias(
                    categorias = estado.categorias,
                    onCategoriaClick = { navController.navigate(Rutas.categoria(it)) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.CATEGORIA, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                PantallaCategoria(
                    nombre = estado.categorias.find { it.id == id }?.nombre ?: "Categoría",
                    productos = estado.catalogo.productos.filter { it.categoriaId == id },
                    onProductoClick = { navController.navigate(Rutas.detalle(it)) },
                    onFavoritoClick = estado::alternarFavorito,
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.FAVORITOS) {
                PantallaFavoritos(
                    favoritos = estado.favoritos,
                    onProductoClick = { navController.navigate(Rutas.detalle(it)) },
                    onFavoritoClick = estado::alternarFavorito,
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.CARRITO) {
                PantallaCarrito(
                    items = estado.itemsCarrito,
                    onCambiarCantidad = estado::cambiarCantidad,
                    onConfirmarCompra = {
                        // Sin tarjeta guardada, primero se piden los datos de pago
                        if (estado.perfil.pago.tieneTarjeta) finalizarCompra()
                        else navController.navigate(Rutas.DATOS_PAGO)
                    },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.DATOS_PAGO) {
                PantallaDatosPago(
                    pago = estado.pagoCompra,
                    onPagoChange = { estado.pagoCompra = it },
                    onGuardarYConfirmar = { if (estado.guardarPagoCompra()) finalizarCompra() },
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.PERFIL) {
                PantallaPerfil(
                    estado = estado.perfil,
                    onEstadoChange = { estado.perfil = it },
                    onEditar = { estado.perfil = estado.perfil.copy(seccionEditando = it) },
                    onGuardar = {
                        if (estado.guardarPerfil(it)) aviso("Datos guardados con éxito")
                        else aviso("Error al guardar: revisa los campos")
                    },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.SEGUIMIENTO) {
                PantallaSeguimiento(
                    pedidos = estado.misPedidos,
                    onPedidoClick = { navController.navigate(Rutas.pedido(it)) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.PEDIDO, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                val pedido = estado.pedidos.find { it.id == id } ?: return@composable
                PantallaPedido(pedido = pedido, onVolver = volver, onMenu = abrirMenu)
            }
        }
    }
}
