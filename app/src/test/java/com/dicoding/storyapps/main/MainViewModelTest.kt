package com.dicoding.storyapps.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asFlow
import androidx.paging.AsyncPagingDataDiffer
import androidx.paging.PagingData
import com.dicoding.storyapps.*
import com.dicoding.storyapps.local.StoryModel
import com.dicoding.storyapps.repo.StoryRepository
import com.dicoding.storyapps.response.LoginResult
import com.dicoding.storyapps.story.StoryAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
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
class MainViewModelTest {

    companion object {
        private const val TOKEN = "token"
    }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var mainViewModel: MainViewModel

    @Before
    fun setup() {
        mainViewModel = MainViewModel(storyRepository)
    }

    @Test
    fun `When User Account Should Not Null and Return Success`() {
        val dummyUser = DataDummy.generateDummyUserLoginResponse()
        val expectedUser = MutableLiveData<LoginResult>()
        expectedUser.value = dummyUser
        Mockito.`when`(storyRepository.getUserToken()).thenReturn(expectedUser.asFlow())

        val actualUser = mainViewModel.getUserToken().getOrAwaitValue()
        Mockito.verify(storyRepository).getUserToken()
        assertNotNull(actualUser)
        assertEquals(dummyUser, actualUser)
    }

    @Test
    fun `When Get Story Should Not Null and Return Success`() = runTest {
        val dummyStory = DataDummy.generateDummyStoryResponse().listStory
        val data: PagingData<StoryModel> = PagingDataTestSource.snapshot(dummyStory!!)
        val expectedStory = MutableLiveData<PagingData<StoryModel>>()
        expectedStory.value = data
        Mockito.`when`(storyRepository.getStories(TOKEN)).thenReturn(expectedStory)

        val actualStory: PagingData<StoryModel> = mainViewModel.getStories(TOKEN).getOrAwaitValue()

        val differ = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = NoopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )

        differ.submitData(actualStory)

        assertNotNull(differ.snapshot())
        assertEquals(dummyStory.size, differ.snapshot().size) // Membandingkan jumlah atau ukuran data
        assertEquals(dummyStory[0], differ.snapshot().toList()[0]) // Membandingkan objek data pertama
    }

    @Test
    fun `When Get Story Should Handle Empty Data`() = runTest {
        // Simulasikan repository mengembalikan data cerita kosong
        val expectedEmptyStory = MutableLiveData<PagingData<StoryModel>>()
        expectedEmptyStory.value = PagingData.empty()
        Mockito.`when`(storyRepository.getStories(TOKEN)).thenReturn(expectedEmptyStory)

        val actualEmptyStory: PagingData<StoryModel> = mainViewModel.getStories(TOKEN).getOrAwaitValue()

        // Pastikan bahwa tidak ada data cerita yang dikembalikan
        val differEmpty = AsyncPagingDataDiffer(
            diffCallback = StoryAdapter.DIFF_CALLBACK,
            updateCallback = NoopListUpdateCallback,
            workerDispatcher = Dispatchers.Main
        )
        differEmpty.submitData(actualEmptyStory)

        assertNotNull(differEmpty.snapshot())
        assertEquals(0, differEmpty.snapshot().size) // Membandingkan ukuran data dengan nilai nol
    }

    @Test
    fun `When User Logout Should Success`() = runTest {
        mainViewModel.userLogout()
        Mockito.verify(storyRepository).userLogout()
    }
}