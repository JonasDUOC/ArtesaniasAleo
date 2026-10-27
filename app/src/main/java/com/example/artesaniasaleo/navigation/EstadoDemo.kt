package com.example.artesaniasaleo.navigation

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.example.artesaniasaleo.model.CATEGORIAS_DEMO
import com.example.artesaniasaleo.model.CatalogoUiState
import com.example.artesaniasaleo.model.DatosPagoUiState
import com.example.artesaniasaleo.model.LineaPedido
import com.example.artesaniasaleo.model.LoginUiState
import com.example.artesaniasaleo.model.PEDIDOS_DEMO
import com.example.artesaniasaleo.model.PERFIL_DEMO
import com.example.artesaniasaleo.model.PRODUCTOS_DEMO
import com.example.artesaniasaleo.model.PedidoUi
import com.example.artesaniasaleo.model.PerfilUiState
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.model.RecuperarUiState
import com.example.artesaniasaleo.model.RegistroUiState

// PROVISIONAL: guarda los datos en memoria y valida, para que la app funcione completa.
// Cuando existan los ViewModel y el repository (con Jonas), cada parte pasa a su ViewModel
// y los datos se leen y guardan en Room. Las pantallas no cambian.
class EstadoDemo {
    var login by mutableStateOf(LoginUiState())
    var registro by mutableStateOf(RegistroUiState())
    var recuperar by mutableStateOf(RecuperarUiState())
    var catalogo by mutableStateOf(CatalogoUiState(productos = PRODUCTOS_DEMO))
    var perfil by mutableStateOf(PERFIL_DEMO)
    var pagoCompra by mutableStateOf(DatosPagoUiState())
    var carrito by mutableStateOf(mapOf<Int, Int>())   // id del producto -> cantidad
    var pedidos by mutableStateOf(PEDIDOS_DEMO)
    val categorias = CATEGORIAS_DEMO
    private var siguientePedido = 1034

    // ---------- Consultas ----------

    fun producto(id: Int): ProductoUi? = catalogo.productos.find { it.id == id }

    val favoritos: List<ProductoUi>
        get() = catalogo.productos.filter { it.esFavorito }

    val itemsCarrito: List<Pair<ProductoUi, Int>>
        get() = carrito.mapNotNull { (id, cantidad) -> producto(id)?.let { it to cantidad } }

    // El cliente solo ve sus pedidos
    val misPedidos: List<PedidoUi>
        get() = pedidos.filter { it.cliente == perfil.nombreCompleto }

    // ---------- Login y registro (mismas reglas que el mockup) ----------

    fun validarLogin(): Boolean {
        val errorCorreo = if (esCorreoValido(login.correo)) null else "Ingresa un correo válido"
        val errorContrasena = if (login.contrasena.length >= 4) null else "Contraseña incorrecta"
        login = login.copy(errorCorreo = errorCorreo, errorContrasena = errorContrasena)
        return errorCorreo == null && errorContrasena == null
    }

    // Valida la etapa actual del registro; devuelve true si se puede avanzar
    fun validarEtapaRegistro(): Boolean {
        val r = registro
        registro = when (r.pasoActual) {
            1 -> r.copy(
                errorCorreo = if (esCorreoValido(r.correo)) null else "Ingresa un correo válido",
                errorContrasena = if (r.contrasena.length >= 6) null else "Debe tener al menos 6 caracteres",
                errorConfirmar = if (r.confirmarContrasena == r.contrasena) null else "Las contraseñas no coinciden",
                errorTerminos = if (r.aceptaTerminos) null else "Debes aceptar los términos para continuar"
            )
            2 -> r.copy(errorCodigo = if (r.codigo == "1234") null else "Código incorrecto") // demo: 1234
            else -> r.copy(errorRut = if (esRutValido(r.rut)) null else "RUT inválido (verifica el dígito verificador)")
        }
        val x = registro
        return listOf(x.errorCorreo, x.errorContrasena, x.errorConfirmar, x.errorTerminos, x.errorCodigo, x.errorRut)
            .all { it == null }
    }

    // Al terminar el registro, los datos pasan al perfil
    fun finalizarRegistro() {
        val r = registro
        perfil = PerfilUiState(
            rut = r.rut, nombres = r.nombres, apellido = r.apellido, correo = r.correo,
            region = r.region, comuna = r.comuna, calle = r.calle, numero = r.numero, departamento = r.departamento
        )
        registro = RegistroUiState()
    }

    fun validarRecuperar(): Boolean {
        val error = if (esCorreoValido(recuperar.correo)) null else "Ingresa un correo válido"
        recuperar = recuperar.copy(errorCorreo = error)
        return error == null
    }

    // ---------- Catálogo, favoritos y carrito ----------

    fun alternarFavorito(id: Int) {
        catalogo = catalogo.copy(
            productos = catalogo.productos.map { if (it.id == id) it.copy(esFavorito = !it.esFavorito) else it }
        )
    }

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

    fun cerrarSesion() {
        login = LoginUiState()
        carrito = emptyMap()
    }

    // ---------- Validaciones ----------

    private fun esCorreoValido(correo: String) = Regex("""\S+@\S+\.\S+""").matches(correo.trim())

    // RUT chileno con formato 12.345.678-5 y dígito verificador (módulo 11)
    private fun esRutValido(rut: String): Boolean {
        val partes = Regex("""(\d{1,2})\.(\d{3})\.(\d{3})-([\dkK])""").matchEntire(rut.trim()) ?: return false
        val (a, b, c, dv) = partes.destructured
        val cuerpo = a + b + c
        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador < 7) multiplicador + 1 else 2
        }
        val esperado = when (val resto = 11 - suma % 11) {
            11 -> "0"
            10 -> "K"
            else -> resto.toString()
        }
        return esperado == dv.uppercase()
    }
}
