package com.example.artesaniasaleo.repository

import com.example.artesaniasaleo.data.local.dao.CategoriaDao
import com.example.artesaniasaleo.data.local.entity.Categoria
import kotlinx.coroutines.flow.Flow

class CategoriaRepository(private val categoriaDao: CategoriaDao) {

    fun observarTodas(): Flow<List<Categoria>> = categoriaDao.observarTodas()

    suspend fun crear(nombre: String): Int {
        val limpio = nombre.trim()
        if (limpio.isBlank()) {
            throw IllegalArgumentException("El nombre de la categoría es obligatorio")
        }
        if (categoriaDao.buscarPorNombre(limpio) != null) {
            throw IllegalArgumentException("Ya existe una categoría con ese nombre")
        }
        return categoriaDao.insertar(Categoria(nombre = limpio)).toInt()
    }

    suspend fun renombrar(categoria: Categoria, nuevoNombre: String) {
        val limpio = nuevoNombre.trim()
        if (limpio.isBlank()) {
            throw IllegalArgumentException("El nombre de la categoría es obligatorio")
        }
        val existente = categoriaDao.buscarPorNombre(limpio)
        if (existente != null && existente.id != categoria.id) {
            throw IllegalArgumentException("Ya existe una categoría con ese nombre")
        }
        categoriaDao.actualizar(categoria.copy(nombre = limpio))
    }

    suspend fun eliminar(categoria: Categoria) {
        if (categoriaDao.contarProductos(categoria.id) > 0) {
            throw IllegalStateException("No se puede eliminar: la categoría tiene productos")
        }
        categoriaDao.eliminar(categoria)
    }
}