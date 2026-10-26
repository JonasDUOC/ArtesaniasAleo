package com.example.artesaniasaleo.repository

import com.example.artesaniasaleo.data.local.dao.UsuarioDao
import com.example.artesaniasaleo.data.local.datastore.SesionDataStore
import com.example.artesaniasaleo.data.local.entity.Direccion
import com.example.artesaniasaleo.data.local.entity.PerfilUsuario
import com.example.artesaniasaleo.data.local.entity.Tarjeta
import com.example.artesaniasaleo.data.local.entity.Usuario
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class UsuarioRepository(
    private val usuarioDao: UsuarioDao,
    private val sesionDataStore: SesionDataStore
) {

    val usuarioIdSesion: Flow<Int?> = sesionDataStore.usuarioId

    @OptIn(ExperimentalCoroutinesApi::class)
    val usuarioActual: Flow<Usuario?> = sesionDataStore.usuarioId.flatMapLatest { id ->
        if (id == null) flowOf(null) else usuarioDao.observarPorId(id)
    }

    suspend fun correoDisponible(correo: String): Boolean {
        return usuarioDao.buscarPorCorreo(normalizar(correo)) == null
    }

    suspend fun login(correo: String, contrasenia: String): Usuario {
        val usuario = usuarioDao.buscarPorCorreo(normalizar(correo))
            ?: throw IllegalArgumentException("La cuenta no existe")
        if (usuario.contrasenia != contrasenia) {
            throw IllegalArgumentException("Contraseña incorrecta")
        }
        if (usuario.estado == "bloqueado") {
            throw IllegalStateException("Tu cuenta está bloqueada")
        }
        sesionDataStore.guardarSesion(usuario.id)
        return usuario
    }

    suspend fun registrar(
        correo: String,
        contrasenia: String,
        perfil: PerfilUsuario,
        direccion: Direccion
    ): Int {
        val correoNormalizado = normalizar(correo)
        if (usuarioDao.buscarPorCorreo(correoNormalizado) != null) {
            throw IllegalArgumentException("El correo ya está registrado")
        }
        val nuevoId = usuarioDao.registrarCuenta(
            Usuario(correo = correoNormalizado, contrasenia = contrasenia),
            perfil,
            direccion
        )
        sesionDataStore.guardarSesion(nuevoId)
        return nuevoId
    }

    suspend fun cerrarSesion() {
        sesionDataStore.cerrarSesion()
    }

    suspend fun recuperarContrasenia(correo: String, nuevaContrasenia: String) {
        val filas = usuarioDao.actualizarContrasenia(normalizar(correo), nuevaContrasenia)
        if (filas == 0) {
            throw IllegalArgumentException("El correo no está registrado")
        }
    }

    fun observarPerfil(usuarioId: Int): Flow<PerfilUsuario?> = usuarioDao.observarPerfil(usuarioId)

    suspend fun guardarPerfil(perfil: PerfilUsuario) = usuarioDao.guardarPerfil(perfil)

    fun observarDireccion(usuarioId: Int): Flow<Direccion?> = usuarioDao.observarDireccion(usuarioId)

    suspend fun guardarDireccion(direccion: Direccion) = usuarioDao.guardarDireccion(direccion)

    fun observarTarjeta(usuarioId: Int): Flow<Tarjeta?> = usuarioDao.observarTarjeta(usuarioId)

    suspend fun guardarTarjeta(tarjeta: Tarjeta) = usuarioDao.guardarTarjeta(tarjeta)

    suspend fun eliminarTarjeta(usuarioId: Int) = usuarioDao.eliminarTarjeta(usuarioId)

    fun observarTodos(): Flow<List<Usuario>> = usuarioDao.observarTodos()

    suspend fun cambiarRol(usuarioId: Int, rol: String) = usuarioDao.actualizarRol(usuarioId, rol)

    suspend fun bloquear(usuarioId: Int) = usuarioDao.actualizarEstado(usuarioId, "bloqueado")

    suspend fun activar(usuarioId: Int) = usuarioDao.actualizarEstado(usuarioId, "activo")

    private fun normalizar(correo: String): String = correo.trim().lowercase()
}