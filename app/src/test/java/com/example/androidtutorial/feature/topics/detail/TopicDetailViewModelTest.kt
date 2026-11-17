package com.example.androidtutorial.feature.topics.detail

import com.example.androidtutorial.MainDispatcherRule
import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.feature.topics.FakeTopicRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TopicDetailViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val topic = Topic(
        id = 5,
        title = "t",
        summary = "s",
        body = "b",
        codeSample = "c",
        category = TopicCategory.KOTLIN,
        isFavorite = false,
        isBuiltIn = false,
        sortOrder = 0,
        createdAt = 1,
    )

    @Test
    fun `known id exposes topic`() = runTest {
        val vm = TopicDetailViewModel(FakeTopicRepository(listOf(topic)), 5)
        assertEquals(topic, vm.state.value.topic)
    }

    @Test
    fun `removal emits close once`() = runTest {
        val repo = FakeTopicRepository(listOf(topic))
        val vm = TopicDetailViewModel(repo, 5)
        val effects = mutableListOf<TopicDetailEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        repo.topics.value = emptyList()
        assertEquals(listOf<TopicDetailEffect>(TopicDetailEffect.Close), effects)
    }

    @Test
    fun `toggle favorite flips flag`() = runTest {
        val repo = FakeTopicRepository(listOf(topic))
        val vm = TopicDetailViewModel(repo, 5)
        vm.onIntent(TopicDetailIntent.ToggleFavorite)
        assertTrue(repo.topics.value.single().isFavorite)
        assertTrue(vm.state.value.topic!!.isFavorite)
    }

    @Test
    fun `failed toggle emits save failed and keeps flag`() = runTest {
        val repo = FakeTopicRepository(listOf(topic))
        val vm = TopicDetailViewModel(repo, 5)
        val effects = mutableListOf<TopicDetailEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        repo.fail = true
        vm.onIntent(TopicDetailIntent.ToggleFavorite)
        assertEquals(listOf<TopicDetailEffect>(TopicDetailEffect.SaveFailed), effects)
        assertFalse(repo.topics.value.single().isFavorite)
    }
}
