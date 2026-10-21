package model

import androidx.room3.Entity

@Entity(tableName = "perfil_usuario",
    foreignKeys = [

    ]
    )
data class PerfilUsuario (
    val perfilUsuarioId: Int,
    val rut: String,
    val name: String,
    val apellido: String
)

//Quedo pendiente acomodar el tema de las tablas de usuario y el de la relacion de perfilUsuario