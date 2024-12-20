package com.dicoding.storyapps.repo

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.liveData
import androidx.lifecycle.map
import androidx.paging.*
import com.dicoding.storyapps.api.ApiService
import com.dicoding.storyapps.database.StoryDao
import com.dicoding.storyapps.database.StoryDatabase
import com.dicoding.storyapps.local.StoryModel
import com.dicoding.storyapps.local.userPreference
import com.dicoding.storyapps.mediator.StoryRemoteMediator
import com.dicoding.storyapps.response.*
import com.dicoding.storyapps.uti.Result
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class StoryRepository private constructor(
    private val apiService: ApiService,
    private val storyDao: StoryDao,
    private val storyDatabase: StoryDatabase,
    private val userPreference: userPreference
) {

    companion object {

        @Volatile
        private var INSTANCE: StoryRepository? = null

        fun getInstance(
            apiService: ApiService,
            storyDao: StoryDao,
            storyDatabase: StoryDatabase,
            userPreference: userPreference
        ): StoryRepository =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: StoryRepository(
                    apiService,
                    storyDao,
                    storyDatabase,
                    userPreference
                )
            }.also {
                INSTANCE = it
            }

    }

//    private val loginResult = MediatorLiveData<Result<LoginResult>>()
    private val loginResult = MediatorLiveData<Result<LoginResult>>()
    private val registerResult = MediatorLiveData<Result<RegisterResponse>>()
    private val uploadResult = MediatorLiveData<Result<AddNewStoryResponse>>()
    private val storiesWithLocation = MediatorLiveData<Result<List<StoryModel>>>()


    fun getStories(token: String): LiveData<PagingData<StoryModel>> {
        @OptIn(ExperimentalPagingApi::class)
        return Pager(
            config = PagingConfig(
                pageSize = 5
            ),
            remoteMediator = StoryRemoteMediator(apiService, storyDatabase, token),
            pagingSourceFactory = {
                storyDao.getStories()
            }
        ).liveData
    }

    fun postLogin(email: String, password: String): MediatorLiveData<Result<LoginResult>> {
        loginResult.value = Result.Loading
        val client = apiService.postLogin(email, password)
        client.enqueue(object : Callback<LoginResponse> {

            override fun onResponse(call: Call<LoginResponse>, response: Response<LoginResponse>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    if (!responseBody.error) {
                        val user = LoginResult(
                            responseBody.loginResult.userId,
                            responseBody.loginResult.name,
                            responseBody.loginResult.token
                        )
                        loginResult.value = Result.Success(user)
                    } else {
                        Log.e("LOGIN_ERROR", "onError: ${responseBody.message}")
                        loginResult.value = Result.Error(responseBody.message)
                    }
                } else {
                    Log.e("LOGIN_ERROR", "onError: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<LoginResponse>, t: Throwable) {
                loginResult.value = Result.Error(t.message.toString())
            }

        })

        return loginResult
    }

    fun postRegister(name: String, email: String, password: String): LiveData<Result<RegisterResponse>>{
        registerResult.value = Result.Loading
        val client = apiService.postRegister(name, email, password)
        client.enqueue(object : Callback<RegisterResponse> {

            override fun onResponse(
                call: Call<RegisterResponse>,
                response: Response<RegisterResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    if (!responseBody.error) {
                        registerResult.value = Result.Success(responseBody)
                    } else {
                        registerResult.value = Result.Error(responseBody.message)
                    }
                } else {
                    registerResult.value = Result.Error(response.message().toString())
                }
            }

            override fun onFailure(call: Call<RegisterResponse>, t: Throwable) {
                registerResult.value = Result.Error(t.message.toString())
            }

        })

        return registerResult
    }

    fun getStoriesWithLocation(token: String): MediatorLiveData<Result<List<StoryModel>>> {
        storiesWithLocation.value = Result.Loading
        val client = apiService.getStoriesWithLocation(
            "Bearer $token",
            100,
            true
        )
        client.enqueue(object :  Callback<GetAllStoryResponse> {

            override fun onResponse(
                call: Call<GetAllStoryResponse>,
                response: Response<GetAllStoryResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    if (!responseBody.error) {
                        storiesWithLocation.value = Result.Success(responseBody.listStory!!)
                    } else {
                        storiesWithLocation.value = Result.Error(responseBody.message)
                    }
                } else {
                    storiesWithLocation.value = Result.Error(response.message())
                }
            }

            override fun onFailure(call: Call<GetAllStoryResponse>, t: Throwable) {
                storiesWithLocation.value = Result.Error(t.message.toString())
            }

        })

        return storiesWithLocation
    }

    fun getDetailStories(id: String): LiveData<Result<StoryModel>> = liveData {
        emit(Result.Loading)
        try {
            val localData: LiveData<Result<StoryModel>> = storyDao.getDetailStory(id).map {
                Result.Success(it)
            }
            emitSource(localData)
        } catch (e: Exception) {
            emit(Result.Error(e.message.toString()))
        }
    }

    fun uploadImageStory(
        token: String,
        imageMultipartBody: MultipartBody.Part,
        description: RequestBody,
        lat: RequestBody?,
        lon: RequestBody?
    ): MediatorLiveData<Result<AddNewStoryResponse>> {
        uploadResult.value = Result.Loading
        val client = apiService.postStory("Bearer $token", imageMultipartBody, description, lat, lon)
        client.enqueue(object : Callback<AddNewStoryResponse> {

            override fun onResponse(
                call: Call<AddNewStoryResponse>,
                response: Response<AddNewStoryResponse>
            ) {
                if (response.isSuccessful) {
                    val responseBody = response.body()!!
                    if (!responseBody.error) {
                        uploadResult.value = Result.Success(responseBody)
                    } else {
                        uploadResult.value = Result.Error(responseBody.message)
                    }
                } else {
                    uploadResult.value = Result.Error(response.message())
                }
            }

            override fun onFailure(call: Call<AddNewStoryResponse>, t: Throwable) {
                uploadResult.value = Result.Error(t.message.toString())
            }

        })

        return uploadResult
    }

    fun getUserToken() = userPreference.getUserToken()

    suspend fun userSave(user: LoginResult) {
        userPreference.userSave(user)
    }

    suspend fun userLogout() {
        userPreference.userLogout()
    }
}