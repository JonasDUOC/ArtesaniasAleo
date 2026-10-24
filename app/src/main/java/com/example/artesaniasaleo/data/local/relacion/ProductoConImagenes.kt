package com.example.artesaniasaleo.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.artesaniasaleo.data.local.entity.ImagenProducto
import com.example.artesaniasaleo.data.local.entity.Producto

data class ProductoConImagenes(
    @Embedded val producto: Producto,
    @Relation(
        parentColumn = "id",
        entityColumn = "productoId"
    )
    val imagenes: List<ImagenProducto>
)