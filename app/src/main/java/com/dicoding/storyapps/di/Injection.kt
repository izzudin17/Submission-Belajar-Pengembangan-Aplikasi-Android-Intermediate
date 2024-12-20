package com.dicoding.storyapps.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.dicoding.storyapps.api.ApiConfig
import com.dicoding.storyapps.database.StoryDatabase
import com.dicoding.storyapps.local.userPreference
import com.dicoding.storyapps.repo.StoryRepository

object Injection {
    fun provideRepository(
        context: Context,
        dataStore: DataStore<Preferences>
    ): StoryRepository {
        val apiService = ApiConfig.getApiService()
        val storyDatabase = StoryDatabase.getInstance(context)
        val storyDao = storyDatabase.storyDao()
        val userPreference = userPreference.getInstance(dataStore)

        return StoryRepository.getInstance(apiService, storyDao, storyDatabase, userPreference)
    }
}