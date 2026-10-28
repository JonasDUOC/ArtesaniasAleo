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

// CU-07 Recuperar contraseña:
// etapa 1 valida que el correo exista y "envía" el código; etapa 2 valida el código y guarda la nueva contraseña.
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

    fun onCodigoChange(valor: String) {
        // Solo números y máximo 4 dígitos
        if (valor.length <= 4 && valor.all { it.isDigit() }) {
            _uiState.update { it.copy(codigo = valor, errorCodigo = null) }
        }
    }

    fun onNuevaChange(valor: String) {
        _uiState.update { it.copy(nueva = valor, errorNueva = null) }
    }

    fun onConfirmarChange(valor: String) {
        _uiState.update { it.copy(confirmar = valor, errorConfirmar = null) }
    }

    // Botón principal: cada etapa hace lo suyo
    fun continuar() {
        if (_operacion.value is EstadoOperacion.Cargando) return
        if (_uiState.value.paso == 1) verificarCorreo() else cambiarContrasena()
    }

    // Flecha atrás. true = volvió a la etapa 1; false = estaba en la 1 y hay que volver al login
    fun retroceder(): Boolean {
        if (_uiState.value.paso == 1) return false
        _uiState.update { it.copy(paso = 1, codigo = "", nueva = "", confirmar = "") }
        return true
    }

    fun operacionMostrada() {
        _operacion.value = EstadoOperacion.Inactivo
    }

    // ---------- Etapa 1: el correo debe existir (CU-07, paso 1 y excepción 1) ----------

    private fun verificarCorreo() {
        val correo = _uiState.value.correo
        if (!esCorreoValido(correo)) {
            _uiState.update { it.copy(errorCorreo = "Ingresa un correo válido") }
            return
        }
        viewModelScope.launch {
            _operacion.value = EstadoOperacion.Cargando
            try {
                if (usuarioRepository.correoDisponible(correo)) {
                    // "Disponible" = nadie lo usa = la cuenta no existe
                    _uiState.update { it.copy(errorCorreo = "El correo no está registrado") }
                } else {
                    // CU-07 paso 2: aquí se "envía" el código (en el MVP es fijo, igual que en el registro)
                    _uiState.update { it.copy(paso = 2) }
                }
                _operacion.value = EstadoOperacion.Inactivo
            } catch (e: Exception) {
                _operacion.value = EstadoOperacion.Error(e.message ?: "No se pudo verificar el correo")
            }
        }
    }

    // ---------- Etapa 2: código + nueva contraseña (CU-07, paso 3) ----------

    private fun cambiarContrasena() {
        val r = _uiState.value
        val revisado = r.copy(
            errorCodigo = if (r.codigo == CODIGO_DEMO) null else "Código incorrecto",
            errorNueva = if (r.nueva.length >= MIN_CONTRASENA) null else "Debe tener al menos $MIN_CONTRASENA caracteres",
            errorConfirmar = if (r.confirmar == r.nueva) null else "Las contraseñas no coinciden"
        )
        _uiState.value = revisado
        if (revisado.errorCodigo != null || revisado.errorNueva != null || revisado.errorConfirmar != null) return

        viewModelScope.launch {
            _operacion.value = EstadoOperacion.Cargando
            try {
                usuarioRepository.recuperarContrasenia(revisado.correo, revisado.nueva)
                _operacion.value = EstadoOperacion.Exito // la pantalla avisa y vuelve al login
            } catch (e: Exception) {
                _operacion.value = EstadoOperacion.Error(e.message ?: "No se pudo cambiar la contraseña")
            }
        }
    }

    private companion object {
        const val MIN_CONTRASENA = 6   // misma regla que el registro
        const val CODIGO_DEMO = "1234" // mismo código simulado que el registro (CU-02)
    }
}
