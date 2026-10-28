package com.example.artesaniasaleo.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artesaniasaleo.model.CatalogoUiState
import com.example.artesaniasaleo.model.CategoriaUi
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.repository.CategoriaRepository
import com.example.artesaniasaleo.repository.FavoritoRepository
import com.example.artesaniasaleo.repository.ProductoRepository
import com.example.artesaniasaleo.repository.UsuarioRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// CU-12 a CU-19: catálogo, búsqueda, categorías, detalle y favoritos.
// Un solo ViewModel compartido por esas pantallas (Guía 11): todas leen la misma lista de Room,
// así un favorito marcado en el detalle aparece marcado en el catálogo sin hacer nada más.
class CatalogoViewModel(
    productoRepository: ProductoRepository,
    categoriaRepository: CategoriaRepository,
    private val favoritoRepository: FavoritoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // Lo que escribe el usuario en el buscador. Va aparte (no dentro del combine) para que el
    // TextField se actualice al instante, sin esperar a que se recalcule toda la lista.
    private val _busqueda = MutableStateFlow("")
    val busqueda: StateFlow<String> = _busqueda.asStateFlow()

    // CU-15: orden por precio dentro de una categoría (true = menor a mayor)
    private val _ordenAscendente = MutableStateFlow(true)
    val ordenAscendente: StateFlow<Boolean> = _ordenAscendente.asStateFlow()

    // Cargando hasta que Room entregue la primera lista; Error si algo falla (CU-12, excepción)
    private val _carga = MutableStateFlow<EstadoOperacion>(EstadoOperacion.Cargando)
    val carga: StateFlow<EstadoOperacion> = _carga.asStateFlow()

    // Ids de los favoritos del usuario con sesión. Si cambia la sesión, cambia la consulta.
    @OptIn(ExperimentalCoroutinesApi::class)
    private val idsFavoritos: Flow<Set<Int>> = usuarioRepository.usuarioIdSesion
        .flatMapLatest { id ->
            if (id == null) flowOf(emptyList<Int>()) else favoritoRepository.observarIdsFavoritos(id)
        }
        .map { it.toSet() }

    // combine junta tres Flow de Room en un solo estado. Cada vez que cambia cualquiera
    // (un producto nuevo, un favorito, una categoría), se recalcula y la pantalla se redibuja.
    val uiState: StateFlow<CatalogoUiState> = combine(
        productoRepository.observarCatalogo(),
        idsFavoritos,
        categoriaRepository.observarTodas()
    ) { productos, favoritos, categorias ->
        CatalogoUiState(
            productos = productos.map { it.aProductoUi(favoritos) }, // conversión en Mapeos.kt
            categorias = categorias.map { CategoriaUi(id = it.id, nombre = it.nombre) }
        )
    }
        .onEach { _carga.value = EstadoOperacion.Exito }
        .catch { error ->
            Log.e(TAG, "Error al cargar el catálogo", error)
            _carga.value = EstadoOperacion.Error("No se pudo cargar el catálogo")
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CatalogoUiState()
        )

    fun onBusquedaChange(texto: String) {
        _busqueda.value = texto
    }

    fun cambiarOrden() {
        _ordenAscendente.value = !_ordenAscendente.value
    }

    // CU-17 y CU-18: marcar o desmarcar. Solo se escribe en Room; la lista se actualiza sola.
    fun alternarFavorito(productoId: Int) {
        viewModelScope.launch {
            try {
                val usuarioId = usuarioRepository.usuarioIdSesion.first() ?: return@launch
                val esFavoritoActual = uiState.value.producto(productoId)?.esFavorito ?: false
                favoritoRepository.alternar(usuarioId, productoId, esFavoritoActual)
            } catch (e: Exception) {
                // Si falla, el corazón simplemente no cambia: la pantalla siempre muestra lo que hay en Room
                Log.e(TAG, "No se pudo actualizar el favorito $productoId", e)
            }
        }
    }

    private companion object {
        const val TAG = "CatalogoViewModel"
    }
}
