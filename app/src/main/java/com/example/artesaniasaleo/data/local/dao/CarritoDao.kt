package com.example.artesaniasaleo.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.artesaniasaleo.data.local.entity.CarritoItem
import com.example.artesaniasaleo.data.local.relation.CarritoItemConProducto
import kotlinx.coroutines.flow.Flow

@Dao
interface CarritoDao {

    @Transaction
    @Query("SELECT * FROM carrito_item WHERE usuarioId = :usuarioId")
    fun observarCarrito(usuarioId: Int): Flow<List<CarritoItemConProducto>>

    @Query("SELECT * FROM carrito_item WHERE usuarioId = :usuarioId AND productoId = :productoId")
    suspend fun buscarItem(usuarioId: Int, productoId: Int): CarritoItem?

    @Query("SELECT COALESCE(SUM(cantidad), 0) FROM carrito_item WHERE usuarioId = :usuarioId")
    fun observarCantidadTotal(usuarioId: Int): Flow<Int>

    @Query(
        """
        SELECT COALESCE(SUM(c.cantidad * p.precio), 0)
        FROM carrito_item c
        INNER JOIN producto p ON p.id = c.productoId
        WHERE c.usuarioId = :usuarioId
        """
    )
    fun observarTotal(usuarioId: Int): Flow<Int>

    // Control de stock: cuántas unidades quedan de un producto (null = el producto no existe)
    @Query("SELECT stock FROM producto WHERE id = :productoId")
    suspend fun obtenerStock(productoId: Int): Int?

    @Upsert
    suspend fun guardar(item: CarritoItem)

    @Transaction
    suspend fun agregar(usuarioId: Int, productoId: Int, cantidad: Int) {
        val existente = buscarItem(usuarioId, productoId)
        if (existente == null) {
            guardar(CarritoItem(usuarioId = usuarioId, productoId = productoId, cantidad = cantidad))
        } else {
            guardar(existente.copy(cantidad = existente.cantidad + cantidad))
        }
    }

    @Query("UPDATE carrito_item SET cantidad = :cantidad WHERE usuarioId = :usuarioId AND productoId = :productoId")
    suspend fun actualizarCantidad(usuarioId: Int, productoId: Int, cantidad: Int)

    @Query("DELETE FROM carrito_item WHERE usuarioId = :usuarioId AND productoId = :productoId")
    suspend fun eliminar(usuarioId: Int, productoId: Int)

    @Query("DELETE FROM carrito_item WHERE usuarioId = :usuarioId")
    suspend fun vaciar(usuarioId: Int)
}