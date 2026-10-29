package com.example.artesaniasaleo.ui.screens.admin

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.artesaniasaleo.model.CATEGORIAS_DEMO
import com.example.artesaniasaleo.model.CategoriaFormUiState
import com.example.artesaniasaleo.model.CategoriaUi
import com.example.artesaniasaleo.model.PRODUCTOS_DEMO
import com.example.artesaniasaleo.model.ProductoFormUiState
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.ui.components.BarraSuperior
import com.example.artesaniasaleo.ui.components.BotonPrincipal
import com.example.artesaniasaleo.ui.components.CampoSelector
import com.example.artesaniasaleo.ui.components.CampoTexto
import com.example.artesaniasaleo.ui.components.FilaLista
import com.example.artesaniasaleo.ui.components.SelectorImagenes
import com.example.artesaniasaleo.ui.theme.ArtesaniasAleoTheme

// Dos pestañas: categorías y productos, cada una con su lista y botón "Nuevo"
@OptIn(ExperimentalMaterial3Api::class) // PrimaryTabRow puede estar marcado como experimental
@Composable
fun PantallaGestionContenido(
    categorias: List<CategoriaUi>,
    productos: List<ProductoUi>,
    onEditarCategoria: (Int?) -> Unit,   // null = nueva
    onEliminarCategoria: (Int) -> Unit,
    onEditarProducto: (Int?) -> Unit,
    onEliminarProducto: (Int) -> Unit,
    onInicio: () -> Unit,
    onMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Pestaña y texto de búsqueda son solo visuales: viven en la pantalla
    var pestana by rememberSaveable { mutableIntStateOf(0) }
    var busqueda by rememberSaveable { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = { BarraSuperior(titulo = "Gestión de contenido", esPantallaRaiz = true, onIconoIzquierdo = onInicio, onMenu = onMenu) }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
        ) {
            PrimaryTabRow(selectedTabIndex = pestana, containerColor = MaterialTheme.colorScheme.background) {
                Tab(selected = pestana == 0, onClick = { pestana = 0 }, text = { Text("Categorías") })
                Tab(selected = pestana == 1, onClick = { pestana = 1 }, text = { Text("Productos") })
            }
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (pestana == 0) {
                    categorias.forEach { categoria ->
                        FilaLista(texto = categoria.nombre) {
                            AccionesFila({ onEditarCategoria(categoria.id) }, { onEliminarCategoria(categoria.id) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    BotonPrincipal(texto = "+ Nueva categoría", onClick = { onEditarCategoria(null) })
                } else {
                    OutlinedTextField(
                        value = busqueda,
                        onValueChange = { busqueda = it },
                        placeholder = { Text("Buscar por producto o categoría…") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    // Busca en el nombre del producto o en el de su categoría
                    val filtrados = productos.filter { producto ->
                        val categoria = categorias.find { it.id == producto.categoriaId }?.nombre.orEmpty()
                        producto.nombre.contains(busqueda, ignoreCase = true) || categoria.contains(busqueda, ignoreCase = true)
                    }
                    if (filtrados.isEmpty()) {
                        Text("Sin resultados", style = MaterialTheme.typography.bodySmall, modifier = Modifier.padding(top = 12.dp))
                    }
                    filtrados.forEach { producto ->
                        val textoStock = if (producto.agotado) "Agotado" else "Stock: ${producto.stock}"
                        FilaLista(texto = "${producto.nombre}\n$textoStock") {
                            AccionesFila({ onEditarProducto(producto.id) }, { onEliminarProducto(producto.id) })
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                    BotonPrincipal(texto = "+ Nuevo producto", onClick = { onEditarProducto(null) })
                }
            }
        }
    }
}

// Botones "Editar" y "Eliminar" (este último en rojo) a la derecha de cada fila
@Composable
private fun AccionesFila(onEditar: () -> Unit, onEliminar: () -> Unit) {
    Row {
        TextButton(onClick = onEditar) { Text("Editar") }
        TextButton(onClick = onEliminar) { Text("Eliminar", color = MaterialTheme.colorScheme.error) }
    }
}

// Formulario para crear o editar una categoría
@Composable
fun PantallaFormCategoria(
    estado: CategoriaFormUiState,
    onNombreChange: (String) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = if (estado.id == null) "Nueva categoría" else "Editar categoría",
                esPantallaRaiz = false, onIconoIzquierdo = onVolver, onMenu = {}, mostrarMenu = false
            )
        }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            CampoTexto(
                etiqueta = "Nombre de la categoría",
                valor = estado.nombre,
                onValorChange = onNombreChange,
                mensajeError = estado.errorNombre
            )
            Spacer(Modifier.height(16.dp))
            BotonPrincipal(texto = "Guardar", onClick = onGuardar)
        }
    }
}

// Formulario para crear o editar un producto
@Composable
fun PantallaFormProducto(
    estado: ProductoFormUiState,
    categorias: List<CategoriaUi>,
    onEstadoChange: (ProductoFormUiState) -> Unit,
    onGuardar: () -> Unit,
    onVolver: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            BarraSuperior(
                titulo = if (estado.id == null) "Nuevo producto" else "Editar producto",
                esPantallaRaiz = false, onIconoIzquierdo = onVolver, onMenu = {}, mostrarMenu = false
            )
        }
    ) { espacioBarra ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(espacioBarra)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 16.dp)
        ) {
            CampoTexto(
                etiqueta = "Título",
                valor = estado.nombre,
                onValorChange = { onEstadoChange(estado.copy(nombre = it)) },
                mensajeError = estado.errorNombre
            )
            Spacer(Modifier.height(8.dp))
            CampoTexto(
                etiqueta = "Descripción",
                valor = estado.descripcion,
                onValorChange = { onEstadoChange(estado.copy(descripcion = it)) }
            )
            Spacer(Modifier.height(8.dp))
            CampoTexto(
                etiqueta = "Precio",
                valor = estado.precio,
                onValorChange = { onEstadoChange(estado.copy(precio = it.filter(Char::isDigit))) },
                placeholder = "0",
                mensajeError = estado.errorPrecio,
                tipoTeclado = KeyboardType.Number
            )
            Spacer(Modifier.height(8.dp))
            // Control de stock: unidades disponibles (0 = agotado; el cliente no puede comprarlo)
            CampoTexto(
                etiqueta = "Stock (unidades)",
                valor = estado.stock,
                onValorChange = { onEstadoChange(estado.copy(stock = it.filter(Char::isDigit), errorStock = null)) },
                placeholder = "0",
                mensajeError = estado.errorStock,
                tipoTeclado = KeyboardType.Number
            )
            Spacer(Modifier.height(8.dp))
            // El selector muestra nombres; al elegir se guarda el id de la categoría
            CampoSelector(
                etiqueta = "Categoría",
                opciones = categorias.map { it.nombre },
                seleccionada = categorias.find { it.id == estado.categoriaId }?.nombre,
                onSeleccionar = { nombre ->
                    onEstadoChange(estado.copy(categoriaId = categorias.find { it.nombre == nombre }?.id))
                },
                mensajeError = estado.errorCategoria
            )
            Spacer(Modifier.height(12.dp))
            // CU-32: una o varias imágenes desde la galería o la cámara (se muestran como carrusel)
            SelectorImagenes(
                imagenes = estado.imagenes,
                mensajeError = estado.errorImagenes,
                onAgregar = { nuevas ->
                    onEstadoChange(estado.copy(imagenes = estado.imagenes + nuevas, errorImagenes = null))
                },
                onQuitar = { uri -> onEstadoChange(estado.copy(imagenes = estado.imagenes - uri)) }
            )
            Spacer(Modifier.height(16.dp))
            BotonPrincipal(texto = "Guardar", onClick = onGuardar)
        }
    }
}

@Preview(name = "Gestión de contenido", showBackground = true)
@Composable
private fun VistaGestion() {
    ArtesaniasAleoTheme {
        PantallaGestionContenido(
            categorias = CATEGORIAS_DEMO, productos = PRODUCTOS_DEMO,
            onEditarCategoria = {}, onEliminarCategoria = {}, onEditarProducto = {}, onEliminarProducto = {},
            onInicio = {}, onMenu = {}
        )
    }
}

@Preview(name = "Formulario de producto", showBackground = true)
@Composable
private fun VistaFormProducto() {
    ArtesaniasAleoTheme {
        PantallaFormProducto(
            estado = ProductoFormUiState(nombre = "Jarrón de greda", precio = "18500", stock = "5", categoriaId = 1),
            categorias = CATEGORIAS_DEMO, onEstadoChange = {}, onGuardar = {}, onVolver = {}
        )
    }
}
