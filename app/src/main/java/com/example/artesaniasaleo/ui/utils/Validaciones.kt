package com.example.artesaniasaleo.ui.utils

import com.example.artesaniasaleo.model.DatosPagoUiState
import java.time.YearMonth

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

// Control de stock: número entero desde 0 (0 = agotado)
fun esStockValido(stock: String): Boolean = stock.trim().toIntOrNull()?.let { it >= 0 } == true

// CU-11: devuelve la tarjeta con los errores de cada campo (null = campo correcto)
fun validarTarjeta(pago: DatosPagoUiState): DatosPagoUiState = pago.copy(
    errorNumero = if (pago.numero.length == 16 && pago.numero.all { it.isDigit() }) null else "Debe tener 16 dígitos",
    errorVencimiento = when {
        !Regex("""(0[1-9]|1[0-2])/\d{2}""").matches(pago.vencimiento) -> "Formato MM/AA"
        estaVencida(pago.vencimiento) -> "La tarjeta está vencida"
        else -> null
    },
    errorCvv = if (pago.cvv.length in 3..4 && pago.cvv.all { it.isDigit() }) null else "3 o 4 dígitos"
)

fun tarjetaSinErrores(pago: DatosPagoUiState): Boolean =
    pago.errorNumero == null && pago.errorVencimiento == null && pago.errorCvv == null

// "MM/AA" → mes y año. La tarjeta sirve hasta el último día de ese mes,
// así que solo está vencida si ese mes ya pasó
private fun estaVencida(vencimiento: String): Boolean {
    val (mes, anio) = vencimiento.split("/").map { it.toInt() }
    return YearMonth.of(2000 + anio, mes).isBefore(YearMonth.now())
}
