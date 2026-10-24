package com.example.artesaniasaleo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.artesaniasaleo.data.local.entity.Favorito
import com.example.artesaniasaleo.data.local.relation.ProductoConImagenes
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritoDao {

    @Transaction
    @Query(
        """
        SELECT p.* FROM producto p
        INNER JOIN favorito f ON f.productoId = p.id
        WHERE f.usuarioId = :usuarioId AND p.estado = 'activo'
        ORDER BY p.titulo ASC
        """
    )
    fun observarFavoritos(usuarioId: Int): Flow<List<ProductoConImagenes>>

    @Query("SELECT productoId FROM favorito WHERE usuarioId = :usuarioId")
    fun observarIdsFavoritos(usuarioId: Int): Flow<List<Int>>

    @Query("SELECT EXISTS(SELECT 1 FROM favorito WHERE usuarioId = :usuarioId AND productoId = :productoId)")
    fun observarEsFavorito(usuarioId: Int, productoId: Int): Flow<Boolean>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun agregar(favorito: Favorito)

    @Query("DELETE FROM favorito WHERE usuarioId = :usuarioId AND productoId = :productoId")
    suspend fun quitar(usuarioId: Int, productoId: Int)
}