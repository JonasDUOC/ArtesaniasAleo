package com.example.artesaniasaleo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artesaniasaleo.model.EstadoPedido
import com.example.artesaniasaleo.model.PedidoUi
import com.example.artesaniasaleo.repository.PedidoRepository
import com.example.artesaniasaleo.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// Cliente: CU-25 y CU-26 (seguimiento). Administrador: CU-35 a CU-38 (notificaciones y estados)
// y CU-40 (resumen). Todo se lee de las mismas tablas pedido / pedido_detalle.
class PedidosViewModel(
    private val pedidoRepository: PedidoRepository,
    usuarioRepository: UsuarioRepository
) : ViewModel() {

    // Pedidos del cliente con sesión (seguimiento)
    @OptIn(ExperimentalCoroutinesApi::class)
    val misPedidos: StateFlow<List<PedidoUi>> = usuarioRepository.usuarioIdSesion
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList<PedidoUi>())
            else pedidoRepository.observarMisPedidos(id).map { lista -> lista.map { it.aPedidoUi() } }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Todos los pedidos, para las notificaciones del administrador
    val todos: StateFlow<List<PedidoUi>> = pedidoRepository.observarTodos()
        .map { lista -> lista.map { it.aPedidoUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Pedidos en "Confirmación de pago": el número del menú y del resumen (CU-35)
    val pendientes: StateFlow<Int> = pedidoRepository.observarPendientes()
        .map { it.size }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    // Total vendido hoy (CU-40)
    val ventasHoy: StateFlow<Int> = pedidoRepository.observarVentasDeHoy()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

    fun pedido(id: Int): PedidoUi? =
        todos.value.find { it.id == id } ?: misPedidos.value.find { it.id == id }

    // CU-37 y CU-38: "En proceso" → En preparación; "Enviado" → Enviado.
    // El cliente lo ve en su seguimiento porque lee la misma tabla (CU-26).
    fun cambiarEstado(pedidoId: Int, nuevo: EstadoPedido) {
        viewModelScope.launch {
            try {
                pedidoRepository.cambiarEstado(pedidoId, nuevo)
            } catch (e: Exception) {
                Log.e("PedidosViewModel", "No se pudo cambiar el estado del pedido $pedidoId", e)
            }
        }
    }
}
