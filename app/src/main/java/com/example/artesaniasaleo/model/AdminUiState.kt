package com.example.artesaniasaleo.model

import android.net.Uri

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
    val stock: String = "",        // texto, como el precio; se valida al guardar
    val categoriaId: Int? = null,
    // Imágenes elegidas de la galería o tomadas con la cámara (Guía 13: el estado guarda Uri)
    val imagenes: List<Uri> = emptyList(),
    val errorNombre: String? = null,
    val errorPrecio: String? = null,
    val errorStock: String? = null,
    val errorCategoria: String? = null,
    val errorImagenes: String? = null
)
