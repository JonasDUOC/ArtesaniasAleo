package com.example.artesaniasaleo.model

// Usuario como lo ve el administrador en "Control de usuarios" (entidad Usuario + PerfilUsuario)
data class UsuarioUi(
    val id: Int,
    val nombre: String,
    val esAdmin: Boolean = false,
    val bloqueado: Boolean = false
)

// Formulario de categoría. id = null: categoría nueva
data class CategoriaFormUiState(
    val id: Int? = null,
    val nombre: String = "",
    val errorNombre: String? = null
)

// Formulario de producto. El precio se escribe como texto y se valida al guardar
data class ProductoFormUiState(
    val id: Int? = null,
    val nombre: String = "",
    val descripcion: String = "",
    val precio: String = "",
    val categoriaId: Int? = null,
    val errorNombre: String? = null,
    val errorPrecio: String? = null,
    val errorCategoria: String? = null
)
