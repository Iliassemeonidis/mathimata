package com.example.androidtutorial.core.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import com.example.androidtutorial.core.model.TopicCategory

@Entity(tableName = "topics")
data class TopicEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val summary: String,
    val body: String,
    val codeSample: String,
    val category: TopicCategory,
    val isFavorite: Boolean,
    val isBuiltIn: Boolean,
    val sortOrder: Int,
    val createdAt: Long,
)

class TopicCategoryConverter {
    @TypeConverter
    fun fromCategory(category: TopicCategory): String = category.name

    @TypeConverter
    fun toCategory(name: String): TopicCategory = TopicCategory.valueOf(name)
}
