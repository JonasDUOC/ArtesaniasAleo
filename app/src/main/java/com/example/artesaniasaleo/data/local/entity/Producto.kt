package com.example.artesaniasaleo.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey
import androidx.room3.Index

@Entity(
    tableName = "producto",
    foreignKeys = [ForeignKey(
        entity = Categoria::class,
        parentColumns = ["id"],
        childColumns = ["categoriaId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("categoriaId")]
    )
data class Producto (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val categoriaId: Int,
    val titulo: String,
    val descripcion: String,
    val precio: Int,
    val estado: String = "activo"
)