package com.example.artesaniasaleo.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.artesaniasaleo.data.local.entity.PerfilUsuario
import com.example.artesaniasaleo.data.local.entity.Usuario

// Una cuenta con su perfil (nombre y apellido), para el control de usuarios del administrador
data class UsuarioConPerfil(
    @Embedded val usuario: Usuario,
    @Relation(
        parentColumn = "id",
        entityColumn = "usuarioId"
    )
    val perfil: PerfilUsuario?
)
