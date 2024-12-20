package com.dicoding.storyapps.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dicoding.storyapps.response.LoginResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class userPreference private constructor(private val dataStore: DataStore<Preferences>){

    companion object {

        @Volatile
        private var INSTANCE: userPreference? = null

        private val ID_KEY = stringPreferencesKey("id")
        private val NAME_KEY = stringPreferencesKey("name")
        private val TOKEN_KEY = stringPreferencesKey("token")

        fun getInstance(dataStore: DataStore<Preferences>): userPreference {
            return INSTANCE ?: synchronized(this) {
                val instance = userPreference(dataStore)
                INSTANCE = instance
                instance
            }
        }

    }


    fun getUserToken(): Flow<LoginResult> {
        return dataStore.data.map { preferences ->
            LoginResult(
                preferences[ID_KEY] ?: "",
                preferences[NAME_KEY] ?: "",
                preferences[TOKEN_KEY] ?: ""
            )
        }
    }

    suspend fun userSave(user: LoginResult) {
        dataStore.edit { preferences ->
            preferences[ID_KEY] = user.userId
            preferences[NAME_KEY] = user.name
            preferences[TOKEN_KEY] = user.token
        }
    }

    suspend fun userLogout() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }

}