package com.example.artesaniasaleo.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.Index


@Entity(
    tableName = "favorito",
    primaryKeys = ["usuarioId", "productoId"],
    foreignKeys = [ForeignKey(
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
    )],
    indices = [Index("productoId")]
    )
data class Favorito (
    val usuarioId: Int,
    val productoId: Int
)