package com.example.androidtutorial.feature.topics.detail

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidtutorial.core.design.BookColors
import com.example.androidtutorial.feature.topics.list.FavoriteHeart
import com.example.androidtutorial.feature.topics.list.TopicBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicDetailScreen(
    viewModel: TopicDetailViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val currentOnClose by rememberUpdatedState(onClose)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                TopicDetailEffect.Close -> currentOnClose()
                TopicDetailEffect.SaveFailed -> snackbarHost.showSnackbar(
                    message = "Не получилось сохранить",
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    val turn = remember { Animatable(-8f) }
    LaunchedEffect(Unit) { turn.animateTo(0f, tween(320)) }

    Scaffold(
        modifier = modifier,
        containerColor = BookColors.Paper,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BookColors.Paper),
            )
        },
    ) { padding ->
        val topic = state.topic
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .graphicsLayer {
                    rotationY = turn.value
                    cameraDistance = 12f * density
                },
        ) {
            if (topic != null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Row(verticalAlignment = Alignment.Top) {
                        Box(Modifier.padding(top = 8.dp)) { TopicBadge(topic) }
                        Spacer(Modifier.width(12.dp))
                        Text(
                            text = topic.title,
                            style = MaterialTheme.typography.headlineMedium,
                            color = BookColors.Ink,
                            modifier = Modifier.weight(1f),
                        )
                        FavoriteHeart(
                            isFavorite = topic.isFavorite,
                            onClick = { viewModel.onIntent(TopicDetailIntent.ToggleFavorite) },
                        )
                    }
                    Text(
                        text = topic.body,
                        style = MaterialTheme.typography.bodyLarge,
                        color = BookColors.Ink,
                    )
                    Box(
                        Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(BookColors.CodePlate),
                    ) {
                        Text(
                            text = topic.codeSample,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = FontFamily.Monospace,
                            color = BookColors.Ink,
                            softWrap = false,
                            modifier = Modifier
                                .horizontalScroll(rememberScrollState())
                                .padding(16.dp),
                        )
                    }
                }
            }
        }
    }
}
