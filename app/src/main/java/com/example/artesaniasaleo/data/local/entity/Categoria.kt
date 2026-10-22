package com.example.artesaniasaleo.data.local.entity

import androidx.room3.Entity
import androidx.room3.PrimaryKey
import androidx.room3.Index

@Entity(tableName = "categoria",
    indices = [Index(value = ["nombre"], unique = true)])
data class Categoria (
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val nombre: String
)