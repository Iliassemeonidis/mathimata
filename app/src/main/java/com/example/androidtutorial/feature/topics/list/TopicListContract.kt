package com.example.androidtutorial.feature.topics.list

import com.example.androidtutorial.core.model.Topic
import kotlin.random.Random

fun interface RandomSource {
    fun next(): Random
}

data class TopicListState(
    val topics: List<Topic> = emptyList(),
    val errorVisible: Boolean = false,
)

sealed interface TopicListIntent {
    data class Visit(val visit: Int) : TopicListIntent
    data class ToggleFavorite(val id: Long) : TopicListIntent
    data class Delete(val topic: Topic) : TopicListIntent
    data object UndoDelete : TopicListIntent
    data object DismissError : TopicListIntent
}

sealed interface TopicListEffect {
    data class ShowUndo(val topic: Topic) : TopicListEffect
    data object ShowError : TopicListEffect
}
