package com.example.artesaniasaleo.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.sesionDataStore: DataStore<Preferences> by preferencesDataStore(name = "sesion")

class SesionDataStore(private val context: Context) {

    private object Claves {
        val USUARIO_ID = intPreferencesKey("usuario_id")
    }

    val usuarioId: Flow<Int?> = context.sesionDataStore.data
        .catch { error ->
            if (error is IOException) {
                emit(emptyPreferences())
            } else {
                throw error
            }
        }
        .map { preferencias ->
            preferencias[Claves.USUARIO_ID]
        }

    suspend fun guardarSesion(id: Int) {
        context.sesionDataStore.edit { preferencias ->
            preferencias[Claves.USUARIO_ID] = id
        }
    }

    suspend fun cerrarSesion() {
        context.sesionDataStore.edit { preferencias ->
            preferencias.remove(Claves.USUARIO_ID)
        }
    }
}