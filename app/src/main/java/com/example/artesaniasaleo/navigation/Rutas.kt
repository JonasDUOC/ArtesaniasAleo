package com.example.artesaniasaleo.navigation

// Nombres de las pantallas a las que se puede navegar (un solo lugar para no escribirlos a mano)
object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val TERMINOS = "terminos"
    const val RECUPERAR = "recuperar"
    const val CATALOGO = "catalogo"
    const val CATEGORIAS = "categorias"
    const val CATEGORIA = "categoria/{id}"    // {id}: argumento que va dentro de la ruta
    const val DETALLE = "detalle/{id}"
    const val FAVORITOS = "favoritos"
    const val CARRITO = "carrito"
    const val DATOS_PAGO = "datos_pago"
    const val PERFIL = "perfil"
    const val SEGUIMIENTO = "seguimiento"
    const val PEDIDO = "pedido/{id}"
    const val ESCANEAR_QR = "escanear_qr"

    // Administrador
    const val RESUMEN = "resumen"
    const val NOTIFICACIONES = "notificaciones"
    const val PEDIDO_ADMIN = "pedido_admin/{id}"
    const val GESTION = "gestion"
    const val FORM_CATEGORIA = "form_categoria"
    const val FORM_PRODUCTO = "form_producto"
    const val USUARIOS = "usuarios"
    const val EDITAR_USUARIO = "editar_usuario/{id}"

    // Pantallas que se ven sin sesión iniciada; todas las demás exigen sesión
    val SIN_SESION = setOf(LOGIN, REGISTRO, TERMINOS, RECUPERAR)

    // Arman la ruta con el id real, por ejemplo "detalle/3"
    fun categoria(id: Int) = "categoria/$id"
    fun detalle(id: Int) = "detalle/$id"
    fun pedido(id: Int) = "pedido/$id"
    fun pedidoAdmin(id: Int) = "pedido_admin/$id"
    fun editarUsuario(id: Int) = "editar_usuario/$id"
}
