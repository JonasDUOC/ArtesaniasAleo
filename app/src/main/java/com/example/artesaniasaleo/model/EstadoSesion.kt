package com.example.artesaniasaleo.model

// ¿Hay alguien con la sesión iniciada? Lo decide SesionViewModel leyendo DataStore + Room.
sealed class EstadoSesion {
    data object Verificando : EstadoSesion()   // al abrir la app, mientras se lee DataStore
    data object SinSesion : EstadoSesion()     // nadie conectado → pantalla de login
    data class Activa(val usuarioId: Int, val esAdmin: Boolean) : EstadoSesion()
}
