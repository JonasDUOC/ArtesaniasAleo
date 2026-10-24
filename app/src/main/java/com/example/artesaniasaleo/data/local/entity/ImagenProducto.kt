package com.example.artesaniasaleo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Index


@Entity(
    tableName = "imagen_producto",
    foreignKeys = [ForeignKey(
        entity = Producto::class,
        parentColumns = ["id"],
        childColumns = ["productoId"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [Index("productoId")]
    )
data class ImagenProducto (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val productoId: Int,
    val url: String,
    val orden: Int
)