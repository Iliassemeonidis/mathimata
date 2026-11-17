package com.example.androidtutorial.feature.topics.list

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.androidtutorial.core.design.BookColors
import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory

@Composable
fun TopicCard(
    topic: Topic,
    onOpen: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(16.dp)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(BookColors.Card)
            .clickable(onClick = onOpen)
            .padding(start = 14.dp, top = 10.dp, bottom = 14.dp, end = 6.dp),
        verticalAlignment = Alignment.Top,
    ) {
        Box(Modifier.padding(top = 6.dp)) { TopicBadge(topic) }
        Spacer(Modifier.width(12.dp))
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(top = 6.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = topic.title,
                style = MaterialTheme.typography.headlineSmall.copy(fontSize = 18.sp, lineHeight = 24.sp),
                color = BookColors.Ink,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = topic.summary,
                style = MaterialTheme.typography.bodyMedium,
                color = BookColors.InkSecondary,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
            )
        }
        FavoriteHeart(isFavorite = topic.isFavorite, onClick = onToggleFavorite)
    }
}

@Composable
fun TopicBadge(topic: Topic, modifier: Modifier = Modifier) {
    val kotlin = topic.category == TopicCategory.KOTLIN
    val background = if (kotlin) BookColors.KotlinBadge else BookColors.AndroidBadge
    val content = if (kotlin) BookColors.KotlinBadgeContent else BookColors.AndroidBadgeContent
    if (topic.isBuiltIn) {
        val description = if (kotlin) "Тема Kotlin" else "Тема Android"
        Box(
            modifier = modifier
                .size(26.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(background)
                .clearAndSetSemantics { contentDescription = description },
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = if (kotlin) "K" else "A",
                color = content,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
            )
        }
    } else {
        Box(
            modifier = modifier
                .size(26.dp)
                .clip(CircleShape)
                .background(background)
                .clearAndSetSemantics { contentDescription = "Своя тема" },
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = content,
                modifier = Modifier.size(16.dp),
            )
        }
    }
}

@Composable
fun FavoriteHeart(
    isFavorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val description = if (isFavorite) "В избранном" else "Не в избранном"
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        if (isFavorite) {
            Icon(
                imageVector = Icons.Filled.Favorite,
                contentDescription = description,
                tint = BookColors.HeartOn,
                modifier = Modifier.size(22.dp),
            )
        } else {
            Icon(
                imageVector = Icons.Outlined.FavoriteBorder,
                contentDescription = description,
                tint = BookColors.Ink.copy(alpha = 0.28f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
