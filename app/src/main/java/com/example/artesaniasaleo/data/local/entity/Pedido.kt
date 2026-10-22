package com.example.artesaniasaleo.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey
import androidx.room3.Index

@Entity(
    tableName = "pedido",
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index("usuarioId")]
)
data class Pedido(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val usuarioId: Int,
    val estado: String = "Confirmación de pago",
    val fecha: Long = System.currentTimeMillis(),
    val total: Int
)