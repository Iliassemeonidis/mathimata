package com.example.androidtutorial.feature.topics.list

import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.core.model.TopicFilter

object TopicOrdering {
    fun arrange(
        topics: List<Topic>,
        filter: TopicFilter,
        shuffle: (List<Topic>) -> List<Topic>,
    ): List<Topic> {
        val filtered = when (filter) {
            TopicFilter.ALL -> topics
            TopicFilter.KOTLIN -> topics.filter { it.category == TopicCategory.KOTLIN }
            TopicFilter.ANDROID -> topics.filter { it.category == TopicCategory.ANDROID }
            TopicFilter.FAVORITES -> topics.filter { it.isFavorite }
        }
        if (filter == TopicFilter.ALL) return shuffle(filtered)
        return when (filter) {
            TopicFilter.KOTLIN -> block(TopicCategory.KOTLIN, filtered)
            TopicFilter.ANDROID -> block(TopicCategory.ANDROID, filtered)
            TopicFilter.FAVORITES -> block(TopicCategory.KOTLIN, filtered) + block(TopicCategory.ANDROID, filtered)
            TopicFilter.ALL -> error("home is shuffled above")
        }
    }

    private fun block(category: TopicCategory, topics: List<Topic>): List<Topic> {
        val matching = topics.filter { it.category == category }
        val builtIn = matching.filter { it.isBuiltIn }.sortedBy { it.sortOrder }
        val own = matching.filter { !it.isBuiltIn }.sortedBy { it.createdAt }
        return builtIn + own
    }
}
