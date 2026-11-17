package com.example.androidtutorial.feature.topics.editor

import com.example.androidtutorial.core.model.TopicCategory

data class TopicEditorState(
    val title: String = "",
    val summary: String = "",
    val body: String = "",
    val codeSample: String = "",
    val category: TopicCategory? = null,
    val titleError: Boolean = false,
    val summaryError: Boolean = false,
    val bodyError: Boolean = false,
    val codeError: Boolean = false,
    val categoryError: Boolean = false,
)

sealed interface TopicEditorIntent {
    data class Title(val value: String) : TopicEditorIntent
    data class Summary(val value: String) : TopicEditorIntent
    data class Body(val value: String) : TopicEditorIntent
    data class Code(val value: String) : TopicEditorIntent
    data class Category(val value: TopicCategory) : TopicEditorIntent
    data object Save : TopicEditorIntent
}

sealed interface TopicEditorEffect {
    data object Saved : TopicEditorEffect
    data object SaveFailed : TopicEditorEffect
}
