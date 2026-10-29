package com.example.artesaniasaleo.repository

import com.example.artesaniasaleo.data.local.dao.CarritoDao
import com.example.artesaniasaleo.data.local.relation.CarritoItemConProducto
import kotlinx.coroutines.flow.Flow

class CarritoRepository(private val carritoDao: CarritoDao) {

    fun observarCarrito(usuarioId: Int): Flow<List<CarritoItemConProducto>> =
        carritoDao.observarCarrito(usuarioId)

    fun observarCantidadTotal(usuarioId: Int): Flow<Int> = carritoDao.observarCantidadTotal(usuarioId)

    fun observarTotal(usuarioId: Int): Flow<Int> = carritoDao.observarTotal(usuarioId)

    // CU-20 + control de stock: lo que ya está en el carrito más lo nuevo no puede superar el stock
    suspend fun agregar(usuarioId: Int, productoId: Int, cantidad: Int = 1) {
        if (cantidad <= 0) {
            throw IllegalArgumentException("La cantidad debe ser mayor a 0")
        }
        val enCarrito = carritoDao.buscarItem(usuarioId, productoId)?.cantidad ?: 0
        validarStock(productoId, enCarrito + cantidad)
        carritoDao.agregar(usuarioId, productoId, cantidad)
    }

    // CU-21 y CU-22: solo al SUBIR la cantidad se revisa el stock (bajar siempre se permite,
    // así el cliente puede ajustar si el admin bajó el stock); al llegar a 0 se quita del carrito
    suspend fun cambiarCantidad(usuarioId: Int, productoId: Int, cantidad: Int) {
        if (cantidad <= 0) {
            carritoDao.eliminar(usuarioId, productoId)
            return
        }
        val actual = carritoDao.buscarItem(usuarioId, productoId)?.cantidad ?: 0
        if (cantidad > actual) {
            validarStock(productoId, cantidad)
        }
        carritoDao.actualizarCantidad(usuarioId, productoId, cantidad)
    }

    // Lanza una excepción con el mensaje para el usuario si no alcanza el stock.
    // La compra lo vuelve a revisar en PedidoDao.confirmarCompra (el admin pudo bajarlo entremedio).
    private suspend fun validarStock(productoId: Int, cantidadPedida: Int) {
        val stock = carritoDao.obtenerStock(productoId)
            ?: throw IllegalStateException("El producto ya no está disponible")
        if (cantidadPedida > stock) {
            throw IllegalStateException(mensajeSinStock(stock))
        }
    }

    private fun mensajeSinStock(stock: Int): String = when (stock) {
        0 -> "Producto agotado"
        1 -> "Solo queda 1 unidad"
        else -> "Solo quedan $stock unidades"
    }

    suspend fun quitar(usuarioId: Int, productoId: Int) = carritoDao.eliminar(usuarioId, productoId)

    suspend fun vaciar(usuarioId: Int) = carritoDao.vaciar(usuarioId)
}