package com.example.artesaniasaleo.model

// Solo lo que escribe el usuario y los errores de cada campo.
// Cómo va el proceso (cargando / error / éxito) está en EstadoOperacion.
data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val errorCorreo: String? = null,
    val errorContrasena: String? = null
) {
    // Ambos campos llenos (la pantalla además lo deshabilita mientras carga)
    val camposCompletos: Boolean
        get() = correo.isNotBlank() && contrasena.isNotBlank()
}
