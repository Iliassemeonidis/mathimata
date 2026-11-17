package com.example.androidtutorial.feature.topics.editor

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.androidtutorial.core.design.BookColors
import com.example.androidtutorial.core.model.TopicCategory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopicEditorScreen(
    viewModel: TopicEditorViewModel,
    onClose: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val snackbarHost = remember { SnackbarHostState() }
    val currentOnClose by rememberUpdatedState(onClose)

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                TopicEditorEffect.Saved -> currentOnClose()
                TopicEditorEffect.SaveFailed -> snackbarHost.showSnackbar(
                    message = "Не получилось сохранить",
                    duration = SnackbarDuration.Short,
                )
            }
        }
    }

    Scaffold(
        modifier = modifier,
        containerColor = BookColors.Paper,
        snackbarHost = { SnackbarHost(snackbarHost) },
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Новая тема",
                        style = MaterialTheme.typography.headlineSmall,
                        color = BookColors.Ink,
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Назад")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BookColors.Paper),
            )
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            OutlinedTextField(
                value = state.title,
                onValueChange = { viewModel.onIntent(TopicEditorIntent.Title(it)) },
                label = { Text("Название") },
                isError = state.titleError,
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.summary,
                onValueChange = { viewModel.onIntent(TopicEditorIntent.Summary(it)) },
                label = { Text("Краткое описание") },
                isError = state.summaryError,
                minLines = 2,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.body,
                onValueChange = { viewModel.onIntent(TopicEditorIntent.Body(it)) },
                label = { Text("Разбор") },
                isError = state.bodyError,
                minLines = 4,
                modifier = Modifier.fillMaxWidth(),
            )
            OutlinedTextField(
                value = state.codeSample,
                onValueChange = { viewModel.onIntent(TopicEditorIntent.Code(it)) },
                label = { Text("Пример кода") },
                isError = state.codeError,
                minLines = 4,
                textStyle = TextStyle(fontFamily = FontFamily.Monospace),
                modifier = Modifier.fillMaxWidth(),
            )
            Text(
                text = "Раздел",
                style = MaterialTheme.typography.bodyMedium,
                color = if (state.categoryError) {
                    MaterialTheme.colorScheme.error
                } else {
                    BookColors.InkSecondary
                },
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                CategoryButton(
                    label = "Kotlin",
                    selected = state.category == TopicCategory.KOTLIN,
                    error = state.categoryError,
                    onClick = { viewModel.onIntent(TopicEditorIntent.Category(TopicCategory.KOTLIN)) },
                    modifier = Modifier.weight(1f),
                )
                CategoryButton(
                    label = "Android",
                    selected = state.category == TopicCategory.ANDROID,
                    error = state.categoryError,
                    onClick = { viewModel.onIntent(TopicEditorIntent.Category(TopicCategory.ANDROID)) },
                    modifier = Modifier.weight(1f),
                )
            }
            Button(
                onClick = { viewModel.onIntent(TopicEditorIntent.Save) },
                colors = ButtonDefaults.buttonColors(containerColor = BookColors.Primary),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text("Сохранить")
            }
        }
    }
}

@Composable
private fun CategoryButton(
    label: String,
    selected: Boolean,
    error: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (selected) {
        FilledTonalButton(onClick = onClick, modifier = modifier) { Text(label) }
    } else {
        val border = if (error) {
            BorderStroke(1.dp, MaterialTheme.colorScheme.error)
        } else {
            ButtonDefaults.outlinedButtonBorder(enabled = true)
        }
        OutlinedButton(onClick = onClick, border = border, modifier = modifier) { Text(label) }
    }
}
