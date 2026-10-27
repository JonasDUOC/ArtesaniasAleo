package com.example.artesaniasaleo.model

// Datos del mockup. Se usan en las vistas previas y hasta que las pantallas lean la base de datos

val CATEGORIAS_DEMO = listOf(
    CategoriaUi(id = 1, nombre = "Cerámica"),
    CategoriaUi(id = 2, nombre = "Tejidos"),
    CategoriaUi(id = 3, nombre = "Madera"),
    CategoriaUi(id = 4, nombre = "Joyería")
)

val PRODUCTOS_DEMO = listOf(
    ProductoUi(
        id = 1, nombre = "Jarrón de greda", precio = 18500, categoriaId = 1,
        descripcion = "Pieza única elaborada con técnica de greda cocida y pintura mineral. 24cm alto x 14cm diámetro."
    ),
    ProductoUi(
        id = 2, nombre = "Chal de lana", precio = 12000, categoriaId = 2, esFavorito = true,
        descripcion = "Tejido a telar con lana de oveja teñida naturalmente. 180x60cm."
    ),
    ProductoUi(
        id = 3, nombre = "Cuchara tallada", precio = 6500, categoriaId = 3,
        descripcion = "Cuchara de servir tallada a mano en madera de raulí."
    ),
    ProductoUi(
        id = 4, nombre = "Anillo de plata", precio = 15900, categoriaId = 4,
        descripcion = "Anillo artesanal en plata 950 con piedra natural."
    )
)

val PERFIL_DEMO = PerfilUiState(
    rut = "12.345.678-5", nombres = "Camila", apellido = "Torres",
    telefono = "+56 9 1234 5678", correo = "camila.t@mail.com",
    region = "Región Metropolitana", comuna = "Ñuñoa", calle = "Av. Irarrázaval", numero = "1234"
)

val PEDIDOS_DEMO = listOf(
    PedidoUi(
        id = 1033, cliente = "Pedro Ruiz", correo = "pedro.r@mail.com", telefono = "+56 9 8765 4321",
        direccion = "Región de Valparaíso, Viña del Mar, Calle 5 Norte 220",
        lineas = listOf(LineaPedido("Cuchara tallada", 2)), total = 13000
    ),
    PedidoUi(
        id = 1032, cliente = "Camila Torres", correo = "camila.t@mail.com", telefono = "+56 9 1234 5678",
        direccion = "Región Metropolitana, Ñuñoa, Av. Irarrázaval 1234",
        lineas = listOf(LineaPedido("Jarrón de greda", 1), LineaPedido("Chal de lana", 2)),
        total = 42500, estado = EstadoPedido.EN_PREPARACION
    ),
    PedidoUi(
        id = 1029, cliente = "Camila Torres", correo = "camila.t@mail.com", telefono = "+56 9 1234 5678",
        direccion = "Región Metropolitana, Ñuñoa, Av. Irarrázaval 1234",
        lineas = listOf(LineaPedido("Anillo de plata", 1)), total = 15900, estado = EstadoPedido.ENVIADO
    )
)

val USUARIOS_DEMO = listOf(
    UsuarioUi(id = 1, nombre = "Camila Torres"),
    UsuarioUi(id = 2, nombre = "Pedro Ruiz"),
    UsuarioUi(id = 3, nombre = "Alejandra R.", esAdmin = true)
)
