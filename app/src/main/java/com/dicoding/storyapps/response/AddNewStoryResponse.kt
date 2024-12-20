package com.dicoding.storyapps.response

import com.dicoding.storyapps.local.StoryModel
import com.google.gson.annotations.SerializedName

data class AddNewStoryResponse(
    @field:SerializedName("error")
    val error: Boolean,

    @field:SerializedName("message")
    val message: String,

    @field:SerializedName("ListStory")
    val listStory: List<StoryModel?>?

)