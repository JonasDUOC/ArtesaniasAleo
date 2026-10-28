package com.example.artesaniasaleo.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.artesaniasaleo.data.local.database.AppDatabase
import com.example.artesaniasaleo.data.local.datastore.SesionDataStore
import com.example.artesaniasaleo.repository.CategoriaRepository
import com.example.artesaniasaleo.repository.FavoritoRepository
import com.example.artesaniasaleo.repository.ProductoRepository
import com.example.artesaniasaleo.repository.UsuarioRepository

// Crea los ViewModel con sus dependencias (sin Hilt).
// viewModel() sin Factory solo sabe crear ViewModel con constructor vacío;
// los nuestros necesitan un repository, así que esta clase sabe cómo armarlos.
class AppViewModelFactory(context: Context) : ViewModelProvider.Factory {

    // applicationContext vive lo mismo que la app; el de la Activity se destruye al rotar
    private val appContext = context.applicationContext

    // by lazy: se crean la primera vez que se usan y luego se reutiliza la misma instancia
    private val database by lazy { AppDatabase.getDatabase(appContext) }
    private val sesionDataStore by lazy { SesionDataStore(appContext) }

    private val usuarioRepository by lazy {
        UsuarioRepository(database.usuarioDao(), sesionDataStore)
    }
    private val categoriaRepository by lazy { CategoriaRepository(database.categoriaDao()) }
    // ProductoRepository necesita el contexto para copiar las fotos a la carpeta de la app
    private val productoRepository by lazy { ProductoRepository(database.productoDao(), appContext) }
    private val favoritoRepository by lazy { FavoritoRepository(database.favoritoDao()) }
    // Próximos: carritoRepository, pedidoRepository

    // Android llama a create() cuando una pantalla pide un ViewModel con viewModel(factory = ...)
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(SesionViewModel::class.java) ->
                SesionViewModel(usuarioRepository) as T
            modelClass.isAssignableFrom(LoginViewModel::class.java) ->
                LoginViewModel(usuarioRepository) as T
            modelClass.isAssignableFrom(RegistroViewModel::class.java) ->
                RegistroViewModel(usuarioRepository) as T
            modelClass.isAssignableFrom(CatalogoViewModel::class.java) ->
                CatalogoViewModel(productoRepository, categoriaRepository, favoritoRepository, usuarioRepository) as T
            else -> throw IllegalArgumentException(
                "ViewModel no registrado en AppViewModelFactory: ${modelClass.name}"
            )
        }
    }
}
