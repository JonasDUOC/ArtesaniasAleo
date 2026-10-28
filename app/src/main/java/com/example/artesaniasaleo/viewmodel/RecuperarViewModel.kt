package com.example.artesaniasaleo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.RecuperarUiState
import com.example.artesaniasaleo.repository.UsuarioRepository
import com.example.artesaniasaleo.ui.utils.esCorreoValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// CU-07 (parte 1): valida que el correo exista y "envía" el código.
// Falta la etapa de nueva contraseña: la pantalla aún no la tiene (UsuarioRepository.recuperarContrasenia ya existe).
class RecuperarViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecuperarUiState())
    val uiState: StateFlow<RecuperarUiState> = _uiState.asStateFlow()

    private val _operacion = MutableStateFlow<EstadoOperacion>(EstadoOperacion.Inactivo)
    val operacion: StateFlow<EstadoOperacion> = _operacion.asStateFlow()

    fun onCorreoChange(valor: String) {
        _uiState.update { it.copy(correo = valor, errorCorreo = null) }
    }

    fun enviar() {
        val correo = _uiState.value.correo
        if (!esCorreoValido(correo)) {
            _uiState.update { it.copy(errorCorreo = "Ingresa un correo válido") }
            return
        }
        viewModelScope.launch {
            try {
                if (usuarioRepository.correoDisponible(correo)) {
                    // "Disponible" = nadie lo usa = no existe la cuenta (CU-07, excepción 1)
                    _uiState.update { it.copy(errorCorreo = "El correo no está registrado") }
                } else {
                    _operacion.value = EstadoOperacion.Exito
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorCorreo = e.message ?: "No se pudo verificar el correo") }
            }
        }
    }

    fun operacionMostrada() {
        _operacion.value = EstadoOperacion.Inactivo
    }
}
