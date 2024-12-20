package com.dicoding.storyapps.database

import androidx.lifecycle.LiveData
import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.dicoding.storyapps.local.StoryModel

@Dao
interface StoryDao {

    @Query("SELECT * FROM story")
    fun getStories(): PagingSource<Int, StoryModel>

    @Query("SELECT * FROM story WHERE id = :id LIMIT 1")
    fun getDetailStory(id: String): LiveData<StoryModel>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addStories(stories: List<StoryModel>)

    @Query("DELETE FROM story")
    suspend fun deleteAll()

}