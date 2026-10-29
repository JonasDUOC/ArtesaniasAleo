package com.example.artesaniasaleo.viewmodel

import com.example.artesaniasaleo.data.local.relation.PedidoConDetalles
import com.example.artesaniasaleo.data.local.relation.ProductoConImagenes
import com.example.artesaniasaleo.data.local.relation.UsuarioConPerfil
import com.example.artesaniasaleo.model.EstadoPedido
import com.example.artesaniasaleo.model.LineaPedido
import com.example.artesaniasaleo.model.PedidoUi
import com.example.artesaniasaleo.model.ProductoUi
import com.example.artesaniasaleo.model.UsuarioUi

// Conversiones de entidades de Room (data/local) a modelos de pantalla (model/).
// Las usan varios ViewModel; así las pantallas nunca dependen de Room.
// Son funciones de extensión: se llaman como si fueran métodos de la entidad.

fun ProductoConImagenes.aProductoUi(favoritos: Set<Int> = emptySet()): ProductoUi {
    val rutas = imagenes.sortedBy { it.orden }.map { it.url }
    return ProductoUi(
        id = producto.id,
        nombre = producto.titulo,        // el MER dice "titulo"; la pantalla, "nombre"
        precio = producto.precio,
        imagenUrl = rutas.firstOrNull(), // primera imagen del carrusel
        imagenes = rutas,
        esFavorito = producto.id in favoritos,
        descripcion = producto.descripcion,
        categoriaId = producto.categoriaId,
        stock = producto.stock
    )
}

// El estado se guarda como texto en la BD; aquí se convierte al enum de la UI
fun estadoPedidoDesde(etiqueta: String): EstadoPedido =
    EstadoPedido.entries.find { it.etiqueta == etiqueta } ?: EstadoPedido.CONFIRMACION_PAGO

fun PedidoConDetalles.aPedidoUi(): PedidoUi {
    val nombre = listOfNotNull(perfil?.nombre, perfil?.apellido).joinToString(" ").ifBlank { "Cliente" }
    val textoDireccion = direccion?.let { d ->
        "${d.region}, ${d.comuna}, ${d.calle} ${d.numero}" +
            if (!d.departamento.isNullOrBlank()) " (${d.departamento})" else ""
    } ?: "Sin dirección"
    return PedidoUi(
        id = pedido.id,
        cliente = nombre,
        correo = usuario?.correo.orEmpty(),
        telefono = perfil?.telefono.orEmpty(),
        direccion = textoDireccion,   // CU-36: datos del cliente SIN el RUT
        lineas = detalles.map { LineaPedido(it.producto.producto.titulo, it.detalle.cantidad) },
        total = pedido.total,
        estado = estadoPedidoDesde(pedido.estado)
    )
}

fun UsuarioConPerfil.aUsuarioUi(): UsuarioUi = UsuarioUi(
    id = usuario.id,
    nombre = listOfNotNull(perfil?.nombre, perfil?.apellido).joinToString(" ").ifBlank { usuario.correo },
    esAdmin = usuario.rol == "admin",
    bloqueado = usuario.estado == "bloqueado"
)
