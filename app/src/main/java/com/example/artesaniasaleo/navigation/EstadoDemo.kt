package com.example.artesaniasaleo.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.artesaniasaleo.model.CatalogoUiState
import com.example.artesaniasaleo.model.CategoriaFormUiState
import com.example.artesaniasaleo.model.CategoriaUi
import com.example.artesaniasaleo.model.DatosPagoUiState
import com.example.artesaniasaleo.model.EstadoPedido
import com.example.artesaniasaleo.model.LineaPedido
import com.example.artesaniasaleo.model.PEDIDOS_DEMO
import com.example.artesaniasaleo.model.PERFIL_DEMO
import com.example.artesaniasaleo.model.PedidoUi
import com.example.artesaniasaleo.model.ProductoFormUiState
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.model.RecuperarUiState
import com.example.artesaniasaleo.model.USUARIOS_DEMO
import com.example.artesaniasaleo.model.UsuarioUi
import com.example.artesaniasaleo.ui.utils.esCorreoValido

// PROVISIONAL: guarda los datos en memoria y valida, para que la app funcione completa.
// Cuando existan los ViewModel y el repository (con Jonas), cada parte pasa a su ViewModel
// y los datos se leen y guardan en Room. Las pantallas no cambian.
// Ya migrado: login y sesión → LoginViewModel y SesionViewModel; registro → RegistroViewModel;
// catálogo, búsqueda, detalle, favoritos y categorías → CatalogoViewModel.
// `catalogo` y `categorias` siguen aquí solo para el carrito y la gestión de contenido:
// AppNavegacion les copia los datos de Room (puente provisional).
class EstadoDemo {
    var recuperar by mutableStateOf(RecuperarUiState())
    var catalogo by mutableStateOf(CatalogoUiState())
    var perfil by mutableStateOf(PERFIL_DEMO)
    var pagoCompra by mutableStateOf(DatosPagoUiState())
    var carrito by mutableStateOf(mapOf<Int, Int>())   // id del producto -> cantidad
    var pedidos by mutableStateOf(PEDIDOS_DEMO)
    var categorias by mutableStateOf(listOf<CategoriaUi>())
    var usuarios by mutableStateOf(USUARIOS_DEMO)
    var formCategoria by mutableStateOf(CategoriaFormUiState())
    var formProducto by mutableStateOf(ProductoFormUiState())
    private var siguientePedido = 1034
    private var siguienteId = 100 // ids para categorías y productos nuevos

    // ---------- Consultas ----------

    fun producto(id: Int): ProductoUi? = catalogo.productos.find { it.id == id }

    val itemsCarrito: List<Pair<ProductoUi, Int>>
        get() = carrito.mapNotNull { (id, cantidad) -> producto(id)?.let { it to cantidad } }

    // El cliente solo ve sus pedidos
    val misPedidos: List<PedidoUi>
        get() = pedidos.filter { it.cliente == perfil.nombreCompleto }

    // Pedidos que esperan que el administrador confirme el pago
    val pedidosPendientes: Int
        get() = pedidos.count { it.estado == EstadoPedido.CONFIRMACION_PAGO }

    // ---------- Recuperar contraseña ----------

    fun validarRecuperar(): Boolean {
        val error = if (esCorreoValido(recuperar.correo)) null else "Ingresa un correo válido"
        recuperar = recuperar.copy(errorCorreo = error)
        return error == null
    }

    // ---------- Carrito ----------

    fun agregarAlCarrito(id: Int) = cambiarCantidad(id, 1)

    // Suma o resta; si la cantidad llega a 0 el producto sale del carrito
    fun cambiarCantidad(id: Int, cambio: Int) {
        val nueva = (carrito[id] ?: 0) + cambio
        carrito = if (nueva <= 0) carrito - id else carrito + (id to nueva)
    }

    // Crea el pedido con lo que hay en el carrito y lo vacía. Devuelve el número del pedido
    fun confirmarCompra(): Int {
        val pedido = PedidoUi(
            id = siguientePedido++,
            cliente = perfil.nombreCompleto,
            correo = perfil.correo,
            telefono = perfil.telefono,
            direccion = perfil.direccionCompleta,
            lineas = itemsCarrito.map { (producto, cantidad) -> LineaPedido(producto.nombre, cantidad) },
            total = itemsCarrito.sumOf { (producto, cantidad) -> producto.precio * cantidad }
        )
        pedidos = listOf(pedido) + pedidos // el más nuevo primero
        carrito = emptyMap()
        return pedido.id
    }

    // ---------- Pago y perfil ----------

    fun validarPago(pago: DatosPagoUiState): DatosPagoUiState = pago.copy(
        errorNumero = if (pago.numero.length == 16) null else "Debe tener 16 dígitos",
        errorVencimiento = if (Regex("""\d{2}/\d{2}""").matches(pago.vencimiento)) null else "Formato MM/AA",
        errorCvv = if (pago.cvv.length in 3..4) null else "3 o 4 dígitos"
    )

    // Guarda la tarjeta ingresada al comprar; devuelve true si los datos son válidos
    fun guardarPagoCompra(): Boolean {
        pagoCompra = validarPago(pagoCompra)
        val valido = pagoCompra.errorNumero == null && pagoCompra.errorVencimiento == null && pagoCompra.errorCvv == null
        if (valido) perfil = perfil.copy(pago = pagoCompra)
        return valido
    }

    // Valida la pestaña (0 Personal, 1 Dirección, 2 Pago); si está bien, deja de editar
    fun guardarPerfil(seccion: Int): Boolean {
        val p = perfil
        val revisado = when (seccion) {
            0 -> p.copy(
                errorTelefono = if (p.telefono.isNotBlank()) null else "Obligatorio",
                errorCorreo = if (esCorreoValido(p.correo)) null else "Ingresa un correo válido"
            )
            1 -> p.copy(
                errorRegion = if (p.region != null) null else "Selecciona una región",
                errorComuna = if (p.comuna != null) null else "Selecciona una comuna",
                errorCalle = if (p.calle.isNotBlank()) null else "Obligatorio",
                errorNumero = if (p.numero.isNotBlank()) null else "Obligatorio"
            )
            else -> p.copy(pago = validarPago(p.pago))
        }
        val valido = listOf(
            revisado.errorTelefono, revisado.errorCorreo, revisado.errorRegion, revisado.errorComuna,
            revisado.errorCalle, revisado.errorNumero, revisado.pago.errorNumero,
            revisado.pago.errorVencimiento, revisado.pago.errorCvv
        ).all { it == null }
        perfil = if (valido) revisado.copy(seccionEditando = null) else revisado
        return valido
    }

    // La sesión la cierra SesionViewModel; aquí solo se vacía el carrito de la demo
    fun cerrarSesion() {
        carrito = emptyMap()
    }

    // ---------- Administrador: pedidos ----------

    fun cambiarEstadoPedido(id: Int, nuevo: EstadoPedido) {
        pedidos = pedidos.map { if (it.id == id) it.copy(estado = nuevo) else it }
    }

    // ---------- Administrador: categorías ----------

    // Prepara el formulario: vacío para una nueva o con los datos de la que se edita
    fun abrirFormCategoria(id: Int?) {
        val categoria = categorias.find { it.id == id }
        formCategoria = CategoriaFormUiState(id = categoria?.id, nombre = categoria?.nombre.orEmpty())
    }

    fun guardarCategoria(): Boolean {
        val f = formCategoria
        val nombre = f.nombre.trim()
        val repetida = categorias.any { it.nombre.equals(nombre, ignoreCase = true) && it.id != f.id }
        val error = when {
            nombre.isEmpty() -> "Ingresa un nombre"
            repetida -> "Ya existe una categoría con ese nombre"
            else -> null
        }
        formCategoria = f.copy(errorNombre = error)
        if (error != null) return false
        categorias = if (f.id == null) categorias + CategoriaUi(siguienteId++, nombre)
        else categorias.map { if (it.id == f.id) it.copy(nombre = nombre) else it }
        return true
    }

    // Devuelve el mensaje para el usuario
    fun eliminarCategoria(id: Int): String {
        if (catalogo.productos.any { it.categoriaId == id }) return "No se puede eliminar: tiene productos asociados"
        categorias = categorias.filter { it.id != id }
        return "Categoría eliminada"
    }

    // ---------- Administrador: productos ----------

    fun abrirFormProducto(id: Int?) {
        val p = producto(id ?: -1)
        formProducto = if (p == null) ProductoFormUiState(categoriaId = categorias.firstOrNull()?.id)
        else ProductoFormUiState(p.id, p.nombre, p.descripcion, p.precio.toString(), p.categoriaId)
    }

    fun guardarProducto(): Boolean {
        val f = formProducto
        val precio = f.precio.toIntOrNull() ?: 0
        val revisado = f.copy(
            errorNombre = if (f.nombre.isNotBlank()) null else "Obligatorio",
            errorPrecio = if (precio > 0) null else "Ingresa un precio válido",
            errorCategoria = if (f.categoriaId != null) null else "Selecciona una categoría"
        )
        formProducto = revisado
        if (revisado.errorNombre != null || revisado.errorPrecio != null || revisado.errorCategoria != null) return false
        val datos = ProductoUi(
            id = f.id ?: siguienteId++, nombre = f.nombre.trim(), precio = precio,
            descripcion = f.descripcion.trim(), categoriaId = f.categoriaId ?: 0
        )
        val productos = if (f.id == null) catalogo.productos + datos
        else catalogo.productos.map { if (it.id == f.id) datos.copy(esFavorito = it.esFavorito) else it }
        catalogo = catalogo.copy(productos = productos)
        return true
    }

    // Al eliminar un producto también sale del carrito
    fun eliminarProducto(id: Int) {
        catalogo = catalogo.copy(productos = catalogo.productos.filter { it.id != id })
        carrito = carrito - id
    }

    // ---------- Administrador: usuarios ----------

    fun usuario(id: Int): UsuarioUi? = usuarios.find { it.id == id }

    fun alternarBloqueo(id: Int) {
        usuarios = usuarios.map { if (it.id == id) it.copy(bloqueado = !it.bloqueado) else it }
    }

    fun hacerAdmin(id: Int) {
        usuarios = usuarios.map { if (it.id == id) it.copy(esAdmin = true) else it }
    }
}
