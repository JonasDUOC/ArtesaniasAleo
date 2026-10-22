package com.example.artesaniasaleo.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

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
