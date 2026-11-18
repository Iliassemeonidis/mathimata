package com.example.androidtutorial.feature.topics.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxDefaults
import androidx.compose.material3.SwipeToDismissBoxState
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidtutorial.core.design.BookColors
import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory

@Composable
fun TopicListScreen(
    viewModel: TopicListViewModel,
    visit: Int,
    showAdd: Boolean,
    addCategory: TopicCategory?,
    emptyText: String?,
    onOpenTopic: (Long) -> Unit,
    onAdd: (TopicCategory?) -> Unit,
    onEffect: (TopicListViewModel, TopicListEffect) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var resetSignal by remember { mutableIntStateOf(0) }
    val currentOnEffect by rememberUpdatedState(onEffect)

    LaunchedEffect(viewModel, visit) {
        viewModel.onIntent(TopicListIntent.Visit(visit))
    }
    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            if (effect is TopicListEffect.ShowError) resetSignal++
            currentOnEffect(viewModel, effect)
        }
    }

    Box(modifier.fillMaxSize()) {
        if (state.topics.isEmpty() && emptyText != null) {
            Text(
                text = emptyText,
                style = MaterialTheme.typography.bodyLarge,
                color = BookColors.InkSecondary,
                modifier = Modifier.align(Alignment.Center),
            )
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(state.topics, key = { it.id }) { topic ->
                DismissibleTopicCard(
                    topic = topic,
                    resetSignal = resetSignal,
                    onOpen = { onOpenTopic(topic.id) },
                    onToggleFavorite = { viewModel.onIntent(TopicListIntent.ToggleFavorite(topic.id)) },
                    onDelete = { viewModel.onIntent(TopicListIntent.Delete(topic)) },
                )
            }
        }
        if (showAdd) {
            FloatingActionButton(
                onClick = { onAdd(addCategory) },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Добавить тему")
            }
        }
    }
}

@Composable
private fun DismissibleTopicCard(
    topic: Topic,
    resetSignal: Int,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    onDelete: () -> Unit,
) {
    // Not saveable: LazyColumn keeps saved item state by key, so a restored topic would come
    // back already dismissed and delete itself again.
    val positionalThreshold = SwipeToDismissBoxDefaults.positionalThreshold
    val state = remember {
        SwipeToDismissBoxState(SwipeToDismissBoxValue.Settled, positionalThreshold)
    }
    val currentOnDelete by rememberUpdatedState(onDelete)

    LaunchedEffect(state.currentValue) {
        if (state.currentValue != SwipeToDismissBoxValue.Settled) currentOnDelete()
    }
    LaunchedEffect(resetSignal) {
        if (state.currentValue != SwipeToDismissBoxValue.Settled) state.reset()
    }

    SwipeToDismissBox(
        state = state,
        enableDismissFromStartToEnd = !topic.isBuiltIn,
        enableDismissFromEndToStart = !topic.isBuiltIn,
        backgroundContent = {
            if (state.dismissDirection != SwipeToDismissBoxValue.Settled) {
                val alignment = if (state.dismissDirection == SwipeToDismissBoxValue.StartToEnd) {
                    Alignment.CenterStart
                } else {
                    Alignment.CenterEnd
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(16.dp))
                        .background(BookColors.DeleteBackground)
                        .padding(horizontal = 20.dp),
                    contentAlignment = alignment,
                ) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить", tint = BookColors.Ink)
                }
            }
        },
        modifier = Modifier.fillMaxWidth(),
    ) {
        TopicCard(topic = topic, onOpen = onOpen, onToggleFavorite = onToggleFavorite)
    }
}
