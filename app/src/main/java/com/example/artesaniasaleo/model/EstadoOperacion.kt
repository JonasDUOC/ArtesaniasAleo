package com.example.artesaniasaleo.model

// Estado de una operación que demora (consultar o guardar en Room): login, registro,
// guardar perfil, guardar producto... Se reutiliza en todos los ViewModel.
// "sealed" = el compilador conoce todas las opciones, así un `when` no puede olvidar ninguna.
sealed class EstadoOperacion {
    data object Inactivo : EstadoOperacion()                    // aún no se intenta nada
    data object Cargando : EstadoOperacion()                    // esperando a Room
    data object Exito : EstadoOperacion()                       // terminó bien
    data class Error(val mensaje: String) : EstadoOperacion()   // mensaje para el usuario
}
