package com.example.artesaniasaleo.model

// Lo que muestra una tarjeta del catálogo (junta datos de Producto, ImagenProducto y Favorito)
data class ProductoUi(
    val id: Int,
    val nombre: String,             // Producto.titulo
    val precio: Int,                // pesos chilenos, sin decimales
    val imagenUrl: String? = null,  // ruta de la primera imagen; null = sin imagen
    val esFavorito: Boolean = false
)
