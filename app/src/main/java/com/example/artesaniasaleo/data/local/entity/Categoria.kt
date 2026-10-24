package com.example.artesaniasaleo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.Index

@Entity(tableName = "categoria",
    indices = [Index(value = ["nombre"], unique = true)])
data class Categoria (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String
)