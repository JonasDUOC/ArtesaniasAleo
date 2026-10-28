package com.example.artesaniasaleo.navigation

import android.widget.Toast
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
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
import com.example.artesaniasaleo.model.EstadoCompra
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.EstadoSesion
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
import com.example.artesaniasaleo.viewmodel.AdminViewModel
import com.example.artesaniasaleo.viewmodel.AppViewModelFactory
import com.example.artesaniasaleo.viewmodel.CarritoViewModel
import com.example.artesaniasaleo.viewmodel.CatalogoViewModel
import com.example.artesaniasaleo.viewmodel.LoginViewModel
import com.example.artesaniasaleo.viewmodel.PedidosViewModel
import com.example.artesaniasaleo.viewmodel.PerfilViewModel
import com.example.artesaniasaleo.viewmodel.RecuperarViewModel
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

    // ---------- ViewModels compartidos por varias pantallas (fuera del NavHost, Guía 11) ----------
    val catalogoViewModel: CatalogoViewModel = viewModel(factory = fabrica)
    val catalogo by catalogoViewModel.uiState.collectAsState()
    val busqueda by catalogoViewModel.busqueda.collectAsState()
    val cargaCatalogo by catalogoViewModel.carga.collectAsState()

    val carritoViewModel: CarritoViewModel = viewModel(factory = fabrica)
    val itemsCarrito by carritoViewModel.items.collectAsState()
    val cantidadCarrito by carritoViewModel.cantidadTotal.collectAsState()
    val pagoCompra by carritoViewModel.pago.collectAsState()
    val compra by carritoViewModel.compra.collectAsState()

    val pedidosViewModel: PedidosViewModel = viewModel(factory = fabrica)
    val misPedidos by pedidosViewModel.misPedidos.collectAsState()
    val todosLosPedidos by pedidosViewModel.todos.collectAsState()
    val pedidosPendientes by pedidosViewModel.pendientes.collectAsState()
    val ventasHoy by pedidosViewModel.ventasHoy.collectAsState()

    val adminViewModel: AdminViewModel = viewModel(factory = fabrica)
    val categoriasAdmin by adminViewModel.categorias.collectAsState()
    val productosAdmin by adminViewModel.productos.collectAsState()
    val usuarios by adminViewModel.usuarios.collectAsState()
    val mensajeAdmin by adminViewModel.mensaje.collectAsState()

    val contexto = LocalContext.current
    val menu = rememberDrawerState(DrawerValue.Closed)
    val alcance = rememberCoroutineScope()      // abrir y cerrar el menú es una animación (corrutina)

    // Acciones que usan varias pantallas
    val aviso: (String) -> Unit = { Toast.makeText(contexto, it, Toast.LENGTH_SHORT).show() }
    val abrirMenu: () -> Unit = { alcance.launch { menu.open() } }
    val volver: () -> Unit = { navController.popBackStack() }
    // El rol se lee de Room a través de SesionViewModel
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

    // CU-24: resultado de confirmar la compra (desde el carrito o desde "Datos de pago")
    LaunchedEffect(compra) {
        when (val resultado = compra) {
            is EstadoCompra.Confirmada -> {
                aviso("Compra confirmada — pedido #${resultado.pedidoId}")
                navController.navigate(Rutas.SEGUIMIENTO) { popUpTo(inicio) }
                carritoViewModel.compraMostrada()
            }
            // Sin tarjeta guardada: primero se piden los datos de pago y luego se retoma la compra
            EstadoCompra.SinTarjeta -> {
                navController.navigate(Rutas.DATOS_PAGO)
                carritoViewModel.compraMostrada()
            }
            is EstadoCompra.Error -> {
                aviso(resultado.mensaje)
                carritoViewModel.compraMostrada()
            }
            else -> Unit
        }
    }

    // Mensajes del panel de administración (eliminar, bloquear, guardar...)
    LaunchedEffect(mensajeAdmin) {
        mensajeAdmin?.let {
            aviso(it)
            adminViewModel.mensajeMostrado()
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

    // CU-27: el administrador ve lo mismo que el cliente + las opciones de administración
    val opcionesCliente = listOf(
        OpcionMenu("Perfil", Icons.Filled.Person, { irDesdeMenu(Rutas.PERFIL) }),
        OpcionMenu("Carrito", Icons.Filled.ShoppingCart, { irDesdeMenu(Rutas.CARRITO) }, cantidadCarrito),
        OpcionMenu("Favoritos", Icons.Filled.FavoriteBorder, { irDesdeMenu(Rutas.FAVORITOS) }),
        OpcionMenu("Categorías", Icons.AutoMirrored.Filled.List, { irDesdeMenu(Rutas.CATEGORIAS) }),
        OpcionMenu("Seguimiento", Icons.Filled.LocationOn, { irDesdeMenu(Rutas.SEGUIMIENTO) })
    )
    val opcionesAdmin = listOf(
        OpcionMenu("Resumen", Icons.Filled.DateRange, { irDesdeMenu(Rutas.RESUMEN) }),
        OpcionMenu("Notificaciones", Icons.Filled.Notifications, { irDesdeMenu(Rutas.NOTIFICACIONES) }, pedidosPendientes),
        OpcionMenu("Gestión de contenido", Icons.Filled.Edit, { irDesdeMenu(Rutas.GESTION) }),
        OpcionMenu("Control de usuarios", Icons.Filled.AccountCircle, { irDesdeMenu(Rutas.USUARIOS) }),
        OpcionMenu("Catálogo", Icons.Filled.Home, { irDesdeMenu(Rutas.CATALOGO) })
    )
    val opcionesMenu = (if (esAdmin) opcionesAdmin else emptyList()) + opcionesCliente + listOf(
        OpcionMenu("Cerrar sesión", Icons.AutoMirrored.Filled.ExitToApp, {
            alcance.launch { menu.close() }
            onCerrarSesion() // borra la sesión de DataStore → LaunchedEffect(sesion) lleva al login
        })
    )

    // El menú lateral envuelve a todas las pantallas; solo se abre con el ícono de menú
    ModalNavigationDrawer(
        drawerState = menu,
        gesturesEnabled = menu.isOpen,
        drawerContent = { MenuLateral(opcionesMenu) }
    ) {
        NavHost(navController = navController, startDestination = destinoInicial) {

            // ---------- Sin sesión ----------

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
                val recuperarViewModel: RecuperarViewModel = viewModel(factory = fabrica)
                val uiState by recuperarViewModel.uiState.collectAsState()
                val operacion by recuperarViewModel.operacion.collectAsState()
                LaunchedEffect(operacion) {
                    if (operacion is EstadoOperacion.Exito) {
                        aviso("Código enviado a tu correo")
                        recuperarViewModel.operacionMostrada()
                        volver()
                    }
                }
                PantallaRecuperar(
                    estado = uiState,
                    onCorreoChange = recuperarViewModel::onCorreoChange,
                    onEnviar = recuperarViewModel::enviar,
                    onVolver = volver
                )
            }

            // ---------- Cliente ----------

            composable(Rutas.CATALOGO) {
                PantallaCatalogo(
                    estado = catalogo.copy(busqueda = busqueda), // la búsqueda viene en su propio StateFlow
                    carga = cargaCatalogo,
                    onBusquedaChange = catalogoViewModel::onBusquedaChange,
                    onProductoClick = { navController.navigate(Rutas.detalle(it)) },
                    onFavoritoClick = catalogoViewModel::alternarFavorito,
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            // El id viaja dentro de la ruta ("detalle/3") y se lee aquí como número
            composable(Rutas.DETALLE, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                // Mientras Room no entrega la lista, todavía no hay producto que mostrar
                val producto = catalogo.producto(id) ?: return@composable
                PantallaDetalle(
                    producto = producto,
                    onFavoritoClick = { catalogoViewModel.alternarFavorito(id) },
                    onAgregarCarrito = {
                        carritoViewModel.agregar(id)
                        aviso("Agregado al carrito")
                    },
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.CATEGORIAS) {
                PantallaCategorias(
                    categorias = catalogo.categorias,
                    onCategoriaClick = { navController.navigate(Rutas.categoria(it)) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.CATEGORIA, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                PantallaCategoria(
                    nombre = catalogo.nombreCategoria(id),
                    productos = catalogo.productosDeCategoria(id), // ordenados por precio (CU-15)
                    onProductoClick = { navController.navigate(Rutas.detalle(it)) },
                    onFavoritoClick = catalogoViewModel::alternarFavorito,
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.FAVORITOS) {
                PantallaFavoritos(
                    favoritos = catalogo.favoritos,
                    onProductoClick = { navController.navigate(Rutas.detalle(it)) },
                    onFavoritoClick = catalogoViewModel::alternarFavorito,
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.CARRITO) {
                PantallaCarrito(
                    items = itemsCarrito,
                    onCambiarCantidad = carritoViewModel::cambiarCantidad,
                    // Si no hay tarjeta, el resultado SinTarjeta lleva a "Datos de pago" (LaunchedEffect(compra))
                    onConfirmarCompra = carritoViewModel::confirmarCompra,
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.DATOS_PAGO) {
                PantallaDatosPago(
                    pago = pagoCompra,
                    onPagoChange = carritoViewModel::onPagoChange,
                    onGuardarYConfirmar = carritoViewModel::guardarTarjetaYConfirmar,
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.PERFIL) {
                val perfilViewModel: PerfilViewModel = viewModel(factory = fabrica)
                val perfil by perfilViewModel.uiState.collectAsState()
                val operacion by perfilViewModel.operacion.collectAsState()
                // CU-09/10/11: mensaje "Guardado con éxito" o el error al guardar
                LaunchedEffect(operacion) {
                    when (val resultado = operacion) {
                        EstadoOperacion.Exito -> aviso("Datos guardados con éxito")
                        is EstadoOperacion.Error -> aviso(resultado.mensaje)
                        else -> return@LaunchedEffect
                    }
                    perfilViewModel.operacionMostrada()
                }
                PantallaPerfil(
                    estado = perfil,
                    onEstadoChange = perfilViewModel::onEstadoChange,
                    onEditar = perfilViewModel::editar,
                    onGuardar = perfilViewModel::guardar,
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.SEGUIMIENTO) {
                PantallaSeguimiento(
                    pedidos = misPedidos,
                    onPedidoClick = { navController.navigate(Rutas.pedido(it)) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.PEDIDO, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                val pedido = misPedidos.find { it.id == id } ?: return@composable
                PantallaPedido(pedido = pedido, onVolver = volver, onMenu = abrirMenu)
            }

            // ---------- Administrador ----------

            composable(Rutas.RESUMEN) {
                PantallaResumen(
                    pedidosPendientes = pedidosPendientes,
                    ventasHoy = ventasHoy,
                    onPendientes = { navController.navigate(Rutas.NOTIFICACIONES) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.NOTIFICACIONES) {
                PantallaNotificaciones(
                    pedidos = todosLosPedidos,
                    onPedidoClick = { navController.navigate(Rutas.pedidoAdmin(it)) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.PEDIDO_ADMIN, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                // Se busca en la lista observada: al cambiar el estado, Room avisa y esta pantalla se redibuja
                val pedido = todosLosPedidos.find { it.id == id } ?: return@composable
                PantallaPedidoAdmin(
                    pedido = pedido,
                    onCambiarEstado = { nuevo ->
                        pedidosViewModel.cambiarEstado(id, nuevo)
                        aviso("Pedido #$id → ${nuevo.etiqueta}")
                    },
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.GESTION) {
                PantallaGestionContenido(
                    categorias = categoriasAdmin,
                    productos = productosAdmin,
                    onEditarCategoria = {
                        adminViewModel.abrirFormCategoria(it)
                        navController.navigate(Rutas.FORM_CATEGORIA)
                    },
                    onEliminarCategoria = adminViewModel::eliminarCategoria,
                    onEditarProducto = {
                        adminViewModel.abrirFormProducto(it)
                        navController.navigate(Rutas.FORM_PRODUCTO)
                    },
                    onEliminarProducto = adminViewModel::eliminarProducto,
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.FORM_CATEGORIA) {
                val formulario by adminViewModel.formCategoria.collectAsState()
                val operacion by adminViewModel.operacion.collectAsState()
                // Guardado con éxito → vuelve a la gestión de contenido
                LaunchedEffect(operacion) {
                    if (operacion is EstadoOperacion.Exito) {
                        adminViewModel.operacionMostrada()
                        volver()
                    }
                }
                PantallaFormCategoria(
                    estado = formulario,
                    onNombreChange = adminViewModel::onNombreCategoriaChange,
                    onGuardar = adminViewModel::guardarCategoria,
                    onVolver = volver
                )
            }

            composable(Rutas.FORM_PRODUCTO) {
                val formulario by adminViewModel.formProducto.collectAsState()
                val operacion by adminViewModel.operacion.collectAsState()
                LaunchedEffect(operacion) {
                    if (operacion is EstadoOperacion.Exito) {
                        adminViewModel.operacionMostrada()
                        volver()
                    }
                }
                PantallaFormProducto(
                    estado = formulario,
                    categorias = categoriasAdmin,
                    onEstadoChange = adminViewModel::onFormProductoChange,
                    onGuardar = adminViewModel::guardarProducto,
                    onVolver = volver
                )
            }

            composable(Rutas.USUARIOS) {
                PantallaUsuarios(
                    usuarios = usuarios,
                    onEditar = { navController.navigate(Rutas.editarUsuario(it)) },
                    onInicio = irAInicio,
                    onMenu = abrirMenu
                )
            }

            composable(Rutas.EDITAR_USUARIO, arguments = listOf(navArgument("id") { type = NavType.IntType })) { entrada ->
                val id = entrada.arguments?.getInt("id") ?: 0
                val usuario = usuarios.find { it.id == id } ?: return@composable
                PantallaEditarUsuario(
                    usuario = usuario,
                    onAlternarBloqueo = { adminViewModel.alternarBloqueo(id) },
                    onHacerAdmin = { adminViewModel.hacerAdmin(id) },
                    onVolver = volver,
                    onMenu = abrirMenu
                )
            }
        }
    }
}
