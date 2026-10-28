package com.example.artesaniasaleo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import com.example.artesaniasaleo.data.local.entity.Direccion
import com.example.artesaniasaleo.data.local.entity.PerfilUsuario
import com.example.artesaniasaleo.data.local.entity.Tarjeta
import com.example.artesaniasaleo.data.local.entity.Usuario
import com.example.artesaniasaleo.data.local.relation.UsuarioConPerfil
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    @Query("SELECT * FROM usuario WHERE correo = :correo LIMIT 1")
    suspend fun buscarPorCorreo(correo: String): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :id")
    suspend fun buscarPorId(id: Int): Usuario?

    @Query("SELECT * FROM usuario WHERE id = :id")
    fun observarPorId(id: Int): Flow<Usuario?>

    @Query("SELECT * FROM usuario ORDER BY fechaRegistro DESC")
    fun observarTodos(): Flow<List<Usuario>>

    // Control de usuarios (CU-39): cada cuenta con su perfil, para mostrar el nombre
    @Transaction
    @Query("SELECT * FROM usuario ORDER BY fechaRegistro DESC")
    fun observarTodosConPerfil(): Flow<List<UsuarioConPerfil>>

    // CU-09: el cliente puede cambiar su correo desde el perfil
    @Query("UPDATE usuario SET correo = :correo WHERE id = :id")
    suspend fun actualizarCorreo(id: Int, correo: String)

    @Insert
    suspend fun insertarUsuario(usuario: Usuario): Long

    @Transaction
    suspend fun registrarCuenta(
        usuario: Usuario,
        perfil: PerfilUsuario,
        direccion: Direccion
    ): Int {
        val nuevoId = insertarUsuario(usuario).toInt()
        guardarPerfil(perfil.copy(usuarioId = nuevoId))
        guardarDireccion(direccion.copy(usuarioId = nuevoId))
        return nuevoId
    }

    @Query("UPDATE usuario SET contrasenia = :nueva WHERE correo = :correo")
    suspend fun actualizarContrasenia(correo: String, nueva: String): Int

    @Query("UPDATE usuario SET rol = :rol WHERE id = :id")
    suspend fun actualizarRol(id: Int, rol: String)

    @Query("UPDATE usuario SET estado = :estado WHERE id = :id")
    suspend fun actualizarEstado(id: Int, estado: String)

    @Query("SELECT * FROM perfil_usuario WHERE usuarioId = :usuarioId")
    fun observarPerfil(usuarioId: Int): Flow<PerfilUsuario?>

    @Upsert
    suspend fun guardarPerfil(perfil: PerfilUsuario)

    @Query("SELECT * FROM direccion WHERE usuarioId = :usuarioId")
    fun observarDireccion(usuarioId: Int): Flow<Direccion?>

    @Upsert
    suspend fun guardarDireccion(direccion: Direccion)

    @Query("SELECT * FROM tarjeta WHERE usuarioId = :usuarioId")
    fun observarTarjeta(usuarioId: Int): Flow<Tarjeta?>

    @Query("SELECT * FROM tarjeta WHERE usuarioId = :usuarioId")
    suspend fun obtenerTarjeta(usuarioId: Int): Tarjeta?

    @Upsert
    suspend fun guardarTarjeta(tarjeta: Tarjeta)

    @Query("DELETE FROM tarjeta WHERE usuarioId = :usuarioId")
    suspend fun eliminarTarjeta(usuarioId: Int)
}