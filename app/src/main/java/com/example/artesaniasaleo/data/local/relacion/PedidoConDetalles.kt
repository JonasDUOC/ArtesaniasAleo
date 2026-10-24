package com.example.artesaniasaleo.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.artesaniasaleo.data.local.entity.Direccion
import com.example.artesaniasaleo.data.local.entity.Pedido
import com.example.artesaniasaleo.data.local.entity.PedidoDetalle
import com.example.artesaniasaleo.data.local.entity.PerfilUsuario

data class PedidoConDetalles(
    @Embedded val pedido: Pedido,
    @Relation(
        entity = PedidoDetalle::class,
        parentColumn = "id",
        entityColumn = "pedidoId"
    )
    val detalles: List<DetalleConProducto>,
    @Relation(
        parentColumn = "usuarioId",
        entityColumn = "usuarioId"
    )
    val perfil: PerfilUsuario?,
    @Relation(
        parentColumn = "usuarioId",
        entityColumn = "usuarioId"
    )
    val direccion: Direccion?
)