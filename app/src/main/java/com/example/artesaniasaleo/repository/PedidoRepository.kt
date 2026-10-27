package com.example.artesaniasaleo.repository

import com.example.artesaniasaleo.data.local.dao.PedidoDao
import com.example.artesaniasaleo.data.local.dao.UsuarioDao
import com.example.artesaniasaleo.data.local.entity.Pedido
import com.example.artesaniasaleo.data.local.relation.PedidoConDetalles
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.ZoneId

object EstadoPedido {
    const val CONFIRMACION_PAGO = "Confirmación de pago"
    const val EN_PREPARACION = "En preparación"
    const val ENVIADO = "Enviado"
}

class SinTarjetaException : Exception("Agrega una tarjeta para continuar con la compra")

class PedidoRepository(
    private val pedidoDao: PedidoDao,
    private val usuarioDao: UsuarioDao
) {

    suspend fun confirmarCompra(usuarioId: Int): Int {
        if (usuarioDao.obtenerTarjeta(usuarioId) == null) {
            throw SinTarjetaException()
        }
        return pedidoDao.confirmarCompra(usuarioId)
    }

    fun observarMisPedidos(usuarioId: Int): Flow<List<PedidoConDetalles>> =
        pedidoDao.observarPedidosDeUsuario(usuarioId)

    fun observarTodos(): Flow<List<PedidoConDetalles>> = pedidoDao.observarTodos()

    fun observarDetalle(pedidoId: Int): Flow<PedidoConDetalles?> = pedidoDao.observarPorId(pedidoId)

    fun observarPendientes(): Flow<List<Pedido>> = pedidoDao.observarPendientes()

    suspend fun marcarEnPreparacion(pedidoId: Int) =
        pedidoDao.actualizarEstado(pedidoId, EstadoPedido.EN_PREPARACION)

    suspend fun marcarEnviado(pedidoId: Int) =
        pedidoDao.actualizarEstado(pedidoId, EstadoPedido.ENVIADO)

    fun observarVentasDeHoy(): Flow<Int> = pedidoDao.observarVentasDesde(inicioDelDia())

    fun observarPedidosDeHoy(): Flow<Int> = pedidoDao.observarCantidadPedidosDesde(inicioDelDia())

    private fun inicioDelDia(): Long =
        LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
}