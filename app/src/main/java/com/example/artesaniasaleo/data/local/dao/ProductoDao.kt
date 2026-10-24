package com.example.artesaniasaleo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.artesaniasaleo.data.local.entity.ImagenProducto
import com.example.artesaniasaleo.data.local.entity.Producto
import com.example.artesaniasaleo.data.local.relation.ProductoConImagenes
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Transaction
    @Query("SELECT * FROM producto WHERE estado = 'activo' ORDER BY id DESC")
    fun observarActivos(): Flow<List<ProductoConImagenes>>

    @Transaction
    @Query(
        """
        SELECT * FROM producto
        WHERE estado = 'activo' AND categoriaId = :categoriaId
        ORDER BY CASE WHEN :ascendente = 1 THEN precio ELSE -precio END
        """
    )
    fun observarPorCategoria(categoriaId: Int, ascendente: Boolean): Flow<List<ProductoConImagenes>>

    @Transaction
    @Query(
        """
        SELECT * FROM producto
        WHERE estado = 'activo'
        AND (titulo LIKE '%' || :texto || '%' OR descripcion LIKE '%' || :texto || '%')
        ORDER BY titulo ASC
        """
    )
    fun buscar(texto: String): Flow<List<ProductoConImagenes>>

    @Transaction
    @Query("SELECT * FROM producto WHERE id = :id")
    fun observarPorId(id: Int): Flow<ProductoConImagenes?>

    @Transaction
    @Query("SELECT * FROM producto ORDER BY id DESC")
    fun observarTodosAdmin(): Flow<List<ProductoConImagenes>>

    @Query("SELECT * FROM imagen_producto WHERE productoId = :productoId ORDER BY orden ASC")
    suspend fun obtenerImagenes(productoId: Int): List<ImagenProducto>

    @Insert
    suspend fun insertarProducto(producto: Producto): Long

    @Update
    suspend fun actualizarProducto(producto: Producto)

    @Query("UPDATE producto SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String)

    @Insert
    suspend fun insertarImagenes(imagenes: List<ImagenProducto>)

    @Query("DELETE FROM imagen_producto WHERE productoId = :productoId")
    suspend fun eliminarImagenes(productoId: Int)

    @Query("DELETE FROM imagen_producto WHERE id = :id")
    suspend fun eliminarImagen(id: Int)

    @Transaction
    suspend fun crearProductoConImagenes(producto: Producto, rutas: List<String>): Int {
        val nuevoId = insertarProducto(producto).toInt()
        insertarImagenes(
            rutas.mapIndexed { indice, ruta ->
                ImagenProducto(productoId = nuevoId, url = ruta, orden = indice)
            }
        )
        return nuevoId
    }

    @Transaction
    suspend fun actualizarProductoConImagenes(producto: Producto, rutas: List<String>) {
        actualizarProducto(producto)
        eliminarImagenes(producto.id)
        insertarImagenes(
            rutas.mapIndexed { indice, ruta ->
                ImagenProducto(productoId = producto.id, url = ruta, orden = indice)
            }
        )
    }

    @Query("SELECT COUNT(*) FROM pedido_detalle WHERE productoId = :productoId")
    suspend fun contarVentas(productoId: Int): Int

    @Delete
    suspend fun eliminarProducto(producto: Producto)
}