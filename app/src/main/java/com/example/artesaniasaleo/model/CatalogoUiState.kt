package com.example.artesaniasaleo.model

data class CatalogoUiState(
    val busqueda: String = "",
    val productos: List<ProductoUi> = emptyList(),
    val cargando: Boolean = false,
    val errorGeneral: String? = null
) {
    // Productos cuyo nombre contiene lo buscado (sin importar mayúsculas)
    val productosFiltrados: List<ProductoUi>
        get() = productos.filter { it.nombre.contains(busqueda, ignoreCase = true) }
}
