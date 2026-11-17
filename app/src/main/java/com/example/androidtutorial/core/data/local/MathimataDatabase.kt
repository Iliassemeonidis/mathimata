package com.example.androidtutorial.core.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [TopicEntity::class], version = 1, exportSchema = false)
@TypeConverters(TopicCategoryConverter::class)
abstract class MathimataDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
}

object SeedCallback : RoomDatabase.Callback() {
    override fun onCreate(db: SupportSQLiteDatabase) {
        TopicSeed.insert(db)
    }
}
