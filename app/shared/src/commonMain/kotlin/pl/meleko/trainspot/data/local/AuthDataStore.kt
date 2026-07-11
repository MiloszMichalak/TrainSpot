package pl.meleko.trainspot.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class AuthDataStore(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        private val tokenKey = stringPreferencesKey("auth_token")
        private val refreshTokenKey = stringPreferencesKey("refresh_token")
        private val dictionarySyncedKey = booleanPreferencesKey("dictionary_synced")
    }

    suspend fun saveToken(token: String) {
        dataStore.edit { preferences ->
            preferences[tokenKey] = token
        }
    }

    suspend fun saveRefreshToken(refreshToken: String) {
        dataStore.edit { preferences ->
            preferences[refreshTokenKey] = refreshToken
        }
    }

    suspend fun getToken(): String? {
        return dataStore.data.map { it[tokenKey] }.first()
    }

    suspend fun getRefreshToken(): String? {
        return dataStore.data.map { it[refreshTokenKey] }.first()
    }

    suspend fun clearToken() {
        dataStore.edit { preferences ->
            preferences.remove(tokenKey)
            preferences.remove(refreshTokenKey)
            preferences.remove(dictionarySyncedKey)
        }
    }

    suspend fun isDictionarySynced(): Boolean {
        return dataStore.data.map { it[dictionarySyncedKey] ?: false }.first()
    }

    suspend fun setDictionarySynced(synced: Boolean) {
        dataStore.edit { preferences ->
            preferences[dictionarySyncedKey] = synced
        }
    }
}
