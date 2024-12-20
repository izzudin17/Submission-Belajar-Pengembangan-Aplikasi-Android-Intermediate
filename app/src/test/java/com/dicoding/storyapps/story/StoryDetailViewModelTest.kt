package com.dicoding.storyapps.story

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.dicoding.storyapps.DataDummy
import com.dicoding.storyapps.MainDispatcherRule
import com.dicoding.storyapps.uti.Result
import com.dicoding.storyapps.getOrAwaitValue
import com.dicoding.storyapps.local.StoryModel
import com.dicoding.storyapps.repo.StoryRepository
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
class StoryDetailViewModelTest {

    companion object {
        private const val ID = "id"
    }

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Mock
    private lateinit var storyRepository: StoryRepository
    private lateinit var storyDetailViewModel: StoryDetailViewModel

    @Before
    fun setup() {
        storyDetailViewModel = StoryDetailViewModel(storyRepository)
    }

    @Test
    fun `When Get Story Should Not Null and Return Success`() {
        val dummyStory = DataDummy.generateDummyGetAllStoryResponse()
        val expectedStory = MutableLiveData<Result<StoryModel>>()
        expectedStory.value = Result.Success(dummyStory)
        Mockito.`when`(storyRepository.getDetailStories(ID)).thenReturn(expectedStory)

        val actualStory = storyDetailViewModel.getDetailStories(ID).getOrAwaitValue()
        Mockito.verify(storyRepository).getDetailStories(ID)
        assertNotNull(actualStory)
        assertTrue(actualStory is Result.Success)
        assertEquals(dummyStory, (actualStory as Result.Success).data)
    }



}