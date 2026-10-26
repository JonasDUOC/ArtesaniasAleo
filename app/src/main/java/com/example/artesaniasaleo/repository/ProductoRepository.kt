package com.example.artesaniasaleo.repository

import android.content.Context
import android.net.Uri
import com.example.artesaniasaleo.data.local.dao.ProductoDao
import com.example.artesaniasaleo.data.local.entity.Producto
import com.example.artesaniasaleo.data.local.relation.ProductoConImagenes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.util.UUID

class ProductoRepository(
    private val productoDao: ProductoDao,
    private val context: Context
) {

    fun observarCatalogo(): Flow<List<ProductoConImagenes>> = productoDao.observarActivos()

    fun observarPorCategoria(categoriaId: Int, ascendente: Boolean): Flow<List<ProductoConImagenes>> =
        productoDao.observarPorCategoria(categoriaId, ascendente)

    fun buscar(texto: String): Flow<List<ProductoConImagenes>> = productoDao.buscar(texto.trim())

    fun observarDetalle(productoId: Int): Flow<ProductoConImagenes?> = productoDao.observarPorId(productoId)

    fun observarTodosAdmin(): Flow<List<ProductoConImagenes>> = productoDao.observarTodosAdmin()

    suspend fun crear(producto: Producto, imagenes: List<Uri>): Int {
        validar(producto, imagenes)
        val rutas = copiarImagenes(imagenes)
        return productoDao.crearProductoConImagenes(producto, rutas)
    }

    suspend fun actualizar(producto: Producto, imagenes: List<Uri>) {
        validar(producto, imagenes)
        val rutasAnteriores = productoDao.obtenerImagenes(producto.id).map { it.url }
        val rutasNuevas = copiarImagenes(imagenes)
        productoDao.actualizarProductoConImagenes(producto, rutasNuevas)
        borrarArchivos(rutasAnteriores)
    }

    suspend fun eliminar(producto: Producto) {
        if (productoDao.contarVentas(producto.id) > 0) {
            productoDao.actualizarEstado(producto.id, "inactivo")
        } else {
            val rutas = productoDao.obtenerImagenes(producto.id).map { it.url }
            productoDao.eliminarProducto(producto)
            borrarArchivos(rutas)
        }
    }

    suspend fun activar(productoId: Int) = productoDao.actualizarEstado(productoId, "activo")

    private fun validar(producto: Producto, imagenes: List<Uri>) {
        if (producto.titulo.isBlank()) {
            throw IllegalArgumentException("El título es obligatorio")
        }
        if (producto.precio <= 0) {
            throw IllegalArgumentException("El precio debe ser mayor a 0")
        }
        if (imagenes.isEmpty()) {
            throw IllegalArgumentException("Agrega al menos una imagen")
        }
    }

    private suspend fun copiarImagenes(uris: List<Uri>): List<String> = withContext(Dispatchers.IO) {
        val carpeta = File(context.filesDir, "productos").apply { mkdirs() }
        uris.map { uri ->
            val destino = File(carpeta, "${UUID.randomUUID()}.jpg")
            context.contentResolver.openInputStream(uri)?.use { entrada ->
                destino.outputStream().use { salida -> entrada.copyTo(salida) }
            } ?: throw IOException("No se pudo leer la imagen")
            destino.absolutePath
        }
    }

    private suspend fun borrarArchivos(rutas: List<String>) {
        withContext(Dispatchers.IO) {
            rutas.forEach { ruta -> File(ruta).delete() }
        }
    }
}