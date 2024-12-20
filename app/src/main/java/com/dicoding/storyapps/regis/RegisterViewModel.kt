package com.dicoding.storyapps.regis

import androidx.lifecycle.ViewModel
import com.dicoding.storyapps.repo.StoryRepository

class RegisterViewModel(private val storyRepository: StoryRepository): ViewModel() {

    fun postRegister(name: String, email: String, password: String) =
        storyRepository.postRegister(name, email, password)

}