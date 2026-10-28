package com.example.artesaniasaleo.model

// Datos del catálogo que comparten Inicio, Detalle, Favoritos y Categorías.
// Cómo va la carga (cargando / error) está en EstadoOperacion.
data class CatalogoUiState(
    val busqueda: String = "",
    val productos: List<ProductoUi> = emptyList(),
    val categorias: List<CategoriaUi> = emptyList()
) {
    // CU-13: productos cuyo nombre o descripción contiene lo buscado (sin importar mayúsculas)
    val productosFiltrados: List<ProductoUi>
        get() {
            val texto = busqueda.trim()
            return productos.filter {
                it.nombre.contains(texto, ignoreCase = true) || it.descripcion.contains(texto, ignoreCase = true)
            }
        }

    // CU-19: solo los marcados como favoritos
    val favoritos: List<ProductoUi>
        get() = productos.filter { it.esFavorito }

    // CU-16: detalle de un producto
    fun producto(id: Int): ProductoUi? = productos.find { it.id == id }

    // CU-14 y CU-15: productos de una categoría, ordenados por precio
    fun productosDeCategoria(categoriaId: Int, ascendente: Boolean = true): List<ProductoUi> {
        val deLaCategoria = productos.filter { it.categoriaId == categoriaId }
        return if (ascendente) deLaCategoria.sortedBy { it.precio } else deLaCategoria.sortedByDescending { it.precio }
    }

    fun nombreCategoria(categoriaId: Int): String =
        categorias.find { it.id == categoriaId }?.nombre ?: "Categoría"
}
