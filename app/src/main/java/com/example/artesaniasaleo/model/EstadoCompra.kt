package com.example.artesaniasaleo.model

// Resultado de confirmar la compra (CU-24). Es una sealed class propia porque, además de
// Cargando / Exito / Error, la compra tiene un caso especial: no hay tarjeta guardada.
sealed class EstadoCompra {
    data object Inactivo : EstadoCompra()
    data object Procesando : EstadoCompra()
    data class Confirmada(val pedidoId: Int) : EstadoCompra()   // → seguimiento
    data object SinTarjeta : EstadoCompra()                     // → formulario de datos de pago
    data class Error(val mensaje: String) : EstadoCompra()
}
