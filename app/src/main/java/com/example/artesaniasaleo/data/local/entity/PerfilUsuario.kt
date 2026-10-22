package com.example.artesaniasaleo.data.local.entity

import androidx.room3.Entity
import androidx.room3.ForeignKey
import androidx.room3.PrimaryKey

@Entity(
    tableName = "perfil_usuario",
    foreignKeys = [ForeignKey(
        entity = Usuario::class,
        parentColumns = ["id"],
        childColumns = ["usuarioId"],
        onDelete = ForeignKey.CASCADE
    )]
    )
data class PerfilUsuario (
    @PrimaryKey
    val usuarioId: Int,
    val rut: String,
    val nombre: String,
    val apellido: String
)