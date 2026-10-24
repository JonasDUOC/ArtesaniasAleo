package com.example.artesaniasaleo.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.artesaniasaleo.data.local.entity.PedidoDetalle
import com.example.artesaniasaleo.data.local.entity.Producto

data class DetalleConProducto(
    @Embedded val detalle: PedidoDetalle,
    @Relation(
        entity = Producto::class,
        parentColumn = "productoId",
        entityColumn = "id"
    )
    val producto: ProductoConImagenes
)