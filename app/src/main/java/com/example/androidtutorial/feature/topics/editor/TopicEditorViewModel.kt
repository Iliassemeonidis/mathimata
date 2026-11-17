package com.example.androidtutorial.feature.topics.editor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androidtutorial.core.data.repository.TopicRepository
import com.example.androidtutorial.core.model.NewTopic
import com.example.androidtutorial.core.model.TopicCategory
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
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel(assistedFactory = TopicEditorViewModel.Factory::class)
class TopicEditorViewModel @AssistedInject constructor(
    private val repository: TopicRepository,
    @Assisted initialCategory: TopicCategory? = null,
) : ViewModel() {
    @AssistedFactory
    interface Factory {
        fun create(initialCategory: TopicCategory?): TopicEditorViewModel
    }

    private val _state = MutableStateFlow(TopicEditorState(category = initialCategory))
    val state: StateFlow<TopicEditorState> = _state.asStateFlow()

    private val _effects = Channel<TopicEditorEffect>(Channel.BUFFERED)
    val effects: Flow<TopicEditorEffect> = _effects.receiveAsFlow()
    private var saveInFlight = false

    fun onIntent(intent: TopicEditorIntent) {
        when (intent) {
            is TopicEditorIntent.Title -> _state.update { it.copy(title = intent.value) }
            is TopicEditorIntent.Summary -> _state.update { it.copy(summary = intent.value) }
            is TopicEditorIntent.Body -> _state.update { it.copy(body = intent.value) }
            is TopicEditorIntent.Code -> _state.update { it.copy(codeSample = intent.value) }
            is TopicEditorIntent.Category -> _state.update { it.copy(category = intent.value) }
            TopicEditorIntent.Save -> save()
        }
    }

    private fun save() {
        val current = _state.value
        val title = current.title.trim()
        val summary = current.summary.trim()
        val body = current.body.trim()
        val code = current.codeSample.trim()
        val category = current.category
        _state.update {
            it.copy(
                titleError = title.isEmpty(),
                summaryError = summary.isEmpty(),
                bodyError = body.isEmpty(),
                codeError = code.isEmpty(),
                categoryError = category == null,
            )
        }
        if (title.isEmpty() || summary.isEmpty() || body.isEmpty() || code.isEmpty()) return
        if (category == null) return
        if (saveInFlight) return
        saveInFlight = true
        viewModelScope.launch {
            try {
                repository.add(NewTopic(title, summary, body, code, category))
                _effects.send(TopicEditorEffect.Saved)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                saveInFlight = false
                _effects.send(TopicEditorEffect.SaveFailed)
            }
        }
    }
}
