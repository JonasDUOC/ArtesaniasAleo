package com.example.artesaniasaleo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artesaniasaleo.model.EstadoSesion
import com.example.artesaniasaleo.repository.UsuarioRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Fuente única de verdad de la sesión para toda la app: quién está conectado y si es admin.
// Vive mientras vive la Activity (se crea en AppNavegacion, fuera del NavHost).
class SesionViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // usuarioActual = DataStore (id) + Room (usuario). Cada vez que cambia el id guardado
    // o el usuario en la BD (ej.: el admin le cambia el rol), llega un valor nuevo.
    val estado: StateFlow<EstadoSesion> = usuarioRepository.usuarioActual
        .map { usuario ->
            if (usuario == null || usuario.estado == "bloqueado") EstadoSesion.SinSesion
            else EstadoSesion.Activa(usuarioId = usuario.id, esAdmin = usuario.rol == "admin")
        }
        // stateIn convierte el Flow de Room en un StateFlow que la pantalla puede observar.
        // Valor inicial Verificando: aún no se lee DataStore.
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = EstadoSesion.Verificando
        )

    fun cerrarSesion() {
        viewModelScope.launch { usuarioRepository.cerrarSesion() }
    }
}
