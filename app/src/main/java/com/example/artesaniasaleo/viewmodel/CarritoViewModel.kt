package com.example.artesaniasaleo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artesaniasaleo.data.local.entity.Tarjeta
import com.example.artesaniasaleo.model.DatosPagoUiState
import com.example.artesaniasaleo.model.EstadoCompra
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.repository.CarritoRepository
import com.example.artesaniasaleo.repository.PedidoRepository
import com.example.artesaniasaleo.repository.SinTarjetaException
import com.example.artesaniasaleo.repository.UsuarioRepository
import com.example.artesaniasaleo.ui.utils.tarjetaSinErrores
import com.example.artesaniasaleo.ui.utils.validarTarjeta
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// CU-20 a CU-24: agregar al carrito, cambiar cantidades, datos de pago y confirmar la compra.
// Compartido por Detalle, Carrito, Datos de pago y el menú (contador del carrito).
class CarritoViewModel(
    private val carritoRepository: CarritoRepository,
    private val pedidoRepository: PedidoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // Ítems del carrito del usuario con sesión: (producto, cantidad). Cambia solo al cambiar la sesión.
    @OptIn(ExperimentalCoroutinesApi::class)
    val items: StateFlow<List<Pair<ProductoUi, Int>>> = usuarioRepository.usuarioIdSesion
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList<Pair<ProductoUi, Int>>())
            else carritoRepository.observarCarrito(id).map { lista ->
                lista.map { it.producto.aProductoUi() to it.item.cantidad }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Para el número rojo de "Carrito" en el menú
    @OptIn(ExperimentalCoroutinesApi::class)
    val cantidadTotal: StateFlow<Int> = usuarioRepository.usuarioIdSesion
        .flatMapLatest { id -> if (id == null) flowOf(0) else carritoRepository.observarCantidadTotal(id) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // Formulario "Datos de pago" que aparece cuando el cliente compra sin tarjeta guardada
    private val _pago = MutableStateFlow(DatosPagoUiState())
    val pago: StateFlow<DatosPagoUiState> = _pago.asStateFlow()

    private val _compra = MutableStateFlow<EstadoCompra>(EstadoCompra.Inactivo)
    val compra: StateFlow<EstadoCompra> = _compra.asStateFlow()

    // Mensaje corto para un Toast ("Agregado al carrito", "Solo quedan 2 unidades"); null = nada
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    // CU-20
    fun agregar(productoId: Int) {
        ejecutar("agregar al carrito", exito = "Agregado al carrito") { usuarioId ->
            carritoRepository.agregar(usuarioId, productoId)
        }
    }

    // CU-21 y CU-22: +1 / -1; si llega a 0, el repository lo elimina
    fun cambiarCantidad(productoId: Int, cambio: Int) {
        val actual = items.value.find { it.first.id == productoId }?.second ?: 0
        ejecutar("cambiar la cantidad") { usuarioId ->
            carritoRepository.cambiarCantidad(usuarioId, productoId, actual + cambio)
        }
    }

    // CU-24 paso 1: intenta comprar con la tarjeta guardada
    fun confirmarCompra() {
        if (_compra.value is EstadoCompra.Procesando) return
        // Si el carrito está vacío, PedidoDao.confirmarCompra lanza "El carrito está vacío" (CU-24, excepción 2)
        viewModelScope.launch {
            _compra.value = EstadoCompra.Procesando
            _compra.value = try {
                val usuarioId = usuarioIdActual()
                EstadoCompra.Confirmada(pedidoRepository.confirmarCompra(usuarioId))
            } catch (e: SinTarjetaException) {
                EstadoCompra.SinTarjeta // CU-24, excepción 1: redirigir a los datos de pago
            } catch (e: Exception) {
                EstadoCompra.Error(e.message ?: "No se pudo confirmar la compra")
            }
        }
    }

    fun onPagoChange(nuevo: DatosPagoUiState) {
        _pago.value = nuevo
    }

    // CU-24, excepción 1: guarda la tarjeta y retoma la confirmación de la compra
    fun guardarTarjetaYConfirmar() {
        val revisado = validarTarjeta(_pago.value)
        _pago.value = revisado
        if (!tarjetaSinErrores(revisado)) return
        viewModelScope.launch {
            try {
                usuarioRepository.guardarTarjeta(
                    Tarjeta(
                        usuarioId = usuarioIdActual(),
                        tipo = revisado.tipo,
                        numero = revisado.numero,
                        vencimiento = revisado.vencimiento,
                        cvv = revisado.cvv
                    )
                )
                _pago.value = DatosPagoUiState()
                confirmarCompra()
            } catch (e: Exception) {
                _compra.value = EstadoCompra.Error(e.message ?: "No se pudo guardar la tarjeta")
            }
        }
    }

    // La pantalla avisa que ya mostró el resultado (navegó o mostró el mensaje)
    fun compraMostrada() {
        _compra.value = EstadoCompra.Inactivo
    }

    private suspend fun usuarioIdActual(): Int =
        usuarioRepository.usuarioIdSesion.first() ?: throw IllegalStateException("No hay sesión iniciada")

    // Ejecuta una escritura en Room con el id del usuario.
    // Si falla (ej.: sin stock), el mensaje del repository se muestra al usuario y queda en el Logcat.
    private fun ejecutar(accion: String, exito: String? = null, bloque: suspend (Int) -> Unit) {
        viewModelScope.launch {
            try {
                bloque(usuarioIdActual())
                if (exito != null) _mensaje.value = exito
            } catch (e: Exception) {
                Log.e(TAG, "No se pudo $accion", e)
                _mensaje.value = e.message ?: "No se pudo $accion"
            }
        }
    }

    private companion object {
        const val TAG = "CarritoViewModel"
    }
}
