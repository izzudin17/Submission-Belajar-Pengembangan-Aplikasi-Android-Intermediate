package com.dicoding.storyapps.login

import androidx.lifecycle.*
import com.dicoding.storyapps.repo.StoryRepository
import com.dicoding.storyapps.response.LoginResult
import kotlinx.coroutines.*

class LoginViewModel(private val storyRepository: StoryRepository): ViewModel() {

    fun postLogin(email: String, password: String) =
        storyRepository.postLogin(email, password)

    fun userSave(user: LoginResult) {
        viewModelScope.launch {
            storyRepository.userSave(user)
        }
    }
}