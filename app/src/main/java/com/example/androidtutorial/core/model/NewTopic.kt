package com.example.androidtutorial.core.model

data class NewTopic(
    val title: String,
    val summary: String,
    val body: String,
    val codeSample: String,
    val category: TopicCategory,
)
