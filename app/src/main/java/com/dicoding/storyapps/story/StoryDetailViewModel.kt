package com.dicoding.storyapps.story

import androidx.lifecycle.ViewModel
import com.dicoding.storyapps.repo.StoryRepository

class StoryDetailViewModel(private val storyRepository: StoryRepository): ViewModel() {

    fun getDetailStories(id: String) = storyRepository.getDetailStories(id)

}