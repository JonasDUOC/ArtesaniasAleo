package com.example.artesaniasaleo.viewmodel

import androidx.lifecycle.ViewModel
import com.example.artesaniasaleo.model.EstadoQr
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Interpreta el texto que lee ML Kit. No necesita repository: por eso se crea con viewModel()
// sin la Factory. La pantalla solo muestra la cámara; aquí se decide qué significa el código.
class QrViewModel : ViewModel() {

    private val _estado = MutableStateFlow<EstadoQr>(EstadoQr.Escaneando)
    val estado: StateFlow<EstadoQr> = _estado.asStateFlow()
    // La cámara entrega varios cuadros por segundo: solo se procesa el primer código
    // mientras se está escaneando, así no se navega dos veces.
    fun onCodigoLeido(
        texto: String,
        existeProducto: (Int) -> Boolean,
        existePedido: (Int) -> Boolean
    ) {
        if (_estado.value != EstadoQr.Escaneando) return
        // Formato de nuestras etiquetas: "tipo:numero", por ejemplo "producto:3"
        val partes = texto.trim().lowercase().split(":")
        val id = partes.getOrNull(1)?.toIntOrNull()
        _estado.value = when {
            partes.size != 2 || id == null -> EstadoQr.NoReconocido
            partes[0] == "producto" && existeProducto(id) -> EstadoQr.Producto(id)
            partes[0] == "pedido" && existePedido(id) -> EstadoQr.Pedido(id)
            else -> EstadoQr.NoReconocido
        }
    }

    // "Intentar de nuevo" o volver a abrir la pantalla: la cámara vuelve a buscar
    fun reiniciar() {
        _estado.value = EstadoQr.Escaneando
    }
}
