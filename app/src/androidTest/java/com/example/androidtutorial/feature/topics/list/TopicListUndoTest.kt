package com.example.androidtutorial.feature.topics.list

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeLeft
import com.example.androidtutorial.core.data.repository.TopicRepository
import com.example.androidtutorial.core.design.MathimataTheme
import com.example.androidtutorial.core.model.NewTopic
import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.core.model.TopicFilter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

class TopicListUndoTest {
    @get:Rule
    val rule = createComposeRule()

    private val userTopic = Topic(
        id = 7,
        title = "Моя тема",
        summary = "Краткое описание",
        body = "Разбор",
        codeSample = "val a = 1",
        category = TopicCategory.KOTLIN,
        isFavorite = false,
        isBuiltIn = false,
        sortOrder = 0,
        createdAt = 10,
    )

    private class CountingRepository(initial: List<Topic>) : TopicRepository {
        val topics = MutableStateFlow(initial)
        var deletes = 0

        override fun observeAll(): Flow<List<Topic>> = topics

        override suspend fun add(draft: NewTopic) = Unit

        override suspend fun deleteUserTopic(id: Long): Boolean {
            val target = topics.value.firstOrNull { it.id == id } ?: return false
            deletes++
            topics.value = topics.value - target
            return true
        }

        override suspend fun restore(topic: Topic) {
            topics.value = topics.value + topic
        }

        override suspend fun setFavorite(id: Long, favorite: Boolean) = Unit
    }

    @Test
    fun undoneTopicStaysInTheList() {
        val repository = CountingRepository(listOf(userTopic))
        val viewModel = TopicListViewModel(repository, { error("no shuffle") }, TopicFilter.KOTLIN)
        var undoOffers = 0
        rule.setContent {
            MathimataTheme {
                TopicListScreen(
                    viewModel = viewModel,
                    visit = 0,
                    showAdd = false,
                    addCategory = null,
                    emptyText = null,
                    onOpenTopic = {},
                    onAdd = {},
                    onEffect = { _, effect ->
                        if (effect is TopicListEffect.ShowUndo) undoOffers++
                    },
                )
            }
        }

        rule.onNodeWithText("Моя тема").performTouchInput { swipeLeft() }
        rule.waitUntil(timeoutMillis = 5_000) { undoOffers == 1 }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Моя тема").fetchSemanticsNodes().isEmpty()
        }

        rule.runOnIdle { viewModel.onIntent(TopicListIntent.UndoDelete) }
        rule.waitUntil(timeoutMillis = 5_000) {
            rule.onAllNodesWithText("Моя тема").fetchSemanticsNodes().isNotEmpty()
        }
        rule.mainClock.advanceTimeBy(1_000)
        rule.waitForIdle()

        assertEquals(1, repository.deletes)
        assertEquals(1, undoOffers)
        rule.onNodeWithText("Моя тема").assertIsDisplayed()
    }
}
