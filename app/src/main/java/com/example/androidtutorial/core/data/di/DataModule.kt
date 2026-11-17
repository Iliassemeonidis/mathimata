package com.example.androidtutorial.core.data.di

import android.content.Context
import androidx.room.Room
import com.example.androidtutorial.core.data.local.MathimataDatabase
import com.example.androidtutorial.core.data.local.SeedCallback
import com.example.androidtutorial.core.data.local.TopicDao
import com.example.androidtutorial.core.data.repository.Clock
import com.example.androidtutorial.core.data.repository.RoomTopicRepository
import com.example.androidtutorial.core.data.repository.TopicRepository
import com.example.androidtutorial.feature.topics.list.RandomSource
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlin.random.Random

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): MathimataDatabase =
        Room.databaseBuilder(context, MathimataDatabase::class.java, "mathimata.db")
            .addCallback(SeedCallback)
            .build()

    @Provides
    fun provideTopicDao(database: MathimataDatabase): TopicDao = database.topicDao()

    @Provides
    @Singleton
    fun provideClock(): Clock = Clock { System.currentTimeMillis() }

    @Provides
    @Singleton
    fun provideRandomSource(): RandomSource = RandomSource { Random.Default }

    @Provides
    @Singleton
    fun provideTopicRepository(dao: TopicDao, clock: Clock): TopicRepository =
        RoomTopicRepository(dao, clock)
}
