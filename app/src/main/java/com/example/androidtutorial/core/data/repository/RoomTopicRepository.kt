package com.example.androidtutorial.core.data.repository

import com.example.androidtutorial.core.data.local.TopicDao
import com.example.androidtutorial.core.data.local.TopicEntity
import com.example.androidtutorial.core.model.NewTopic
import com.example.androidtutorial.core.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class RoomTopicRepository(
    private val dao: TopicDao,
    private val clock: Clock,
) : TopicRepository {
    override fun observeAll(): Flow<List<Topic>> =
        dao.observeAll().map { entities -> entities.map { it.toTopic() } }

    override suspend fun add(draft: NewTopic) {
        dao.insert(
            TopicEntity(
                id = 0,
                title = draft.title,
                summary = draft.summary,
                body = draft.body,
                codeSample = draft.codeSample,
                category = draft.category,
                isFavorite = false,
                isBuiltIn = false,
                sortOrder = 0,
                createdAt = clock.now(),
            ),
        )
    }

    override suspend fun deleteUserTopic(id: Long): Boolean = dao.deleteUser(id) > 0

    override suspend fun restore(topic: Topic) {
        dao.restore(topic.toEntity())
    }

    override suspend fun setFavorite(id: Long, favorite: Boolean) {
        dao.updateFavorite(id, favorite)
    }
}

private fun TopicEntity.toTopic() = Topic(
    id = id,
    title = title,
    summary = summary,
    body = body,
    codeSample = codeSample,
    category = category,
    isFavorite = isFavorite,
    isBuiltIn = isBuiltIn,
    sortOrder = sortOrder,
    createdAt = createdAt,
)

private fun Topic.toEntity() = TopicEntity(
    id = id,
    title = title,
    summary = summary,
    body = body,
    codeSample = codeSample,
    category = category,
    isFavorite = isFavorite,
    isBuiltIn = isBuiltIn,
    sortOrder = sortOrder,
    createdAt = createdAt,
)
