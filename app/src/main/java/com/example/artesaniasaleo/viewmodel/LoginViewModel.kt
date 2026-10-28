package com.example.artesaniasaleo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.LoginUiState
import com.example.artesaniasaleo.repository.UsuarioRepository
import com.example.artesaniasaleo.ui.utils.esCorreoValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// CU-05 Iniciar sesión. Valida el formulario y le pide al repository que inicie la sesión.
// No navega: cuando la sesión queda guardada, SesionViewModel se entera y la app cambia de pantalla.
class LoginViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // Privado y mutable: solo el ViewModel lo cambia. Público y de solo lectura: la pantalla lo observa.
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    private val _operacion = MutableStateFlow<EstadoOperacion>(EstadoOperacion.Inactivo)
    val operacion: StateFlow<EstadoOperacion> = _operacion.asStateFlow()

    fun onCorreoChange(valor: String) {
        // copy() crea un estado nuevo → Compose lo detecta y redibuja. Al escribir se borra el error.
        _uiState.update { it.copy(correo = valor, errorCorreo = null) }
        limpiarError()
    }

    fun onContrasenaChange(valor: String) {
        _uiState.update { it.copy(contrasena = valor, errorContrasena = null) }
        limpiarError()
    }

    fun iniciarSesion() {
        if (_operacion.value is EstadoOperacion.Cargando) return // evita el doble toque
        if (!validarFormulario()) return                           // errores de formato: no se consulta Room

        val estado = _uiState.value
        // Corrutina: Room no se consulta en el hilo principal. viewModelScope se cancela
        // solo si el ViewModel se destruye.
        viewModelScope.launch {
            _operacion.value = EstadoOperacion.Cargando
            try {
                // Si todo está bien, el repository guarda el id en DataStore
                usuarioRepository.login(estado.correo, estado.contrasena)
                _operacion.value = EstadoOperacion.Exito
            } catch (e: Exception) {
                // Mensajes de CU-05 lanzados por el repository:
                // "La cuenta no existe" / "Contraseña incorrecta" / "Tu cuenta está bloqueada"
                _operacion.value = EstadoOperacion.Error(e.message ?: "No se pudo iniciar sesión")
            }
        }
    }

    // Solo el formato. Que la cuenta exista y la contraseña calce lo decide el repository.
    private fun validarFormulario(): Boolean {
        val errorCorreo = if (esCorreoValido(_uiState.value.correo)) null else "Ingresa un correo válido"
        _uiState.update { it.copy(errorCorreo = errorCorreo) }
        return errorCorreo == null
    }

    // Si había un error de credenciales y el usuario vuelve a escribir, el mensaje desaparece
    private fun limpiarError() {
        if (_operacion.value is EstadoOperacion.Error) _operacion.value = EstadoOperacion.Inactivo
    }
}
