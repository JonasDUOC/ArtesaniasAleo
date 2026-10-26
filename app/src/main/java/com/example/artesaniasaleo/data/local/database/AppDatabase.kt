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
    version = 1,
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
                    .addCallback(crearAdminCallback)
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                INSTANCE = instancia
                instancia
            }
        }

        private val crearAdminCallback = object : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                val ahora = System.currentTimeMillis()
                db.execSQL(
                    """
                    INSERT INTO usuario (correo, contrasenia, rol, estado, fechaRegistro)
                    VALUES ('admin@aleyleo.cl', 'admin123', 'admin', 'activo', $ahora)
                    """
                )
                db.execSQL(
                    """
                    INSERT INTO perfil_usuario (usuarioId, rut, nombre, apellido)
                    VALUES (1, '11111111-1', 'Administradora', 'Ale y Leo')
                    """
                )
            }
        }
    }
}