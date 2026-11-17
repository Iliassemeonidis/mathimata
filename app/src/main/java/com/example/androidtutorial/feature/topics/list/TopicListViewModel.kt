package com.example.androidtutorial.feature.topics.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidtutorial.core.data.repository.TopicRepository
import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicFilter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.random.Random

class TopicListViewModel(
    private val repository: TopicRepository,
    private val randomSource: RandomSource,
    private val filter: TopicFilter,
) : ViewModel() {
    private val _state = MutableStateFlow(TopicListState())
    val state: StateFlow<TopicListState> = _state.asStateFlow()

    private val _effects = Channel<TopicListEffect>(Channel.BUFFERED)
    val effects: Flow<TopicListEffect> = _effects.receiveAsFlow()

    private var raw: List<Topic>? = null
    private var visit: Int? = null
    private var random: Random? = null
    private var lastDeleted: Topic? = null

    init {
        viewModelScope.launch {
            repository.observeAll()
                .catch { showError() }
                .collect {
                    raw = it
                    publish()
                }
        }
    }

    fun onIntent(intent: TopicListIntent) {
        when (intent) {
            is TopicListIntent.Visit -> visit(intent.visit)
            is TopicListIntent.ToggleFavorite -> toggleFavorite(intent.id)
            is TopicListIntent.Delete -> delete(intent.topic)
            TopicListIntent.UndoDelete -> undoDelete()
            TopicListIntent.DismissError -> _state.update { it.copy(errorVisible = false) }
        }
    }

    private fun visit(number: Int) {
        if (visit == number) return
        visit = number
        if (filter == TopicFilter.ALL) {
            TopicOrdering.startHomeVisit()
            random = randomSource.next()
        }
        publish()
    }

    private fun publish() {
        val list = raw ?: return
        val arranged = if (filter == TopicFilter.ALL) {
            val current = random ?: return
            TopicOrdering.arrange(list, filter) { it.shuffled(current) }
        } else {
            TopicOrdering.arrange(list, filter) { it }
        }
        _state.update { it.copy(topics = arranged) }
    }

    private fun toggleFavorite(id: Long) {
        val topic = raw?.firstOrNull { it.id == id } ?: return
        viewModelScope.launch {
            guarded { repository.setFavorite(id, !topic.isFavorite) }
        }
    }

    private fun delete(topic: Topic) {
        viewModelScope.launch {
            guarded {
                if (repository.deleteUserTopic(topic.id)) {
                    lastDeleted = topic
                    _effects.send(TopicListEffect.ShowUndo(topic))
                }
            }
        }
    }

    private fun undoDelete() {
        val topic = lastDeleted ?: return
        viewModelScope.launch {
            guarded {
                repository.restore(topic)
                lastDeleted = null
            }
        }
    }

    private suspend fun guarded(block: suspend () -> Unit) {
        try {
            block()
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            showError()
        }
    }

    private suspend fun showError() {
        _state.update { it.copy(errorVisible = true) }
        _effects.send(TopicListEffect.ShowError)
    }
}
