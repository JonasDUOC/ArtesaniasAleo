package com.example.artesaniasaleo.navigation

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.EstadoSesion
import com.example.artesaniasaleo.model.RecuperarUiState
import com.example.artesaniasaleo.ui.components.MenuLateral
import com.example.artesaniasaleo.ui.components.OpcionMenu
import com.example.artesaniasaleo.ui.components.PantallaCarga
import com.example.artesaniasaleo.ui.screens.admin.PantallaEditarUsuario
import com.example.artesaniasaleo.ui.screens.admin.PantallaFormCategoria
import com.example.artesaniasaleo.ui.screens.admin.PantallaFormProducto
import com.example.artesaniasaleo.ui.screens.admin.PantallaGestionContenido
import com.example.artesaniasaleo.ui.screens.admin.PantallaNotificaciones
import com.example.artesaniasaleo.ui.screens.admin.PantallaPedidoAdmin
import com.example.artesaniasaleo.ui.screens.admin.PantallaResumen
import com.example.artesaniasaleo.ui.screens.admin.PantallaUsuarios
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
import com.example.artesaniasaleo.viewmodel.AppViewModelFactory
import com.example.artesaniasaleo.viewmodel.LoginViewModel
import com.example.artesaniasaleo.viewmodel.RegistroViewModel
import com.example.artesaniasaleo.viewmodel.SesionViewModel
import kotlinx.coroutines.launch

// Punto de entrada de la navegación: primero averigua si hay sesión y luego muestra la app
@Composable
fun AppNavegacion() {
    val contexto = LocalContext.current
    // Una sola Factory para toda la app; remember evita crearla de nuevo en cada recomposición
    val fabrica = remember { AppViewModelFactory(contexto) }
    // Creado fuera del NavHost → vive mientras vive la Activity y lo comparten todas las pantallas
    val sesionViewModel: SesionViewModel = viewModel(factory = fabrica)
    val sesion by sesionViewModel.estado.collectAsState()

    if (sesion is EstadoSesion.Verificando) {
        PantallaCarga() // aún no se lee DataStore: no sabemos si mostrar el login o el catálogo
    } else {
        NavegacionPrincipal(
            fabrica = fabrica,
            sesion = sesion,
            onCerrarSesion = sesionViewModel::cerrarSesion
        )
    }
}

// Mapa de la app: qué pantalla se muestra en cada ruta y cómo se pasa de una a otra
@Composable
private fun NavegacionPrincipal(
    fabrica: AppViewModelFactory,
    sesion: EstadoSesion,
    onCerrarSesion: () -> Unit
) {
    val navController = rememberNavController() // recuerda en qué pantalla estamos y el historial
    val estado = remember { EstadoDemo() }      // provisional, hasta tener los ViewModel
    val contexto = LocalContext.current
    val menu = rememberDrawerState(DrawerValue.Closed)
    val alcance = rememberCoroutineScope()      // abrir y cerrar el menú es una animación (corrutina)

    // Acciones que usan varias pantallas
    val aviso: (String) -> Unit = { Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show() }
    val abrirMenu: () -> Unit = { alcance.launch { menu.open() } }
    val volver: () -> Unit = { navController.popBackStack() }
    // El rol se lee de Room a través de SesionViewModel (ya no del correo escrito)
    val esAdmin = (sesion as? EstadoSesion.Activa)?.esAdmin == true
    // Pantalla base después del login: catálogo para el cliente, resumen para el administrador
    val inicio = if (esAdmin) Rutas.RESUMEN else Rutas.CATALOGO
    // Pantalla con que abre la app; remember la fija la primera vez (si hay sesión guardada, entra directo)
    val destinoInicial = remember { if (sesion is EstadoSesion.Activa) inicio else Rutas.LOGIN }
    val irAlLogin: () -> Unit = {
        // Borra todo el historial: "atrás" ya no vuelve a pantallas de la sesión
        navController.navigate(Rutas.LOGIN) { popUpTo(navController.graph.id) { inclusive = true } }
    }

    // La sesión decide la pantalla: se ejecuta cada vez que cambia (login, cerrar sesión, bloqueo)
    LaunchedEffect(sesion) {
        val rutaActual = navController.currentDestination?.route ?: return@LaunchedEffect
        val enPantallaSinSesion = rutaActual in Rutas.SIN_SESION
        when {
            // Acaba de iniciar sesión (estaba en login/registro) → a su pantalla base
            sesion is EstadoSesion.Activa && enPantallaSinSesion ->
                navController.navigate(inicio) { popUpTo(navController.graph.id) { inclusive = true } }
            // Cerró sesión o lo bloquearon estando dentro → al login
            sesion is EstadoSesion.SinSesion && !enPantallaSinSesion -> irAlLogin()
        }
    }
    // Ícono de casa: vuelve a la pantalla base del historial
    val irAInicio: () -> Unit = { navController.popBackStack(inicio, inclusive = false) }
    // Opciones del menú: abren la pantalla dejando solo la pantalla base debajo
    val irDesdeMenu: (String) -> Unit = { ruta ->
        alcance.launch { menu.close() }
        navController.navigate(ruta) {
            popUpTo(inicio)
            launchSingleTop = true // no abre dos veces la misma pantalla
        }
    }
    val finalizarCompra: () -> Unit = {
        val numero = estado.confirmarCompra()
        aviso("Compra confirmada — pedido #$numero")
        navController.navigate(Rutas.SEGUIMIENTO) { popUpTo(Rutas.CATALOGO) }
    }

    // El menú cambia según el rol, como en el mockup
    val opcionesRol = if (esAdmin) listOf(
        OpcionMenu("Resumen", Icons.Filled.DateRange, { irDesdeMenu(Rutas.RESUMEN) }),
        OpcionMenu("Notificaciones", Icons.Filled.Notifications, { irDesdeMenu(Rutas.NOTIFICACIONES) }, estado.pedidosPendientes),
        OpcionMenu("Gestión de contenido", Icons.Filled.Edit, { irDesdeMenu(Rutas.GESTION) }),
        OpcionMenu("Control de usuarios", Icons.Filled.AccountCircle, { irDesdeMenu(Rutas.USUARIOS) })
    ) else listOf(
        OpcionMenu("Perfil", Icons.Filled.Person, { irDesdeMenu(Rutas.PERFIL) }),
        OpcionMenu("Carrito", Icons.Filled.ShoppingCart, { irDesdeMenu(Rutas.CARRITO) }, estado.carrito.values.sum()),
        OpcionMenu("Favoritos", Icons.Filled.FavoriteBorder, { irDesdeMenu(Rutas.FAVORITOS) }),
        OpcionMenu("Categorías", Icons.AutoMirrored.Filled.List, { irDesdeMenu(Rutas.CATEGORIAS) }),
        OpcionMenu("Seguimiento", Icons.Filled.LocationOn, { irDesdeMenu(Rutas.SEGUIMIENTO) })
    )
    val opcionesMenu = opcionesRol + listOf(
        OpcionMenu("Cerrar sesión", Icons.AutoMirrored.Filled.ExitToApp, {
            alcance.launch { menu.close() }
            estado.cerrarSesion()   // vacía el carrito de la demo
            onCerrarSesion()        // borra la sesión de DataStore → LaunchedEffect(sesion) lleva al login
        })
    )

    // El menú lateral envuelve a todas las pantallas; solo se abre con el ícono de menú
    ModalNavigationDrawer(
        drawerState = menu,
        gesturesEnabled = menu.isOpen,
        drawerContent = { MenuLateral(opcionesMenu) }
    ) {
        NavHost(navController = navController, startDestination = destinoInicial) {

            composable(Rutas.LOGIN) {
                // Creado dentro de composable(): vive mientras el login esté en el historial
                val loginViewModel: LoginViewModel = viewModel(factory = fabrica)
                val uiState by loginViewModel.uiState.collectAsState()
                val operacion by loginViewModel.operacion.collectAsState()
                PantallaLogin(
                    estado = uiState,
                    operacion = operacion,
                    onCorreoChange = loginViewModel::onCorreoChange,
                    onContrasenaChange = loginViewModel::onContrasenaChange,
                    // No navega aquí: al guardarse la sesión, LaunchedEffect(sesion) lleva a la pantalla base
                    onIniciarSesion = loginViewModel::iniciarSesion,
                    onCrearCuenta = { navController.navigate(Rutas.REGISTRO) },
                    onOlvideContrasena = { navController.navigate(Rutas.RECUPERAR) }
                )
            }

            composable(Rutas.REGISTRO) {
                // Vive mientras el registro esté en el historial: si vas a Términos y vuelves,
                // lo escrito se conserva (CU-04)
                val registroViewModel: RegistroViewModel = viewModel(factory = fabrica)
                val uiState by registroViewModel.uiState.collectAsState()
                val operacion by registroViewModel.operacion.collectAsState()
                // Mensaje al crear la cuenta; la navegación al catálogo la hace LaunchedEffect(sesion)
                LaunchedEffect(operacion) {
                    if (operacion is EstadoOperacion.Exito) aviso("Cuenta creada con éxito")
                }
                PantallaRegistro(
                    estado = uiState,
                    operacion = operacion,
                    onEstadoChange = registroViewModel::onEstadoChange,
                    onContinuar = registroViewModel::continuar,
                    // En la etapa 2 o 3 retrocede una etapa; en la 1 vuelve al login
                    onVolver = { if (!registroViewModel.retroceder()) volver() },
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

            // ---------- Administrador ----------

            composable(Rutas.RESUMEN) {
                PantallaResumen(
                    pedidosPendientes = estado.pedidosPendientes,
                    onPendientes = { navController.navigate(Rutas.NOTIFICACIONES) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.NOTIFICACIONES) {
                PantallaNotificaciones(
                    pedidos = estado.pedidos,
                    onPedidoClick = { navController.navigate(Rutas.pedidoAdmin(it)) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.PEDIDO_ADMIN, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                val pedido = estado.pedidos.find { it.id == id } ?: return@composable
                PantallaPedidoAdmin(
                    pedido = pedido,
                    onCambiarEstado = { nuevo ->
                        estado.cambiarEstadoPedido(id, nuevo)
                        aviso("Pedido #$id → ${nuevo.etiqueta}")
                    },
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.GESTION) {
                PantallaGestionContenido(
                    categorias = estado.categorias,
                    productos = estado.catalogo.productos,
                    onEditarCategoria = {
                        estado.abrirFormCategoria(it)
                        navController.navigate(Rutas.FORM_CATEGORIA)
                    },
                    onEliminarCategoria = { aviso(estado.eliminarCategoria(it)) },
                    onEditarProducto = {
                        estado.abrirFormProducto(it)
                        navController.navigate(Rutas.FORM_PRODUCTO)
                    },
                    onEliminarProducto = {
                        estado.eliminarProducto(it)
                        aviso("Producto eliminado")
                    },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.FORM_CATEGORIA) {
                PantallaFormCategoria(
                    estado = estado.formCategoria,
                    onNombreChange = { estado.formCategoria = estado.formCategoria.copy(nombre = it) },
                    onGuardar = {
                        if (estado.guardarCategoria()) {
                            aviso("Categoría guardada")
                            volver()
                        }
                    },
                    onVolver = volver
                )
            }

            composable(Rutas.FORM_PRODUCTO) {
                PantallaFormProducto(
                    estado = estado.formProducto,
                    categorias = estado.categorias,
                    onEstadoChange = { estado.formProducto = it },
                    onGuardar = {
                        if (estado.guardarProducto()) {
                            aviso("Producto guardado")
                            volver()
                        }
                    },
                    onVolver = volver
                )
            }

            composable(Rutas.USUARIOS) {
                PantallaUsuarios(
                    usuarios = estado.usuarios,
                    onEditar = { navController.navigate(Rutas.editarUsuario(it)) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.EDITAR_USUARIO, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                val usuario = estado.usuario(id) ?: return@composable
                PantallaEditarUsuario(
                    usuario = usuario,
                    onAlternarBloqueo = {
                        aviso(if (usuario.bloqueado) "Cuenta de ${usuario.nombre} habilitada" else "Cuenta de ${usuario.nombre} bloqueada")
                        estado.alternarBloqueo(id)
                    },
                    onHacerAdmin = {
                        estado.hacerAdmin(id)
                        aviso("${usuario.nombre} ahora es administrador")
                    },
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }
        }
    }
}
