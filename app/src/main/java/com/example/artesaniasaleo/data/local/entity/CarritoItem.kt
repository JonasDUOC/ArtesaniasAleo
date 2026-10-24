package com.example.artesaniasaleo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "carrito_item",
    primaryKeys = ["usuarioId", "productoId"],
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Producto::class,
            parentColumns = ["id"],
            childColumns = ["productoId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("productoId")]
)
data class CarritoItem(
    val usuarioId: Int,
    val productoId: Int,
    val cantidad: Int
)