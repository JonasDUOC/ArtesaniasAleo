package com.example.artesaniasaleo.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artesaniasaleo.data.local.entity.Direccion
import com.example.artesaniasaleo.data.local.entity.PerfilUsuario
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.REGIONES
import com.example.artesaniasaleo.model.RegistroUiState
import com.example.artesaniasaleo.repository.UsuarioRepository
import com.example.artesaniasaleo.ui.utils.esCorreoValido
import com.example.artesaniasaleo.ui.utils.esRutValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// CU-01 a CU-03: registro en tres etapas (cuenta → código → datos personales y dirección).
// Al terminar, el repository guarda la cuenta y la sesión; SesionViewModel se entera y la app
// navega sola al catálogo (CU-03: "inicia sesión automáticamente").
class RegistroViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegistroUiState())
    val uiState: StateFlow<RegistroUiState> = _uiState.asStateFlow()

    private val _operacion = MutableStateFlow<EstadoOperacion>(EstadoOperacion.Inactivo)
    val operacion: StateFlow<EstadoOperacion> = _operacion.asStateFlow()

    // La pantalla entrega el estado completo ya modificado con copy() (así la diseñó Maxi)
    fun onEstadoChange(nuevo: RegistroUiState) {
        _uiState.value = nuevo
        if (_operacion.value is EstadoOperacion.Error) _operacion.value = EstadoOperacion.Inactivo
    }

    // Botón principal: cada etapa valida lo suyo
    fun continuar() {
        if (_operacion.value is EstadoOperacion.Cargando) return
        when (_uiState.value.pasoActual) {
            1 -> validarCuenta()
            2 -> verificarCodigo()
            else -> crearCuenta()
        }
    }

    // Flecha atrás. true = retrocedió una etapa; false = estaba en la etapa 1 y hay que volver al login
    fun retroceder(): Boolean {
        val paso = _uiState.value.pasoActual
        if (paso == 1) return false
        _uiState.update { it.copy(pasoActual = paso - 1) }
        _operacion.value = EstadoOperacion.Inactivo
        return true
    }

    // ---------- Etapa 1 (CU-01): correo, contraseña, confirmación y términos ----------

    private fun validarCuenta() {
        val r = _uiState.value
        val revisado = r.copy(
            errorCorreo = if (esCorreoValido(r.correo)) null else "Ingresa un correo válido",
            errorContrasena = if (r.contrasena.length >= MIN_CONTRASENA) null
                else "Debe tener al menos $MIN_CONTRASENA caracteres",
            errorConfirmar = if (r.confirmarContrasena == r.contrasena) null else "Las contraseñas no coinciden",
            errorTerminos = if (r.aceptaTerminos) null else "Debes aceptar los términos para continuar"
        )
        _uiState.value = revisado
        if (!sinErrores(revisado.errorCorreo, revisado.errorContrasena, revisado.errorConfirmar, revisado.errorTerminos)) return

        // Formato correcto: ahora sí se consulta Room (CU-01, excepción 1: correo ya registrado)
        viewModelScope.launch {
            _operacion.value = EstadoOperacion.Cargando
            try {
                if (usuarioRepository.correoDisponible(revisado.correo)) {
                    // CU-01 paso 3: aquí se "envía" el código. En el MVP es fijo (CODIGO_DEMO)
                    _uiState.update { it.copy(pasoActual = 2, codigo = "", errorCodigo = null) }
                } else {
                    _uiState.update { it.copy(errorCorreo = "Este correo ya está registrado. Usa otro") }
                }
                _operacion.value = EstadoOperacion.Inactivo
            } catch (e: Exception) {
                _operacion.value = EstadoOperacion.Error(e.message ?: "No se pudo verificar el correo")
            }
        }
    }

    // ---------- Etapa 2 (CU-02): código de verificación ----------

    private fun verificarCodigo() {
        val correcto = _uiState.value.codigo == CODIGO_DEMO
        _uiState.update {
            if (correcto) it.copy(pasoActual = 3, errorCodigo = null)
            else it.copy(errorCodigo = "Código incorrecto")
        }
    }

    // ---------- Etapa 3 (CU-03): datos personales y dirección ----------

    private fun crearCuenta() {
        val r = _uiState.value
        val revisado = r.copy(
            errorRut = if (esRutValido(r.rut)) null else "RUT inválido (verifica el dígito verificador)",
            errorNombres = if (r.nombres.isNotBlank()) null else "Obligatorio",
            errorApellido = if (r.apellido.isNotBlank()) null else "Obligatorio",
            errorRegion = if (r.region != null) null else "Selecciona una región",
            // CU-03: la comuna debe pertenecer a la región elegida
            errorComuna = if (r.comuna != null && r.comuna in REGIONES[r.region].orEmpty()) null
                else "Selecciona una comuna",
            errorCalle = if (r.calle.isNotBlank()) null else "Obligatorio",
            errorNumero = if (r.numero.isNotBlank()) null else "Obligatorio"
        )
        _uiState.value = revisado

        val region = revisado.region
        val comuna = revisado.comuna
        val valido = sinErrores(
            revisado.errorRut, revisado.errorNombres, revisado.errorApellido, revisado.errorRegion,
            revisado.errorComuna, revisado.errorCalle, revisado.errorNumero
        )
        // Después de este if, Kotlin sabe que region y comuna no son null (smart cast)
        if (!valido || region == null || comuna == null) return

        viewModelScope.launch {
            _operacion.value = EstadoOperacion.Cargando
            try {
                // usuarioId = 0 es provisional: UsuarioDao.registrarCuenta lo reemplaza
                // por el id real que Room genera al insertar el usuario
                usuarioRepository.registrar(
                    correo = revisado.correo,
                    contrasenia = revisado.contrasena,
                    perfil = PerfilUsuario(
                        usuarioId = 0,
                        rut = revisado.rut.trim(),
                        nombre = revisado.nombres.trim(),
                        apellido = revisado.apellido.trim()
                    ),
                    direccion = Direccion(
                        usuarioId = 0,
                        region = region,
                        comuna = comuna,
                        calle = revisado.calle.trim(),
                        numero = revisado.numero.trim(),
                        departamento = revisado.departamento.trim().ifBlank { null } // opcional
                    )
                )
                // La sesión ya quedó guardada: AppNavegacion lleva al catálogo
                _operacion.value = EstadoOperacion.Exito
            } catch (e: Exception) {
                // Ej.: "El correo ya está registrado" si alguien lo usó mientras tanto
                _operacion.value = EstadoOperacion.Error(e.message ?: "No se pudo crear la cuenta")
            }
        }
    }

    // vararg: recibe cualquier cantidad de errores (como String... en Java)
    private fun sinErrores(vararg errores: String?): Boolean = errores.all { it == null }

    private companion object {
        const val MIN_CONTRASENA = 6
        const val CODIGO_DEMO = "1234" // CU-02: en el MVP el código es fijo
    }
}
