package com.example.artesaniasaleo.model

data class LoginUiState(
    val correo: String = "",
    val contrasena: String = "",
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorGeneral: String? = null, // ej.: "Correo o contraseña incorrectos"
    val cargando: Boolean = false,
    val loginExitoso: Boolean = false
) {
    // El botón "Ingresar" solo se habilita con ambos campos llenos y sin carga en curso
    val puedeEnviar: Boolean
        get() = correo.isNotBlank() && contrasena.isNotBlank() && !cargando
}