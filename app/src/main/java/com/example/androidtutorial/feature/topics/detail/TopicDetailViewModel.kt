package com.example.androidtutorial.feature.topics.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidtutorial.core.data.repository.TopicRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TopicDetailViewModel.Factory::class)
class TopicDetailViewModel @AssistedInject constructor(
    private val repository: TopicRepository,
    @Assisted private val topicId: Long,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(topicId: Long): TopicDetailViewModel
    }

    private val _state = MutableStateFlow(TopicDetailState())
    val state: StateFlow<TopicDetailState> = _state.asStateFlow()

    private val _effects = Channel<TopicDetailEffect>(Channel.BUFFERED)
    val effects: Flow<TopicDetailEffect> = _effects.receiveAsFlow()

    private var closed = false

    init {
        viewModelScope.launch {
            repository.observeAll()
                .catch { _effects.send(TopicDetailEffect.SaveFailed) }
                .collect { list ->
                    val topic = list.firstOrNull { it.id == topicId }
                    _state.value = TopicDetailState(topic)
                    if (topic == null && !closed) {
                        closed = true
                        _effects.send(TopicDetailEffect.Close)
                    }
                }
        }
    }

    fun onIntent(intent: TopicDetailIntent) {
        when (intent) {
            TopicDetailIntent.ToggleFavorite -> toggleFavorite()
        }
    }

    private fun toggleFavorite() {
        val topic = _state.value.topic ?: return
        viewModelScope.launch {
            try {
                repository.setFavorite(topic.id, !topic.isFavorite)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _effects.send(TopicDetailEffect.SaveFailed)
            }
        }
    }
}
