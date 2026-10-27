package com.example.artesaniasaleo.model

// Estados del pedido en orden; "etiqueta" es el texto que ve el usuario
enum class EstadoPedido(val etiqueta: String) {
    CONFIRMACION_PAGO("Confirmación de pago"),
    EN_PREPARACION("En preparación"),
    ENVIADO("Enviado")
}

// Un producto dentro del pedido (PedidoDetalle)
data class LineaPedido(
    val nombre: String,
    val cantidad: Int
)

// Pedido con los datos del cliente que necesita el administrador para despacharlo
data class PedidoUi(
    val id: Int,
    val cliente: String,
    val correo: String,
    val telefono: String,
    val direccion: String,
    val lineas: List<LineaPedido>,
    val total: Int,
    val estado: EstadoPedido = EstadoPedido.CONFIRMACION_PAGO
)
