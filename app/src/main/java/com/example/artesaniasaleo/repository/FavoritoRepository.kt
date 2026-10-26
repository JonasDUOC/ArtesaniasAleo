package com.example.artesaniasaleo.repository

import com.example.artesaniasaleo.data.local.dao.FavoritoDao
import com.example.artesaniasaleo.data.local.entity.Favorito
import com.example.artesaniasaleo.data.local.relation.ProductoConImagenes
import kotlinx.coroutines.flow.Flow

class FavoritoRepository(private val favoritoDao: FavoritoDao) {

    fun observarFavoritos(usuarioId: Int): Flow<List<ProductoConImagenes>> =
        favoritoDao.observarFavoritos(usuarioId)

    fun observarIdsFavoritos(usuarioId: Int): Flow<List<Int>> = favoritoDao.observarIdsFavoritos(usuarioId)

    fun observarEsFavorito(usuarioId: Int, productoId: Int): Flow<Boolean> =
        favoritoDao.observarEsFavorito(usuarioId, productoId)

    suspend fun alternar(usuarioId: Int, productoId: Int, esFavoritoActual: Boolean) {
        if (esFavoritoActual) {
            favoritoDao.quitar(usuarioId, productoId)
        } else {
            favoritoDao.agregar(Favorito(usuarioId = usuarioId, productoId = productoId))
        }
    }
}