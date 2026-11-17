package com.example.androidtutorial.feature.topics.editor

import com.example.androidtutorial.MainDispatcherRule
import com.example.androidtutorial.core.model.NewTopic
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.feature.topics.FakeTopicRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TopicEditorViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private fun TopicEditorViewModel.fill(title: String = "  Имя  ") {
        onIntent(TopicEditorIntent.Title(title))
        onIntent(TopicEditorIntent.Summary("описание"))
        onIntent(TopicEditorIntent.Body("разбор"))
        onIntent(TopicEditorIntent.Code("код"))
        onIntent(TopicEditorIntent.Category(TopicCategory.KOTLIN))
    }

    @Test
    fun `invalid form flags errors and does not add`() = runTest {
        val repo = FakeTopicRepository()
        val vm = TopicEditorViewModel(repo)
        vm.onIntent(TopicEditorIntent.Title("Имя"))
        vm.onIntent(TopicEditorIntent.Summary("описание"))
        vm.onIntent(TopicEditorIntent.Code("код"))
        vm.onIntent(TopicEditorIntent.Save)
        assertTrue(repo.added.isEmpty())
        assertTrue(vm.state.value.bodyError)
        assertTrue(vm.state.value.categoryError)
    }

    @Test
    fun `valid form adds trimmed topic and emits saved`() = runTest {
        val repo = FakeTopicRepository()
        val vm = TopicEditorViewModel(repo)
        val effects = mutableListOf<TopicEditorEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        vm.fill()
        vm.onIntent(TopicEditorIntent.Save)
        assertEquals(
            listOf(NewTopic("Имя", "описание", "разбор", "код", TopicCategory.KOTLIN)),
            repo.added,
        )
        assertEquals(listOf<TopicEditorEffect>(TopicEditorEffect.Saved), effects)
    }

    @Test
    fun `initial category is preselected`() = runTest {
        val vm = TopicEditorViewModel(FakeTopicRepository(), TopicCategory.ANDROID)
        assertEquals(TopicCategory.ANDROID, vm.state.value.category)
    }

    @Test
    fun `failed save emits save failed only`() = runTest {
        val repo = FakeTopicRepository()
        repo.fail = true
        val vm = TopicEditorViewModel(repo)
        val effects = mutableListOf<TopicEditorEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        vm.fill()
        vm.onIntent(TopicEditorIntent.Save)
        assertEquals(listOf<TopicEditorEffect>(TopicEditorEffect.SaveFailed), effects)
    }
}
