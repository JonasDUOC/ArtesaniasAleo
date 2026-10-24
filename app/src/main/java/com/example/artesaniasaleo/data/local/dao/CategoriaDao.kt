package com.example.artesaniasaleo.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.artesaniasaleo.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoriaDao {

    @Query("SELECT * FROM categoria ORDER BY nombre ASC")
    fun observarTodas(): Flow<List<Categoria>>

    @Query("SELECT * FROM categoria WHERE id = :id")
    suspend fun buscarPorId(id: Int): Categoria?

    @Query("SELECT * FROM categoria WHERE nombre = :nombre LIMIT 1")
    suspend fun buscarPorNombre(nombre: String): Categoria?

    @Query("SELECT COUNT(*) FROM producto WHERE categoriaId = :categoriaId")
    suspend fun contarProductos(categoriaId: Int): Int

    @Insert
    suspend fun insertar(categoria: Categoria): Long

    @Insert
    suspend fun insertarTodas(categorias: List<Categoria>)

    @Update
    suspend fun actualizar(categoria: Categoria)

    @Delete
    suspend fun eliminar(categoria: Categoria)
}