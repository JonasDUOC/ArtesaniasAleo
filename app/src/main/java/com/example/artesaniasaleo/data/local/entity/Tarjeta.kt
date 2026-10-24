package com.example.artesaniasaleo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tarjeta",
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.CASCADE
    )]
    )
data class Tarjeta (
    @PrimaryKey
    val usuarioId: Int,
    val tipo: String,
    val numero: String,
    val vencimiento: String,
    val cvv: String
)
