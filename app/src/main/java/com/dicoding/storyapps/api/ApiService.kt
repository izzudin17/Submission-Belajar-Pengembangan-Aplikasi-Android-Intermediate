package com.dicoding.storyapps.api

import com.dicoding.storyapps.response.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.*

interface ApiService {

    @FormUrlEncoded
    @POST("v1/register")
    fun postRegister(
        @Field("name") name: String,
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<RegisterResponse>

    @FormUrlEncoded
    @POST("v1/login")
    fun postLogin(
        @Field("email") email: String,
        @Field("password") password: String
    ): Call<LoginResponse>

    @Multipart
    @POST("v1/stories")
    fun postStory(
        @Header("Authorization") Bearer: String,
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Part("lat") lat: RequestBody? = null,
        @Part("lon") lon: RequestBody? = null
    ): Call<AddNewStoryResponse>

    @GET("v1/stories")
    fun getStoriesWithLocation(
        @Header("Authorization") Bearer: String,
        @Query("size") size: Int? = null,
        @Query("location") location: Boolean? = null
    ): Call<GetAllStoryResponse>

    @GET("v1/stories")
    suspend fun getStoryList(
        @Header("Authorization") Bearer: String,
        @Query("page") page: Int? = null,
        @Query("size") size: Int? = null
    ):GetAllStoryResponse

}