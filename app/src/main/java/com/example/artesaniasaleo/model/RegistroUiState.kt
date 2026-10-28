package com.example.artesaniasaleo.model

data class RegistroUiState(
    val pasoActual: Int = 1, // 1 = cuenta, 2 = código, 3 = datos personales

    // Paso 1: cuenta
    val correo: String = "",
    val contrasena: String = "",
    val confirmarContrasena: String = "",
    val aceptaTerminos: Boolean = false,
    val errorCorreo: String? = null,
    val errorContrasena: String? = null,
    val errorConfirmar: String? = null,
    val errorTerminos: String? = null,

    // Paso 2: código de verificación
    val codigo: String = "",
    val errorCodigo: String? = null,

    // Paso 3: datos personales y dirección
    val rut: String = "",
    val nombres: String = "",
    val apellido: String = "",
    val region: String? = null, // null = aún no elige
    val comuna: String? = null,
    val calle: String = "",
    val numero: String = "",
    val departamento: String = "", // opcional
    val errorRut: String? = null,
    val errorNombres: String? = null,
    val errorApellido: String? = null,
    val errorRegion: String? = null,
    val errorComuna: String? = null,
    val errorCalle: String? = null,
    val errorNumero: String? = null
    // Cómo va el proceso (cargando / error / éxito) está en EstadoOperacion
) {
    // Habilita "Continuar" solo si los campos obligatorios del paso actual están llenos
    val puedeContinuar: Boolean
        get() = when (pasoActual) {
            1 -> correo.isNotBlank() && contrasena.isNotBlank() &&
                    confirmarContrasena.isNotBlank() && aceptaTerminos
            2 -> codigo.length == 4
            3 -> rut.isNotBlank() && nombres.isNotBlank() && apellido.isNotBlank() &&
                    region != null && comuna != null && calle.isNotBlank() && numero.isNotBlank()
            else -> false
        }
}