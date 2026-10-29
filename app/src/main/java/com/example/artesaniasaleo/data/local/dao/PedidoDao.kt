package com.example.artesaniasaleo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.artesaniasaleo.data.local.entity.CarritoItem
import com.example.artesaniasaleo.data.local.entity.Pedido
import com.example.artesaniasaleo.data.local.entity.PedidoDetalle
import com.example.artesaniasaleo.data.local.entity.Producto
import com.example.artesaniasaleo.data.local.relation.PedidoConDetalles
import kotlinx.coroutines.flow.Flow

@Dao
interface PedidoDao {

    @Insert
    suspend fun insertarPedido(pedido: Pedido): Long

    @Insert
    suspend fun insertarDetalles(detalles: List<PedidoDetalle>)

    @Query("SELECT * FROM carrito_item WHERE usuarioId = :usuarioId")
    suspend fun obtenerItemsCarrito(usuarioId: Int): List<CarritoItem>

    @Query("SELECT * FROM producto WHERE id IN (:ids)")
    suspend fun obtenerProductos(ids: List<Int>): List<Producto>

    @Query("DELETE FROM carrito_item WHERE usuarioId = :usuarioId")
    suspend fun vaciarCarrito(usuarioId: Int)

    // Control de stock: resta lo vendido (se llama solo dentro de confirmarCompra)
    @Query("UPDATE producto SET stock = stock - :cantidad WHERE id = :productoId")
    suspend fun descontarStock(productoId: Int, cantidad: Int)

    @Transaction
    suspend fun confirmarCompra(usuarioId: Int): Int {
        val items = obtenerItemsCarrito(usuarioId)
        if (items.isEmpty()) {
            throw IllegalStateException("El carrito está vacío")
        }
        val productos = obtenerProductos(items.map { it.productoId }).associateBy { it.id }
        // Control de stock: se revisa ANTES de escribir. Si un producto no alcanza, la excepción
        // corta la transacción y no se crea el pedido ni se descuenta nada (todo o nada)
        items.forEach { item ->
            val producto = productos.getValue(item.productoId)
            if (item.cantidad > producto.stock) {
                throw IllegalStateException(
                    "Sin stock suficiente de ${producto.titulo} (quedan ${producto.stock})"
                )
            }
        }
        val detalles = items.map { item ->
            PedidoDetalle(
                pedidoId = 0,
                productoId = item.productoId,
                cantidad = item.cantidad,
                precioUnitario = productos.getValue(item.productoId).precio
            )
        }
        val total = detalles.sumOf { it.cantidad * it.precioUnitario }
        val pedidoId = insertarPedido(Pedido(usuarioId = usuarioId, total = total)).toInt()
        insertarDetalles(detalles.map { it.copy(pedidoId = pedidoId) })
        items.forEach { descontarStock(it.productoId, it.cantidad) }
        vaciarCarrito(usuarioId)
        return pedidoId
    }

    @Transaction
    @Query("SELECT * FROM pedido WHERE usuarioId = :usuarioId ORDER BY fecha DESC")
    fun observarPedidosDeUsuario(usuarioId: Int): Flow<List<PedidoConDetalles>>

    @Transaction
    @Query("SELECT * FROM pedido ORDER BY fecha DESC")
    fun observarTodos(): Flow<List<PedidoConDetalles>>

    @Transaction
    @Query("SELECT * FROM pedido WHERE id = :id")
    fun observarPorId(id: Int): Flow<PedidoConDetalles?>

    @Query("SELECT * FROM pedido WHERE estado = 'Confirmación de pago' ORDER BY fecha DESC")
    fun observarPendientes(): Flow<List<Pedido>>

    @Query("UPDATE pedido SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String)

    @Query("SELECT COALESCE(SUM(total), 0) FROM pedido WHERE fecha >= :desde")
    fun observarVentasDesde(desde: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM pedido WHERE fecha >= :desde")
    fun observarCantidadPedidosDesde(desde: Long): Flow<Int>
}