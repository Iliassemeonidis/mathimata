package com.example.androidtutorial.feature.topics.list

import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.core.model.TopicFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class TopicOrderingTest {
    private fun topic(
        id: Long,
        category: TopicCategory,
        builtIn: Boolean,
        sortOrder: Int,
        createdAt: Long,
        favorite: Boolean = false,
    ) = Topic(
        id = id,
        title = "t$id",
        summary = "s",
        body = "b",
        codeSample = "c",
        category = category,
        isFavorite = favorite,
        isBuiltIn = builtIn,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

    @Test
    fun sectionPutsBuiltInsBeforeUserTopics() {
        val topics = listOf(
            topic(1, TopicCategory.KOTLIN, builtIn = false, sortOrder = 0, createdAt = 20),
            topic(2, TopicCategory.KOTLIN, builtIn = true, sortOrder = 2, createdAt = 0),
            topic(3, TopicCategory.KOTLIN, builtIn = true, sortOrder = 1, createdAt = 0),
            topic(4, TopicCategory.ANDROID, builtIn = true, sortOrder = 1, createdAt = 0),
        )
        val ids = TopicOrdering.arrange(topics, TopicFilter.KOTLIN) { error("no shuffle") }
            .map { it.id }
        assertEquals(listOf(3L, 2L, 1L), ids)
    }

    @Test
    fun favoritesGroupKotlinBeforeAndroid() {
        val topics = listOf(
            topic(1, TopicCategory.ANDROID, true, 1, 0, favorite = true),
            topic(2, TopicCategory.KOTLIN, false, 0, 5, favorite = true),
            topic(3, TopicCategory.KOTLIN, true, 1, 0, favorite = true),
            topic(4, TopicCategory.KOTLIN, true, 2, 0, favorite = false),
        )
        val ids = TopicOrdering.arrange(topics, TopicFilter.FAVORITES) { error("no shuffle") }
            .map { it.id }
        assertEquals(listOf(3L, 2L, 1L), ids)
    }

    @Test
    fun homeUsesTheProvidedShuffle() {
        val topics = listOf(
            topic(1, TopicCategory.KOTLIN, true, 1, 0),
            topic(2, TopicCategory.ANDROID, true, 1, 0),
        )
        val ids = TopicOrdering.arrange(topics, TopicFilter.ALL) { it.reversed() }.map { it.id }
        assertEquals(listOf(2L, 1L), ids)
    }
}
