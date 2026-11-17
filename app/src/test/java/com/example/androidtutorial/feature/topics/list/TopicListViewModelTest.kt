package com.example.androidtutorial.feature.topics.list

import com.example.androidtutorial.MainDispatcherRule
import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.core.model.TopicFilter
import com.example.androidtutorial.feature.topics.FakeTopicRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.random.Random

@OptIn(ExperimentalCoroutinesApi::class)
class TopicListViewModelTest {
    @get:Rule
    val mainRule = MainDispatcherRule()

    private val topics = listOf(
        topic(1, TopicCategory.KOTLIN, builtIn = true, sort = 1),
        topic(2, TopicCategory.KOTLIN, builtIn = true, sort = 2),
        topic(3, TopicCategory.ANDROID, builtIn = true, sort = 1),
        topic(4, TopicCategory.KOTLIN, builtIn = false, sort = 0),
    )

    @Before
    fun resetHomeOrder() {
        TopicOrdering.startHomeVisit()
    }

    private fun topic(id: Long, category: TopicCategory, builtIn: Boolean, sort: Int) = Topic(
        id = id,
        title = "t$id",
        summary = "s$id",
        body = "b$id",
        codeSample = "c$id",
        category = category,
        isFavorite = false,
        isBuiltIn = builtIn,
        sortOrder = sort,
        createdAt = id,
    )

    private fun seededRandom(): RandomSource {
        val seeds = ArrayDeque(listOf(7, 8))
        return RandomSource { Random(seeds.removeFirst()) }
    }

    private fun TopicListViewModel.ids() = state.value.topics.map { it.id }

    @Test
    fun `home visits shuffle once per visit and keep order between emissions`() = runTest {
        val repo = FakeTopicRepository(topics)
        val vm = TopicListViewModel(repo, seededRandom(), TopicFilter.ALL)
        assertEquals(emptyList<Long>(), vm.ids())

        vm.onIntent(TopicListIntent.Visit(1))
        val first = topics.shuffled(Random(7)).map { it.id }
        assertEquals(first, vm.ids())

        repo.topics.value = topics.reversed()
        assertEquals(first, vm.ids())

        repo.topics.value = topics
        assertEquals(first, vm.ids())

        vm.onIntent(TopicListIntent.Visit(2))
        assertEquals(topics.shuffled(Random(8)).map { it.id }, vm.ids())
    }

    @Test
    fun `kotlin filter lists built-ins then own topics without randomness`() = runTest {
        val repo = FakeTopicRepository(topics)
        val vm = TopicListViewModel(
            repo,
            RandomSource { error("random must not be requested") },
            TopicFilter.KOTLIN,
        )
        vm.onIntent(TopicListIntent.Visit(1))
        assertEquals(listOf(1L, 2L, 4L), vm.ids())
    }

    @Test
    fun `deleting own topic emits undo and undo restores it`() = runTest {
        val repo = FakeTopicRepository(topics)
        val vm = TopicListViewModel(repo, RandomSource { error("no") }, TopicFilter.KOTLIN)
        val effects = mutableListOf<TopicListEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        vm.onIntent(TopicListIntent.Visit(1))

        vm.onIntent(TopicListIntent.Delete(topics[3]))
        assertEquals(listOf(1L, 2L), vm.ids())
        assertEquals(listOf<TopicListEffect>(TopicListEffect.ShowUndo(topics[3])), effects)

        vm.onIntent(TopicListIntent.UndoDelete)
        assertEquals(listOf(1L, 2L, 4L), vm.ids())
        assertEquals(listOf(topics[3]), repo.restored)
    }

    @Test
    fun `deleting built-in topic changes nothing`() = runTest {
        val repo = FakeTopicRepository(topics)
        val vm = TopicListViewModel(repo, RandomSource { error("no") }, TopicFilter.KOTLIN)
        val effects = mutableListOf<TopicListEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        vm.onIntent(TopicListIntent.Visit(1))

        vm.onIntent(TopicListIntent.Delete(topics[0]))
        assertEquals(listOf(1L, 2L, 4L), vm.ids())
        assertTrue(effects.none { it is TopicListEffect.ShowUndo })
    }

    @Test
    fun `failed favorite toggle emits error and keeps flag`() = runTest {
        val repo = FakeTopicRepository(topics)
        val vm = TopicListViewModel(repo, RandomSource { error("no") }, TopicFilter.KOTLIN)
        val effects = mutableListOf<TopicListEffect>()
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.effects.toList(effects) }
        vm.onIntent(TopicListIntent.Visit(1))

        repo.fail = true
        vm.onIntent(TopicListIntent.ToggleFavorite(1))
        assertEquals(listOf<TopicListEffect>(TopicListEffect.ShowError), effects)
        assertFalse(vm.state.value.topics.first { it.id == 1L }.isFavorite)
        assertTrue(vm.state.value.errorVisible)

        vm.onIntent(TopicListIntent.DismissError)
        assertFalse(vm.state.value.errorVisible)
    }
}
