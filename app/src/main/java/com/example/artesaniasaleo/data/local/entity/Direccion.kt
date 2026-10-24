package com.example.artesaniasaleo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "direccion",
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.CASCADE
    )]
    )
data class Direccion (
    @PrimaryKey
    val usuarioId: Int,
    val region: String,
    val comuna: String,
    val calle: String,
    val numero: String,
    val departamento: String? = null
)