package com.example.artesaniasaleo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario")
data class Usuario (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val correo: String,
    val contrasenia: String,
    val rol: String = "user",
    val estado: String = "activo",
    val fechaRegistro: Long = System.currentTimeMillis()
)
