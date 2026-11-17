package com.example.androidtutorial.core.data.repository

import com.example.androidtutorial.core.model.NewTopic
import com.example.androidtutorial.core.model.Topic
import kotlinx.coroutines.flow.Flow

interface TopicRepository {
    fun observeAll(): Flow<List<Topic>>
    suspend fun add(draft: NewTopic)
    suspend fun deleteUserTopic(id: Long): Boolean
    suspend fun restore(topic: Topic)
    suspend fun setFavorite(id: Long, favorite: Boolean)
}
