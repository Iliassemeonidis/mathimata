package com.example.androidtutorial.feature.topics.detail

import com.example.androidtutorial.core.model.Topic

data class TopicDetailState(val topic: Topic? = null)

sealed interface TopicDetailIntent {
    data object ToggleFavorite : TopicDetailIntent
}

sealed interface TopicDetailEffect {
    data object Close : TopicDetailEffect
    data object SaveFailed : TopicDetailEffect
}
