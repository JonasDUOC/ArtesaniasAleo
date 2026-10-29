package com.example.artesaniasaleo.model

// Lo que muestra una tarjeta del catálogo (junta datos de Producto, ImagenProducto y Favorito)
data class ProductoUi(
    val id: Int,
    val nombre: String,             // Producto.titulo
    val precio: Int,                // pesos chilenos, sin decimales
    val imagenUrl: String? = null,  // ruta de la primera imagen; null = sin imagen
    val imagenes: List<String> = emptyList(), // todas las rutas, en orden (carrusel del detalle)
    val esFavorito: Boolean = false,
    val descripcion: String = "",   // se muestra en el detalle
    val categoriaId: Int = 0,       // para filtrar por categoría
    val stock: Int = 0              // unidades disponibles; 0 = agotado
) {
    val agotado: Boolean get() = stock <= 0
}
