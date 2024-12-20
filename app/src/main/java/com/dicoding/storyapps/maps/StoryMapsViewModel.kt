package com.dicoding.storyapps.maps

import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.dicoding.storyapps.repo.StoryRepository

class StoryMapsViewModel(private val storyRepository: StoryRepository): ViewModel() {

    fun getUserToken() = storyRepository.getUserToken().asLiveData()

    fun getStoriesWithLocation(token: String) = storyRepository.getStoriesWithLocation(token)

}