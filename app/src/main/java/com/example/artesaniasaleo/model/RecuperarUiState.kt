package com.example.artesaniasaleo.model

// CU-07 en dos etapas: 1 = correo, 2 = código + nueva contraseña
data class RecuperarUiState(
    val paso: Int = 1,
    val correo: String = "",
    val codigo: String = "",
    val nueva: String = "",
    val confirmar: String = "",
    val errorCorreo: String? = null,
    val errorCodigo: String? = null,
    val errorNueva: String? = null,
    val errorConfirmar: String? = null
) {
    // Habilita el botón solo con los campos de la etapa actual llenos
    val puedeContinuar: Boolean
        get() = when (paso) {
            1 -> correo.isNotBlank()
            else -> codigo.length == 4 && nueva.isNotBlank() && confirmar.isNotBlank()
        }
}
