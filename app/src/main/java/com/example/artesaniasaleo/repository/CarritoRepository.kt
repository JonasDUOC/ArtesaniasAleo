package com.example.artesaniasaleo.repository

import com.example.artesaniasaleo.data.local.dao.CarritoDao
import com.example.artesaniasaleo.data.local.relation.CarritoItemConProducto
import kotlinx.coroutines.flow.Flow

class CarritoRepository(private val carritoDao: CarritoDao) {

    fun observarCarrito(usuarioId: Int): Flow<List<CarritoItemConProducto>> =
        carritoDao.observarCarrito(usuarioId)

    fun observarCantidadTotal(usuarioId: Int): Flow<Int> = carritoDao.observarCantidadTotal(usuarioId)

    fun observarTotal(usuarioId: Int): Flow<Int> = carritoDao.observarTotal(usuarioId)

    suspend fun agregar(usuarioId: Int, productoId: Int, cantidad: Int = 1) {
        if (cantidad <= 0) {
            throw IllegalArgumentException("La cantidad debe ser mayor a 0")
        }
        carritoDao.agregar(usuarioId, productoId, cantidad)
    }

    suspend fun cambiarCantidad(usuarioId: Int, productoId: Int, cantidad: Int) {
        if (cantidad <= 0) {
            carritoDao.eliminar(usuarioId, productoId)
        } else {
            carritoDao.actualizarCantidad(usuarioId, productoId, cantidad)
        }
    }

    suspend fun quitar(usuarioId: Int, productoId: Int) = carritoDao.eliminar(usuarioId, productoId)

    suspend fun vaciar(usuarioId: Int) = carritoDao.vaciar(usuarioId)
}