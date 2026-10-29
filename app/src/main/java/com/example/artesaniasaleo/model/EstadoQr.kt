package com.example.artesaniasaleo.model

// Qué está pasando en la pantalla "Escanear QR"
sealed class EstadoQr {
    data object Escaneando : EstadoQr()                    // la cámara busca un código
    data class Producto(val id: Int) : EstadoQr()          // "producto:3" → abrir esa pieza
    data class Pedido(val id: Int) : EstadoQr()            // "pedido:1034" → abrir ese pedido
    data object NoReconocido : EstadoQr()                  // QR ajeno a Ale y Leo o id que no existe
}
