package com.example.artesaniasaleo.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

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
    val numero: String
)