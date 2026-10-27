package com.example.artesaniasaleo.model

// Datos de la tarjeta (ficticios). Se usa en "Datos de pago" y en la pestaña Pago del perfil
data class DatosPagoUiState(
    val tipo: String = "Crédito",
    val numero: String = "",
    val vencimiento: String = "",   // formato MM/AA
    val cvv: String = "",
    val errorNumero: String? = null,
    val errorVencimiento: String? = null,
    val errorCvv: String? = null
) {
    // Hay tarjeta guardada si el número tiene sus 16 dígitos
    val tieneTarjeta: Boolean
        get() = numero.length == 16
}

val TIPOS_TARJETA = listOf("Crédito", "Débito")
