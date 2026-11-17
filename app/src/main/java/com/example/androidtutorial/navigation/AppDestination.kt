package com.example.androidtutorial.navigation

import com.example.androidtutorial.core.model.TopicCategory

object AppDestination {
    const val Auth = "auth"
    const val Shell = "shell"
    const val Home = "topics/all"
    const val Kotlin = "topics/kotlin"
    const val Android = "topics/android"
    const val Study = "study"
    const val Favorites = "topics/favorites"
    const val Detail = "topic/{topicId}"
    const val Editor = "editor?category={category}"
    const val Profile = "profile"

    fun detail(id: Long) = "topic/$id"

    fun editor(category: TopicCategory?) =
        if (category == null) "editor?category=" else "editor?category=${category.name}"
}
