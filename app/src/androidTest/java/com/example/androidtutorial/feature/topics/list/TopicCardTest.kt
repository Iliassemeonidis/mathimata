package com.example.androidtutorial.feature.topics.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.example.androidtutorial.core.design.MathimataTheme
import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TopicCardTest {
    @get:Rule
    val rule = createComposeRule()

    private fun topic(
        id: Long,
        title: String,
        category: TopicCategory,
        builtIn: Boolean,
        favorite: Boolean = false,
    ) = Topic(
        id = id,
        title = title,
        summary = "Краткое описание",
        body = "Разбор",
        codeSample = "val a = 1",
        category = category,
        isFavorite = favorite,
        isBuiltIn = builtIn,
        sortOrder = 1,
        createdAt = 0,
    )

    @Test
    fun builtInCardShowsLetterBadgeOnly() {
        rule.setContent {
            MathimataTheme {
                TopicCard(
                    topic = topic(1, "Null safety", TopicCategory.KOTLIN, builtIn = true),
                    onOpen = {},
                    onToggleFavorite = {},
                )
            }
        }
        rule.onNodeWithContentDescription("Тема Kotlin").assertIsDisplayed()
        rule.onNodeWithContentDescription("Своя тема").assertDoesNotExist()
    }

    @Test
    fun userCardShowsAvatarWithoutLetterBadge() {
        rule.setContent {
            MathimataTheme {
                TopicCard(
                    topic = topic(2, "Моя тема", TopicCategory.ANDROID, builtIn = false),
                    onOpen = {},
                    onToggleFavorite = {},
                )
            }
        }
        rule.onNodeWithContentDescription("Своя тема").assertIsDisplayed()
        rule.onNodeWithContentDescription("Тема Android").assertDoesNotExist()
        rule.onNodeWithContentDescription("Тема Kotlin").assertDoesNotExist()
    }

    @Test
    fun heartClickTogglesFavoriteWithoutOpening() {
        var opened = 0
        var toggled = 0
        rule.setContent {
            MathimataTheme {
                TopicCard(
                    topic = topic(3, "Корутины", TopicCategory.KOTLIN, builtIn = true, favorite = true),
                    onOpen = { opened++ },
                    onToggleFavorite = { toggled++ },
                )
            }
        }
        rule.onNodeWithContentDescription("В избранном").performClick()
        assertEquals(1, toggled)
        assertEquals(0, opened)
    }

    @Test
    fun titleClickOpensTopic() {
        var opened = 0
        var toggled = 0
        rule.setContent {
            MathimataTheme {
                TopicCard(
                    topic = topic(4, "Корутины", TopicCategory.KOTLIN, builtIn = true),
                    onOpen = { opened++ },
                    onToggleFavorite = { toggled++ },
                )
            }
        }
        rule.onNodeWithText("Корутины").performClick()
        assertEquals(1, opened)
        assertEquals(0, toggled)
        rule.onNodeWithContentDescription("Не в избранном").assertIsDisplayed()
    }
}
