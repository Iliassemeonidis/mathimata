package com.example.androidtutorial.core.model

data class Topic(
    val id: Long,
    val title: String,
    val summary: String,
    val body: String,
    val codeSample: String,
    val category: TopicCategory,
    val isFavorite: Boolean,
    val isBuiltIn: Boolean,
    val sortOrder: Int,
    val createdAt: Long,
)
