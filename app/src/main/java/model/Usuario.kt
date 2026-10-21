package model

import androidx.room3.Entity
import androidx.room3.PrimaryKey

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
