package com.example.androidtutorial.feature.study

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.androidtutorial.core.design.BookColors

@Composable
fun StudyScreen(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text(
            text = "Вопросы по темам появятся здесь.",
            style = MaterialTheme.typography.bodyLarge,
            color = BookColors.InkSecondary,
        )
    }
}
