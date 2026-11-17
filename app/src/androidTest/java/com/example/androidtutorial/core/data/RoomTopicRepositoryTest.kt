package com.example.androidtutorial.core.data

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.example.androidtutorial.core.data.local.MathimataDatabase
import com.example.androidtutorial.core.data.local.SeedCallback
import com.example.androidtutorial.core.data.repository.Clock
import com.example.androidtutorial.core.data.repository.RoomTopicRepository
import com.example.androidtutorial.core.model.NewTopic
import com.example.androidtutorial.core.model.TopicCategory
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RoomTopicRepositoryTest {
    private lateinit var database: MathimataDatabase
    private lateinit var repository: RoomTopicRepository

    @Before
    fun setUp() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        database = Room.inMemoryDatabaseBuilder(context, MathimataDatabase::class.java)
            .addCallback(SeedCallback)
            .build()
        repository = RoomTopicRepository(database.topicDao(), Clock { 2_000L })
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun seedInsertsEachCatalogTopicOnce() = runTest {
        val first = repository.observeAll().first()
        val second = repository.observeAll().first()
        assertEquals(20, first.size)
        assertEquals(20, second.size)
        assertEquals(10, first.count { it.category == TopicCategory.KOTLIN })
        assertEquals(10, first.count { it.category == TopicCategory.ANDROID })
        assertTrue(first.all { it.isBuiltIn })
        assertTrue(first.any { it.title == "Стирание типов и reified" })
        assertTrue(first.any { it.title == "Ментальная модель Compose" })
    }

    @Test
    fun addPersistsAUserTopic() = runTest {
        repository.add(NewTopic("Своя", "Кратко", "Разбор", "code", TopicCategory.KOTLIN))
        val added = repository.observeAll().first().single { it.title == "Своя" }
        assertFalse(added.isBuiltIn)
        assertEquals(0, added.sortOrder)
        assertEquals(2_000L, added.createdAt)
        assertFalse(added.isFavorite)
    }

    @Test
    fun deleteAndRestoreKeepTheUserTopicId() = runTest {
        repository.add(NewTopic("Своя", "Кратко", "Разбор", "code", TopicCategory.ANDROID))
        val added = repository.observeAll().first().single { it.title == "Своя" }
        assertTrue(repository.deleteUserTopic(added.id))
        assertTrue(repository.observeAll().first().none { it.id == added.id })
        repository.restore(added)
        assertTrue(repository.observeAll().first().any { it.id == added.id })
    }

    @Test
    fun builtInTopicCannotBeDeleted() = runTest {
        val builtIn = repository.observeAll().first().first { it.isBuiltIn }
        assertFalse(repository.deleteUserTopic(builtIn.id))
        assertTrue(repository.observeAll().first().any { it.id == builtIn.id })
    }

    @Test
    fun favoriteFlagRoundTrips() = runTest {
        val builtIn = repository.observeAll().first().first { it.isBuiltIn }
        repository.setFavorite(builtIn.id, true)
        assertTrue(repository.observeAll().first().first { it.id == builtIn.id }.isFavorite)
        repository.setFavorite(builtIn.id, false)
        assertFalse(repository.observeAll().first().first { it.id == builtIn.id }.isFavorite)
    }
}
