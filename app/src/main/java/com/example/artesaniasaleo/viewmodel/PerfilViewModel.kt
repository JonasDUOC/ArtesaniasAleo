package com.example.artesaniasaleo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artesaniasaleo.data.local.entity.Direccion
import com.example.artesaniasaleo.data.local.entity.PerfilUsuario
import com.example.artesaniasaleo.data.local.entity.Tarjeta
import com.example.artesaniasaleo.data.local.entity.Usuario
import com.example.artesaniasaleo.model.DatosPagoUiState
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.PerfilUiState
import com.example.artesaniasaleo.model.REGIONES
import com.example.artesaniasaleo.repository.UsuarioRepository
import com.example.artesaniasaleo.ui.utils.esCorreoValido
import com.example.artesaniasaleo.ui.utils.tarjetaSinErrores
import com.example.artesaniasaleo.ui.utils.validarTarjeta
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// CU-08 a CU-11: perfil con tres secciones (Personal, Dirección, Pago), cada una con Editar / Guardar.
class PerfilViewModel(
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PerfilUiState())
    val uiState: StateFlow<PerfilUiState> = _uiState.asStateFlow()

    // Resultado de "Guardar cambios": Exito → "Guardado con éxito"; Error → mensaje
    private val _operacion = MutableStateFlow<EstadoOperacion>(EstadoOperacion.Inactivo)
    val operacion: StateFlow<EstadoOperacion> = _operacion.asStateFlow()

    init {
        // Escucha usuario + perfil + dirección + tarjeta de Room. Mientras el cliente está editando
        // una sección, no se pisa lo que está escribiendo.
        viewModelScope.launch {
            datosDeRoom()
                .catch { e -> Log.e(TAG, "Error al leer el perfil", e) }
                .collect { datos ->
                    _uiState.update { actual -> if (actual.seccionEditando == null) datos else actual }
                }
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    private fun datosDeRoom() = usuarioRepository.usuarioIdSesion.flatMapLatest { id ->
        if (id == null) flowOf(PerfilUiState())
        else combine(
            usuarioRepository.usuarioActual,
            usuarioRepository.observarPerfil(id),
            usuarioRepository.observarDireccion(id),
            usuarioRepository.observarTarjeta(id)
        ) { usuario, perfil, direccion, tarjeta -> armarEstado(usuario, perfil, direccion, tarjeta) }
    }

    private fun armarEstado(usuario: Usuario?, perfil: PerfilUsuario?, direccion: Direccion?, tarjeta: Tarjeta?) =
        PerfilUiState(
            rut = perfil?.rut.orEmpty(),
            nombres = perfil?.nombre.orEmpty(),
            apellido = perfil?.apellido.orEmpty(),
            telefono = perfil?.telefono.orEmpty(),
            correo = usuario?.correo.orEmpty(),
            region = direccion?.region,
            comuna = direccion?.comuna,
            calle = direccion?.calle.orEmpty(),
            numero = direccion?.numero.orEmpty(),
            departamento = direccion?.departamento.orEmpty(),
            pago = tarjeta?.let {
                DatosPagoUiState(tipo = it.tipo, numero = it.numero, vencimiento = it.vencimiento, cvv = it.cvv)
            } ?: DatosPagoUiState()
        )

    // La pantalla entrega el estado completo modificado con copy() (diseño de Maxi)
    fun onEstadoChange(nuevo: PerfilUiState) {
        _uiState.value = nuevo
    }

    // "Editar" habilita los campos de esa sección (0 Personal, 1 Dirección, 2 Pago)
    fun editar(seccion: Int) {
        _uiState.update { it.copy(seccionEditando = seccion) }
    }

    fun guardar(seccion: Int) {
        val p = _uiState.value
        val revisado = when (seccion) {
            0 -> p.copy( // CU-09: solo teléfono y correo son editables
                errorTelefono = if (p.telefono.isNotBlank()) null else "Obligatorio",
                errorCorreo = if (esCorreoValido(p.correo)) null else "Ingresa un correo válido"
            )
            1 -> p.copy( // CU-10
                errorRegion = if (p.region != null) null else "Selecciona una región",
                errorComuna = if (p.comuna != null && p.comuna in REGIONES[p.region].orEmpty()) null
                    else "Selecciona una comuna",
                errorCalle = if (p.calle.isNotBlank()) null else "Obligatorio",
                errorNumero = if (p.numero.isNotBlank()) null else "Obligatorio"
            )
            else -> p.copy(pago = validarTarjeta(p.pago)) // CU-11
        }
        _uiState.value = revisado
        val valido = when (seccion) {
            0 -> revisado.errorTelefono == null && revisado.errorCorreo == null
            1 -> listOf(revisado.errorRegion, revisado.errorComuna, revisado.errorCalle, revisado.errorNumero)
                .all { it == null }
            else -> tarjetaSinErrores(revisado.pago)
        }
        if (!valido) {
            _operacion.value = EstadoOperacion.Error("Error al guardar: revisa los campos")
            return
        }

        viewModelScope.launch {
            try {
                val id = usuarioRepository.usuarioIdSesion.first() ?: throw IllegalStateException("No hay sesión")
                when (seccion) {
                    0 -> {
                        usuarioRepository.guardarPerfil(
                            PerfilUsuario(
                                usuarioId = id,
                                rut = revisado.rut,
                                nombre = revisado.nombres,
                                apellido = revisado.apellido,
                                telefono = revisado.telefono.trim()
                            )
                        )
                        usuarioRepository.cambiarCorreo(id, revisado.correo)
                    }
                    1 -> usuarioRepository.guardarDireccion(
                        Direccion(
                            usuarioId = id,
                            region = revisado.region.orEmpty(),
                            comuna = revisado.comuna.orEmpty(),
                            calle = revisado.calle.trim(),
                            numero = revisado.numero.trim(),
                            departamento = revisado.departamento.trim().ifBlank { null }
                        )
                    )
                    else -> usuarioRepository.guardarTarjeta(
                        Tarjeta(
                            usuarioId = id,
                            tipo = revisado.pago.tipo,
                            numero = revisado.pago.numero,
                            vencimiento = revisado.pago.vencimiento,
                            cvv = revisado.pago.cvv
                        )
                    )
                }
                // Deshabilita de nuevo los campos (CU-09/10/11, paso final)
                _uiState.update { it.copy(seccionEditando = null) }
                _operacion.value = EstadoOperacion.Exito
            } catch (e: Exception) {
                _operacion.value = EstadoOperacion.Error(e.message ?: "Error al guardar")
            }
        }
    }

    fun operacionMostrada() {
        _operacion.value = EstadoOperacion.Inactivo
    }

    private companion object {
        const val TAG = "PerfilViewModel"
    }
}
