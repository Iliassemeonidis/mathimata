package com.example.androidtutorial.core.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface TopicDao {
    @Query("SELECT * FROM topics")
    fun observeAll(): Flow<List<TopicEntity>>

    @Insert
    suspend fun insert(entity: TopicEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun restore(entity: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteUser(id: Long): Int

    @Query("UPDATE topics SET isFavorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, favorite: Boolean)
}
