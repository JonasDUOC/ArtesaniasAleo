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

    // Arman la ruta con el id real, por ejemplo "detalle/3"
    fun categoria(id: Int) = "categoria/$id"
    fun detalle(id: Int) = "detalle/$id"
    fun pedido(id: Int) = "pedido/$id"
}
