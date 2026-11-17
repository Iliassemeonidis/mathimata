package com.example.androidtutorial.feature.topics

import com.example.androidtutorial.core.data.repository.TopicRepository
import com.example.androidtutorial.core.model.NewTopic
import com.example.androidtutorial.core.model.Topic
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeTopicRepository(initial: List<Topic> = emptyList()) : TopicRepository {
    val topics = MutableStateFlow(initial)
    val added = mutableListOf<NewTopic>()
    val restored = mutableListOf<Topic>()
    var fail = false

    override fun observeAll(): Flow<List<Topic>> = topics

    override suspend fun add(draft: NewTopic) {
        check(!fail) { "db" }
        added += draft
        val id = (topics.value.maxOfOrNull { it.id } ?: 0L) + 1
        topics.value = topics.value + Topic(
            id = id,
            title = draft.title,
            summary = draft.summary,
            body = draft.body,
            codeSample = draft.codeSample,
            category = draft.category,
            isFavorite = false,
            isBuiltIn = false,
            sortOrder = 0,
            createdAt = 50,
        )
    }

    override suspend fun deleteUserTopic(id: Long): Boolean {
        check(!fail) { "db" }
        val target = topics.value.firstOrNull { it.id == id && !it.isBuiltIn } ?: return false
        topics.value = topics.value - target
        return true
    }

    override suspend fun restore(topic: Topic) {
        check(!fail) { "db" }
        restored += topic
        topics.value = topics.value + topic
    }

    override suspend fun setFavorite(id: Long, favorite: Boolean) {
        check(!fail) { "db" }
        topics.value = topics.value.map { if (it.id == id) it.copy(isFavorite = favorite) else it }
    }
}
