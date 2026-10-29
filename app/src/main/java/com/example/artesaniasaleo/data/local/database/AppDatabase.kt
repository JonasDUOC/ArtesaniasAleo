package com.example.artesaniasaleo.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.artesaniasaleo.data.local.dao.CarritoDao
import com.example.artesaniasaleo.data.local.dao.CategoriaDao
import com.example.artesaniasaleo.data.local.dao.FavoritoDao
import com.example.artesaniasaleo.data.local.dao.PedidoDao
import com.example.artesaniasaleo.data.local.dao.ProductoDao
import com.example.artesaniasaleo.data.local.dao.UsuarioDao
import com.example.artesaniasaleo.data.local.entity.CarritoItem
import com.example.artesaniasaleo.data.local.entity.Categoria
import com.example.artesaniasaleo.data.local.entity.Direccion
import com.example.artesaniasaleo.data.local.entity.Favorito
import com.example.artesaniasaleo.data.local.entity.ImagenProducto
import com.example.artesaniasaleo.data.local.entity.Pedido
import com.example.artesaniasaleo.data.local.entity.PedidoDetalle
import com.example.artesaniasaleo.data.local.entity.PerfilUsuario
import com.example.artesaniasaleo.data.local.entity.Producto
import com.example.artesaniasaleo.data.local.entity.Tarjeta
import com.example.artesaniasaleo.data.local.entity.Usuario

@Database(
    entities = [
        Usuario::class,
        PerfilUsuario::class,
        Direccion::class,
        Tarjeta::class,
        Categoria::class,
        Producto::class,
        ImagenProducto::class,
        CarritoItem::class,
        Favorito::class,
        Pedido::class,
        PedidoDetalle::class
    ],
    version = 4, // 2: PerfilUsuario.telefono · 3: Producto.stock · 4: imágenes semilla
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDao
    abstract fun categoriaDao(): CategoriaDao
    abstract fun productoDao(): ProductoDao
    abstract fun carritoDao(): CarritoDao
    abstract fun favoritoDao(): FavoritoDao
    abstract fun pedidoDao(): PedidoDao

    companion object {

        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instancia = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "artesanias_aleyleo.db"
                )
                    .addCallback(datosInicialesCallback)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instancia
                instancia
            }
        }

        // Se ejecuta UNA sola vez: cuando Room crea el archivo de la base de datos
        // (primera instalación, o después de desinstalar la app).
        private val datosInicialesCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                insertarDatosIniciales(db)
            }

            // Si se sube la versión, fallbackToDestructiveMigration borra y recrea las tablas,
            // pero NO llama a onCreate: por eso los datos semilla se insertan también aquí
            override fun onDestructiveMigration(db: SupportSQLiteDatabase) {
                super.onDestructiveMigration(db)
                insertarDatosIniciales(db)
            }
        }

        // INSERT OR IGNORE: si un dato ya existe (mismo id), se salta en vez de fallar
        private fun insertarDatosIniciales(db: SupportSQLiteDatabase) {
            insertarAdmin(db)
            insertarClienteDemo(db)
            insertarCatalogo(db)
            insertarImagenesCatalogo(db)
        }

        // Cuenta de administrador preexistente (id 1)
        private fun insertarAdmin(db: SupportSQLiteDatabase) {
            val ahora = System.currentTimeMillis()
            db.execSQL(
                """
                INSERT OR IGNORE INTO usuario (id, correo, contrasenia, rol, estado, fechaRegistro)
                VALUES (1, 'admin@aleyleo.cl', 'admin123', 'admin', 'activo', $ahora)
                """
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO perfil_usuario (usuarioId, rut, nombre, apellido, telefono)
                VALUES (1, '11.111.111-1', 'Administradora', 'Ale y Leo', '+56 9 1111 1111')
                """
            )
        }

        // Cliente de prueba (id 2) con perfil, dirección y tarjeta sintética,
        // para probar la compra sin registrarse cada vez: ana@mail.com / 123456
        private fun insertarClienteDemo(db: SupportSQLiteDatabase) {
            val ahora = System.currentTimeMillis()
            db.execSQL(
                """
                INSERT OR IGNORE INTO usuario (id, correo, contrasenia, rol, estado, fechaRegistro)
                VALUES (2, 'ana@mail.com', '123456', 'user', 'activo', $ahora)
                """
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO perfil_usuario (usuarioId, rut, nombre, apellido, telefono)
                VALUES (2, '12.345.678-5', 'Ana', 'Pérez', '+56 9 1234 5678')
                """
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO direccion (usuarioId, region, comuna, calle, numero, departamento)
                VALUES (2, 'Región Metropolitana', 'Ñuñoa', 'Av. Irarrázaval', '1234', NULL)
                """
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO tarjeta (usuarioId, tipo, numero, vencimiento, cvv)
                VALUES (2, 'Crédito', '4111111111111111', '12/29', '123')
                """
            )
        }

        // Datos de prueba (sintéticos) para que el catálogo no parta vacío.
        // Los ids se escriben a mano para que cada producto apunte a su categoría.
        // Stock variado para probar: la Cuchara tallada (id 5) parte agotada y la Manta (id 4) con 1.
        private fun insertarCatalogo(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO categoria (id, nombre) VALUES
                (1, 'Cerámica'),
                (2, 'Tejidos'),
                (3, 'Madera'),
                (4, 'Joyería')
                """
            )
            db.execSQL(
                """
                INSERT OR IGNORE INTO producto (id, categoriaId, titulo, descripcion, precio, estado, stock) VALUES
                (1, 1, 'Jarrón de greda', 'Pieza única elaborada con técnica de greda cocida y pintura mineral. 24 cm de alto x 14 cm de diámetro.', 18500, 'activo', 5),
                (2, 1, 'Taza esmaltada', 'Taza de cerámica modelada a mano con esmalte azul. Capacidad 300 ml.', 7900, 'activo', 3),
                (3, 2, 'Chal de lana', 'Tejido a telar con lana de oveja teñida naturalmente. 180 x 60 cm.', 12000, 'activo', 4),
                (4, 2, 'Manta de telar', 'Manta tejida en telar mapuche con diseños tradicionales. 200 x 140 cm.', 34900, 'activo', 1),
                (5, 3, 'Cuchara tallada', 'Cuchara de servir tallada a mano en madera de raulí.', 6500, 'activo', 0),
                (6, 3, 'Tabla para picar', 'Tabla de raulí curada con aceite de linaza. 40 x 25 cm.', 14500, 'activo', 2),
                (7, 4, 'Anillo de plata', 'Anillo artesanal en plata 950 con piedra natural.', 15900, 'activo', 2),
                (8, 4, 'Aros de cobre', 'Aros martillados a mano en cobre con baño de plata.', 9900, 'activo', 6)
                """
            )
        }

        // Fotos de los productos semilla. Los archivos vienen DENTRO del APK, en
        // res/drawable-nodpi/producto_1.jpg ... producto_8.jpg (nodpi: Android no las reescala).
        // En la tabla se guarda su Uri "android.resource://paquete/drawable/nombre";
        // las fotos que agrega el admin, en cambio, se guardan como ruta de archivo (filesDir/productos).
        // Si falta un archivo, ImagenArtesania muestra el degradado (no se cae).
        private const val FOTOS_APK = "android.resource://com.example.artesaniasaleo/drawable/"

        private fun insertarImagenesCatalogo(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                INSERT OR IGNORE INTO imagen_producto (id, productoId, url, orden) VALUES
                (1, 1, '${FOTOS_APK}producto_1', 0),
                (2, 2, '${FOTOS_APK}producto_2', 0),
                (3, 3, '${FOTOS_APK}producto_3', 0),
                (4, 4, '${FOTOS_APK}producto_4', 0),
                (5, 5, '${FOTOS_APK}producto_5', 0),
                (6, 6, '${FOTOS_APK}producto_6', 0),
                (7, 7, '${FOTOS_APK}producto_7', 0),
                (8, 8, '${FOTOS_APK}producto_8', 0)
                """
            )
        }
    }
}
