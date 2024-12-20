package com.dicoding.storyapps.story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import com.dicoding.storyapps.DataDummy
import com.dicoding.storyapps.MainDispatcherRule
import com.dicoding.storyapps.getOrAwaitValue
import com.dicoding.storyapps.repo.StoryRepository
import com.dicoding.storyapps.response.AddNewStoryResponse
import com.dicoding.storyapps.response.LoginResult
import com.dicoding.storyapps.uti.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.mock
import org.mockito.junit.MockitoJUnitRunner
import java.io.File

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AddNewStoryViewModelTest{

    companion object {
        private const val TOKEN = "token"
    }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var addNewStoryViewModel: AddNewStoryViewModel

    @Before
    fun setup() {
        addNewStoryViewModel = AddNewStoryViewModel(storyRepository)
    }

    @Test
    fun `When Upload Story Should Not Null and Result Success`() {
        val file = mock(File::class.java)
        val requestImageFile = file.asRequestBody("image/jpg".toMediaTypeOrNull())
        val imageMultipartBody = MultipartBody.Part.createFormData(
            "photo",
            file.name,
            requestImageFile
        )
        val description = "This is pict desc".toRequestBody("text/plain".toMediaType())
        val lat = null
        val lon = null

        val dummyStory = DataDummy.generateDummyAddNewStoryResponse()
        val expectedStory = MediatorLiveData<Result<AddNewStoryResponse>>()
        expectedStory.value = Result.Success(dummyStory)
        Mockito.`when`(storyRepository.uploadImageStory(TOKEN, imageMultipartBody, description, lat, lon)).thenReturn(expectedStory)

        val actualStory = addNewStoryViewModel.uploadImage(TOKEN, imageMultipartBody, description, lat, lon).getOrAwaitValue()
        assertNotNull(actualStory)
        assertTrue(actualStory is Result.Success)
    }

    @Test
    fun `When User Account Should Not Null and Return Success`() {
        val dummyUser = DataDummy.generateDummyUserLoginResponse()
        val expectedUser = MutableLiveData<LoginResult>()
        expectedUser.value = dummyUser
        Mockito.`when`(storyRepository.getUserToken()).thenReturn(expectedUser.asFlow())

        val actualUser = addNewStoryViewModel.getUserToken().getOrAwaitValue()
        Mockito.verify(storyRepository).getUserToken()
        assertNotNull(actualUser)
        assertEquals(dummyUser, actualUser)
    }

}