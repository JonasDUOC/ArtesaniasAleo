package com.example.artesaniasaleo.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index

@Entity(
    tableName = "pedido_detalle",
    primaryKeys = ["pedidoId", "productoId"],
    foreignKeys = [ForeignKey(
        entity = Pedido::class,
        parentColumns = ["id"],
        childColumns = ["pedidoId"],
        onDelete = ForeignKey.CASCADE
    ),
    ForeignKey(
        entity = Producto::class,
        parentColumns = ["id"],
        childColumns = ["productoId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("productoId")]
    )
data class PedidoDetalle (
    val pedidoId: Int,
    val productoId: Int,
    val cantidad: Int,
    val precioUnitario: Int
)