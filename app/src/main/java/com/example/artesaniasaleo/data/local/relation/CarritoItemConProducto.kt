package com.example.artesaniasaleo.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.artesaniasaleo.data.local.entity.CarritoItem
import com.example.artesaniasaleo.data.local.entity.Producto

data class CarritoItemConProducto(
    @Embedded val item: CarritoItem,
    @Relation(
        entity = Producto::class,
        parentColumn = "productoId",
        entityColumn = "id"
    )
    val producto: ProductoConImagenes
)