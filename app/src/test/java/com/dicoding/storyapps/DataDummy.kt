package com.dicoding.storyapps

import com.dicoding.storyapps.local.StoryModel
import com.dicoding.storyapps.response.AddNewStoryResponse
import com.dicoding.storyapps.response.GetAllStoryResponse
import com.dicoding.storyapps.response.LoginResult
import com.dicoding.storyapps.response.RegisterResponse


object DataDummy {

    fun generateDummyGetAllStoryResponse(): StoryModel {
        return StoryModel(
            "id",
            "name",
            "description",
            "https://dicoding-web-img.sgp1.cdn.digitaloceanspaces.com/original/commons/feature-1-kurikulum-global-3.png",
            "2022-02-22T22:22:22Z",
            null,
            null,
        )
    }

    fun generateDummyStoryResponse(): GetAllStoryResponse {
        val items: MutableList<StoryModel> = arrayListOf()
        for (i in 1..100) {
            val story = StoryModel(
                "id $i",
                "name",
                "description",
                "https://dicoding-web-img.sgp1.cdn.digitaloceanspaces.com/original/commons/feature-1-kurikulum-global-3.png",
                "2022-02-22T22:22:22Z",
                null,
                null,
            )
            items.add(story)
        }
        return GetAllStoryResponse(
            false,
            "Story data is read",
            items
        )
    }

    fun generateDummyStoryMapsResponse(): GetAllStoryResponse {
        val items: MutableList<StoryModel> = arrayListOf()
        for (i in 0..100) {
            val story = StoryModel(
                "id $i",
                "name",
                "description",
                "https://dicoding-web-img.sgp1.cdn.digitaloceanspaces.com/original/commons/feature-1-kurikulum-global-3.png",
                "2022-02-22T22:22:22Z",
                -7.331406,
                112.782157,
            )
            items.add(story)
        }
        return GetAllStoryResponse(
            false,
            "Story data is read",
            items
        )
    }

    fun generateDummyUserLoginResponse(): LoginResult {
        return LoginResult(
            "id",
            "name",
            "token"
        )
    }

    fun generateDummyUserRegisterResponse(): RegisterResponse {
        return RegisterResponse(
            false,
            "User Created"
        )
    }

    fun generateDummyAddNewStoryResponse(): AddNewStoryResponse {
        return AddNewStoryResponse(
            false,
            "Success",
            null
        )
    }

}