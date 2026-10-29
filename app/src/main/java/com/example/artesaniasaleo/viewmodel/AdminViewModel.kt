package com.example.artesaniasaleo.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.artesaniasaleo.data.local.entity.Categoria
import com.example.artesaniasaleo.data.local.entity.Producto
import com.example.artesaniasaleo.data.local.relation.ProductoConImagenes
import com.example.artesaniasaleo.model.CategoriaFormUiState
import com.example.artesaniasaleo.model.CategoriaUi
import com.example.artesaniasaleo.model.EstadoOperacion
import com.example.artesaniasaleo.model.ProductoFormUiState
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.model.UsuarioUi
import com.example.artesaniasaleo.repository.CategoriaRepository
import com.example.artesaniasaleo.repository.ProductoRepository
import com.example.artesaniasaleo.repository.UsuarioRepository
import com.example.artesaniasaleo.ui.utils.esStockValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File

// Panel de administración: gestión de contenido (CU-28 a CU-34) y control de usuarios (CU-39).
// Compartido por Gestión, los formularios de categoría y producto, Usuarios y Editar usuario.
class AdminViewModel(
    private val categoriaRepository: CategoriaRepository,
    private val productoRepository: ProductoRepository,
    private val usuarioRepository: UsuarioRepository
) : ViewModel() {

    // ---------- Datos de Room ----------

    // Se guardan las entidades porque el repository las necesita para renombrar o eliminar
    private val categoriasEntidad: StateFlow<List<Categoria>> = categoriaRepository.observarTodas()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val categorias: StateFlow<List<CategoriaUi>> = categoriasEntidad
        .map { lista -> lista.map { CategoriaUi(id = it.id, nombre = it.nombre) } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // Todos los productos (también los inactivos), con sus imágenes
    private val productosEntidad: StateFlow<List<ProductoConImagenes>> = productoRepository.observarTodosAdmin()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val productos: StateFlow<List<ProductoUi>> = productosEntidad
        .map { lista -> lista.map { it.aProductoUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val usuarios: StateFlow<List<UsuarioUi>> = usuarioRepository.observarTodosConPerfil()
        .map { lista -> lista.map { it.aUsuarioUi() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    // ---------- Formularios ----------

    private val _formCategoria = MutableStateFlow(CategoriaFormUiState())
    val formCategoria: StateFlow<CategoriaFormUiState> = _formCategoria.asStateFlow()

    private val _formProducto = MutableStateFlow(ProductoFormUiState())
    val formProducto: StateFlow<ProductoFormUiState> = _formProducto.asStateFlow()

    // Exito = el formulario se guardó (la pantalla vuelve atrás)
    private val _operacion = MutableStateFlow<EstadoOperacion>(EstadoOperacion.Inactivo)
    val operacion: StateFlow<EstadoOperacion> = _operacion.asStateFlow()

    // Mensaje corto para un Toast (ej.: "Categoría eliminada"); null = nada que mostrar
    private val _mensaje = MutableStateFlow<String?>(null)
    val mensaje: StateFlow<String?> = _mensaje.asStateFlow()

    fun operacionMostrada() {
        _operacion.value = EstadoOperacion.Inactivo
    }

    fun mensajeMostrado() {
        _mensaje.value = null
    }

    // ---------- Categorías (CU-28 a CU-30) ----------

    // id = null: categoría nueva
    fun abrirFormCategoria(id: Int?) {
        val categoria = categoriasEntidad.value.find { it.id == id }
        _formCategoria.value = CategoriaFormUiState(id = categoria?.id, nombre = categoria?.nombre.orEmpty())
    }

    fun onNombreCategoriaChange(nombre: String) {
        _formCategoria.update { it.copy(nombre = nombre, errorNombre = null) }
    }

    fun guardarCategoria() {
        val f = _formCategoria.value
        if (f.nombre.isBlank()) {
            _formCategoria.update { it.copy(errorNombre = "Ingresa un nombre") }
            return
        }
        viewModelScope.launch {
            try {
                val existente = categoriasEntidad.value.find { it.id == f.id }
                if (existente == null) categoriaRepository.crear(f.nombre)
                else categoriaRepository.renombrar(existente, f.nombre)
                _mensaje.value = "Categoría guardada"
                _operacion.value = EstadoOperacion.Exito
            } catch (e: Exception) {
                // Ej.: "Ya existe una categoría con ese nombre" (CU-28 y CU-29)
                _formCategoria.update { it.copy(errorNombre = e.message) }
            }
        }
    }

    fun eliminarCategoria(id: Int) {
        val categoria = categoriasEntidad.value.find { it.id == id } ?: return
        viewModelScope.launch {
            _mensaje.value = try {
                categoriaRepository.eliminar(categoria)
                "Categoría eliminada"
            } catch (e: Exception) {
                e.message ?: "No se pudo eliminar" // CU-30: con productos no se elimina
            }
        }
    }

    // ---------- Productos (CU-32 a CU-34) ----------

    fun abrirFormProducto(id: Int?) {
        val existente = productosEntidad.value.find { it.producto.id == id }
        _formProducto.value = if (existente == null) {
            ProductoFormUiState(categoriaId = categoriasEntidad.value.firstOrNull()?.id)
        } else {
            val p = existente.producto
            ProductoFormUiState(
                id = p.id,
                nombre = p.titulo,
                descripcion = p.descripcion,
                precio = p.precio.toString(),
                stock = p.stock.toString(),
                categoriaId = p.categoriaId,
                // Las fotos ya guardadas son archivos de la app: se convierten a Uri para el formulario
                imagenes = existente.imagenes.sortedBy { it.orden }.map { Uri.fromFile(File(it.url)) }
            )
        }
    }

    // La pantalla entrega el formulario completo (incluye las imágenes de galería o cámara)
    fun onFormProductoChange(nuevo: ProductoFormUiState) {
        _formProducto.value = nuevo
    }

    fun guardarProducto() {
        val f = _formProducto.value
        val precio = f.precio.toIntOrNull() ?: 0
        val revisado = f.copy(
            errorNombre = if (f.nombre.isNotBlank()) null else "Obligatorio",
            errorPrecio = if (precio > 0) null else "Ingresa un precio válido",
            errorStock = if (esStockValido(f.stock)) null else "Ingresa el stock (0 o más)",
            errorCategoria = if (f.categoriaId != null) null else "Selecciona una categoría",
            errorImagenes = if (f.imagenes.isNotEmpty()) null else "Agrega al menos una imagen" // CU-32
        )
        _formProducto.value = revisado
        val categoriaId = revisado.categoriaId
        val hayErrores = listOf(
            revisado.errorNombre, revisado.errorPrecio, revisado.errorStock,
            revisado.errorCategoria, revisado.errorImagenes
        )
            .any { it != null }
        if (hayErrores || categoriaId == null) return

        viewModelScope.launch {
            _operacion.value = EstadoOperacion.Cargando
            try {
                val anterior = productosEntidad.value.find { it.producto.id == revisado.id }?.producto
                val producto = Producto(
                    id = revisado.id ?: 0,
                    categoriaId = categoriaId,
                    titulo = revisado.nombre.trim(),
                    descripcion = revisado.descripcion.trim(),
                    precio = precio,
                    estado = anterior?.estado ?: "activo",
                    stock = revisado.stock.trim().toInt() // ya validado con esStockValido
                )
                // El repository copia las imágenes a filesDir/productos/ y guarda sus rutas
                if (anterior == null) productoRepository.crear(producto, revisado.imagenes)
                else productoRepository.actualizar(producto, revisado.imagenes)
                _mensaje.value = "Producto guardado"
                _operacion.value = EstadoOperacion.Exito
            } catch (e: Exception) {
                _operacion.value = EstadoOperacion.Inactivo
                _mensaje.value = e.message ?: "No se pudo guardar el producto"
            }
        }
    }

    fun eliminarProducto(id: Int) {
        val producto = productosEntidad.value.find { it.producto.id == id }?.producto ?: return
        viewModelScope.launch {
            _mensaje.value = try {
                // Con ventas queda "inactivo" (no se pierde el historial); sin ventas se borra
                productoRepository.eliminar(producto)
                "Producto eliminado del catálogo"
            } catch (e: Exception) {
                e.message ?: "No se pudo eliminar el producto"
            }
        }
    }

    // ---------- Control de usuarios (CU-39) ----------

    fun usuario(id: Int): UsuarioUi? = usuarios.value.find { it.id == id }

    fun alternarBloqueo(id: Int) {
        val usuario = usuario(id) ?: return
        viewModelScope.launch {
            _mensaje.value = try {
                if (usuarioRepository.usuarioIdSesion.first() == id) {
                    "No puedes bloquear tu propia cuenta" // CU-39, excepción 1
                } else if (usuario.bloqueado) {
                    usuarioRepository.activar(id)
                    "Cuenta de ${usuario.nombre} habilitada"
                } else {
                    usuarioRepository.bloquear(id)
                    "Cuenta de ${usuario.nombre} bloqueada"
                }
            } catch (e: Exception) {
                e.message ?: "No se pudo cambiar el estado de la cuenta"
            }
        }
    }

    fun hacerAdmin(id: Int) {
        val usuario = usuario(id) ?: return
        if (usuario.esAdmin) return // CU-39, excepción 3: no se duplica el rol
        viewModelScope.launch {
            _mensaje.value = try {
                usuarioRepository.cambiarRol(id, "admin")
                "${usuario.nombre} ahora es administrador"
            } catch (e: Exception) {
                e.message ?: "No se pudo cambiar el rol"
            }
        }
    }
}
