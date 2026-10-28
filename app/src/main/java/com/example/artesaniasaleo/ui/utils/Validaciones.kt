package com.example.artesaniasaleo.ui.utils

// Validaciones de formularios en un solo lugar (antes estaban dentro de EstadoDemo).
// Son funciones puras: no usan Android, así se podrán probar con pruebas unitarias (Unidad 3).

fun esCorreoValido(correo: String): Boolean =
    Regex("""\S+@\S+\.\S+""").matches(correo.trim())

// RUT chileno con formato 12.345.678-5 y dígito verificador (módulo 11)
fun esRutValido(rut: String): Boolean {
    val partes = Regex("""(\d{1,2})\.(\d{3})\.(\d{3})-([\dkK])""").matchEntire(rut.trim()) ?: return false
    val (a, b, c, dv) = partes.destructured
    val cuerpo = a + b + c
    var suma = 0
    var multiplicador = 2
    for (digito in cuerpo.reversed()) {
        suma += digito.digitToInt() * multiplicador
        multiplicador = if (multiplicador < 7) multiplicador + 1 else 2
    }
    val esperado = when (val resto = 11 - suma % 11) {
        11 -> "0"
        10 -> "K"
        else -> resto.toString()
    }
    return esperado == dv.uppercase()
}
