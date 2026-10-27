package com.example.artesaniasaleo.model

data class PerfilUiState(
    // Pestaña Personal (RUT, nombres y apellido no se editan)
    val rut: String = "",
    val nombres: String = "",
    val apellido: String = "",
    val telefono: String = "",
    val correo: String = "",
    val errorTelefono: String? = null,
    val errorCorreo: String? = null,

    // Pestaña Dirección
    val region: String? = null,
    val comuna: String? = null,
    val calle: String = "",
    val numero: String = "",
    val departamento: String = "",
    val errorRegion: String? = null,
    val errorComuna: String? = null,
    val errorCalle: String? = null,
    val errorNumero: String? = null,

    // Pestaña Pago
    val pago: DatosPagoUiState = DatosPagoUiState(),

    // Pestaña que se está editando: 0 Personal, 1 Dirección, 2 Pago; null = ninguna
    val seccionEditando: Int? = null
) {
    val nombreCompleto: String
        get() = "$nombres $apellido"

    // Texto de la dirección para el pedido: "Región, Comuna, Calle Número (Depto)"
    val direccionCompleta: String
        get() = "$region, $comuna, $calle $numero" +
                if (departamento.isNotBlank()) " ($departamento)" else ""
}
