package com.dicoding.storyapps.regis

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.dicoding.storyapps.DataDummy
import com.dicoding.storyapps.MainDispatcherRule
import com.dicoding.storyapps.getOrAwaitValue
import com.dicoding.storyapps.repo.StoryRepository
import com.dicoding.storyapps.response.RegisterResponse
import com.dicoding.storyapps.uti.Result
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.junit.MockitoJUnitRunner

@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class RegisterViewModelTest {

    companion object {
        private const val NAME = "name"
        private const val EMAIL = "email"
        private const val PASSWORD = "password"
    }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var registerViewModel: RegisterViewModel

    @Before
    fun setup() {
        registerViewModel = RegisterViewModel(storyRepository)
    }

    @Test
    fun `When User Register Should Not Null and Return Success`() {
        val dummyUser = DataDummy.generateDummyUserRegisterResponse()
        val expectedUser = MutableLiveData<Result<RegisterResponse>>()
        expectedUser.value = Result.Success(dummyUser)
        Mockito.`when`(storyRepository.postRegister(NAME, EMAIL, PASSWORD)).thenReturn(expectedUser)

        val actualUser = registerViewModel.postRegister(NAME, EMAIL, PASSWORD).getOrAwaitValue()
        Mockito.verify(storyRepository).postRegister(NAME, EMAIL, PASSWORD)
        assertNotNull(actualUser)
        assertTrue(actualUser is Result.Success)
    }

}