package com.example.artesaniasaleo.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey
import androidx.room3.Index


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