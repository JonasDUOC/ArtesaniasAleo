package com.example.artesaniasaleo.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

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
    val apellido: String,
    // CU-09: el cliente edita su teléfono en el perfil. No estaba en el MER (agregar allí también)
    val telefono: String? = null
)