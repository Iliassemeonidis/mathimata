# Mathimata Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the local Kotlin and Android study book: seeded Room topics, user topics, favorites, and a beige Material 3 Compose UI.

**Architecture:** One `app` module whose packages match future Gradle modules. Room is the only topic store. `TopicRepository` hides it. Each interactive screen has its own MVI contract and Hilt ViewModel. The shell owns the snackbar so Undo survives a tab change.

**Tech Stack:** Kotlin 2.2.10, AGP 9.3.3, Compose BOM 2026.02.01, Material 3, Room 2.8.5, Hilt 2.60.1, KSP 2.2.10-2.0.2, Navigation Compose 2.10.2, lifecycle 2.11.0, coroutines 1.10.2.

## Global Constraints

- Package and application id stay `com.example.androidtutorial`. Launcher label stays `Android Tutorial`.
- minSdk 24, targetSdk 37, compileSdk 37. Do not change AGP or the Kotlin version.
- UI copy is Russian. Code samples are Kotlin.
- Room database file is `mathimata.db`, version 1, `exportSchema = false`.
- Built-in topics cannot be deleted or edited. User topics cannot be edited.
- A user topic requires title, summary, body, code sample, and category `KOTLIN` or `ANDROID` after trim.
- User-topic icon is a person avatar tinted by category. Built-in icon is a K or A letter. The icon is not stored.
- Favorite heart is `#3C9A62` when selected and a `#3C322A` stroke at 28% alpha when not. The heart does not open the topic.
- Page colors: page `#F6F0E6`, card `#FFFBF6`, ink `#3C322A`, muted `#8A7768`, code well `#EFE4D6`.
- Kotlin wash `rgba(127, 82, 255, 0.16)`, title `#5B3CC4`, badge `#EFE7FB` / `#6D4AD4`.
- Android wash `rgba(61, 155, 110, 0.18)`, title `#246B45`, badge `#E3F3EA` / `#2F7D52`.
- Topic transition is 320 ms, slide from the right plus rotationY from -8° to 0°. Tabs fade in 150 ms.
- Home shuffles once per visit and keeps that id order while the tab stays selected, including when returning from a topic. Kotlin and Android show built-ins by `sortOrder`, then user topics by `createdAt`. Favorites do the same in two blocks: Kotlin, then Android.
- Undo uses `SnackbarDuration.Short`. Dismissing the process while it is visible keeps the topic deleted.
- Do not add a remote API, quiz logic, profile history, syntax highlighting, real auth, or Gradle module splits.
- The folder is not a git repository yet. Task 1 initializes it. If `git commit` fails because `user.name` is unset, stop and ask. Do not change git config.
- `.superpowers/` is already in `.gitignore`. Do not commit it.

## File map

Create these files. Later tasks depend on the signatures in earlier tasks.

```text
app/src/main/java/com/example/androidtutorial/MathimataApp.kt
app/src/main/java/com/example/androidtutorial/navigation/AppDestination.kt
app/src/main/java/com/example/androidtutorial/navigation/AppNavHost.kt
app/src/main/java/com/example/androidtutorial/core/design/BookColors.kt
app/src/main/java/com/example/androidtutorial/core/design/Type.kt
app/src/main/java/com/example/androidtutorial/core/design/Theme.kt
app/src/main/java/com/example/androidtutorial/core/design/BookTransition.kt
app/src/main/java/com/example/androidtutorial/core/model/TopicCategory.kt
app/src/main/java/com/example/androidtutorial/core/model/Topic.kt
app/src/main/java/com/example/androidtutorial/core/model/NewTopic.kt
app/src/main/java/com/example/androidtutorial/core/model/TopicFilter.kt
app/src/main/java/com/example/androidtutorial/core/data/local/TopicEntity.kt
app/src/main/java/com/example/androidtutorial/core/data/local/TopicDao.kt
app/src/main/java/com/example/androidtutorial/core/data/local/MathimataDatabase.kt
app/src/main/java/com/example/androidtutorial/core/data/local/TopicSeed.kt
app/src/main/java/com/example/androidtutorial/core/data/di/DataModule.kt
app/src/main/java/com/example/androidtutorial/core/data/repository/TopicRepository.kt
app/src/main/java/com/example/androidtutorial/core/data/repository/RoomTopicRepository.kt
app/src/main/java/com/example/androidtutorial/feature/topics/list/TopicOrdering.kt
app/src/main/java/com/example/androidtutorial/feature/topics/list/TopicListContract.kt
app/src/main/java/com/example/androidtutorial/feature/topics/list/TopicListViewModel.kt
app/src/main/java/com/example/androidtutorial/feature/topics/list/TopicCard.kt
app/src/main/java/com/example/androidtutorial/feature/topics/list/TopicListScreen.kt
app/src/main/java/com/example/androidtutorial/feature/topics/detail/TopicDetailContract.kt
app/src/main/java/com/example/androidtutorial/feature/topics/detail/TopicDetailViewModel.kt
app/src/main/java/com/example/androidtutorial/feature/topics/detail/TopicDetailScreen.kt
app/src/main/java/com/example/androidtutorial/feature/topics/editor/TopicEditorContract.kt
app/src/main/java/com/example/androidtutorial/feature/topics/editor/TopicEditorViewModel.kt
app/src/main/java/com/example/androidtutorial/feature/topics/editor/TopicEditorScreen.kt
app/src/main/java/com/example/androidtutorial/feature/auth/AuthContract.kt
app/src/main/java/com/example/androidtutorial/feature/auth/AuthViewModel.kt
app/src/main/java/com/example/androidtutorial/feature/auth/AuthScreen.kt
app/src/main/java/com/example/androidtutorial/feature/study/StudyScreen.kt
app/src/main/java/com/example/androidtutorial/feature/profile/ProfileScreen.kt
app/src/main/java/com/example/androidtutorial/feature/shell/MainShell.kt
```

Delete `app/src/main/java/com/example/androidtutorial/ui/theme/` when `core/design` replaces it. Modify `MainActivity.kt`, `AndroidManifest.xml`, `app/build.gradle.kts`, `build.gradle.kts`, `gradle/libs.versions.toml`, and `app/src/main/res/values/strings.xml`.

---

### Task 1: Domain model and list order

**Files:**
- Create: `app/src/main/java/com/example/androidtutorial/core/model/TopicCategory.kt`
- Create: `app/src/main/java/com/example/androidtutorial/core/model/Topic.kt`
- Create: `app/src/main/java/com/example/androidtutorial/core/model/NewTopic.kt`
- Create: `app/src/main/java/com/example/androidtutorial/core/model/TopicFilter.kt`
- Create: `app/src/main/java/com/example/androidtutorial/feature/topics/list/TopicOrdering.kt`
- Test: `app/src/test/java/com/example/androidtutorial/feature/topics/list/TopicOrderingTest.kt`

**Interfaces:**
- Consumes: nothing
- Produces: `TopicCategory`, `Topic`, `NewTopic`, `TopicFilter`, `TopicOrdering.arrange(topics, filter, shuffle)`

- [ ] **Step 1: Initialize git and write the failing test**

```bash
git init
git add .gitignore docs app build.gradle.kts settings.gradle.kts gradle gradle.properties gradlew gradlew.bat
git commit -m "chore: import the Compose template and study-book spec"
```

Create `TopicOrderingTest.kt`:

```kotlin
package com.example.androidtutorial.feature.topics.list

import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.core.model.TopicFilter
import org.junit.Assert.assertEquals
import org.junit.Test

class TopicOrderingTest {
    private fun topic(
        id: Long,
        category: TopicCategory,
        builtIn: Boolean,
        sortOrder: Int,
        createdAt: Long,
        favorite: Boolean = false,
    ) = Topic(
        id = id,
        title = "t$id",
        summary = "s",
        body = "b",
        codeSample = "c",
        category = category,
        isFavorite = favorite,
        isBuiltIn = builtIn,
        sortOrder = sortOrder,
        createdAt = createdAt,
    )

    @Test
    fun sectionPutsBuiltInsBeforeUserTopics() {
        val topics = listOf(
            topic(1, TopicCategory.KOTLIN, builtIn = false, sortOrder = 0, createdAt = 20),
            topic(2, TopicCategory.KOTLIN, builtIn = true, sortOrder = 2, createdAt = 0),
            topic(3, TopicCategory.KOTLIN, builtIn = true, sortOrder = 1, createdAt = 0),
            topic(4, TopicCategory.ANDROID, builtIn = true, sortOrder = 1, createdAt = 0),
        )
        val ids = TopicOrdering.arrange(topics, TopicFilter.KOTLIN) { error("no shuffle") }
            .map { it.id }
        assertEquals(listOf(3L, 2L, 1L), ids)
    }

    @Test
    fun favoritesGroupKotlinBeforeAndroid() {
        val topics = listOf(
            topic(1, TopicCategory.ANDROID, true, 1, 0, favorite = true),
            topic(2, TopicCategory.KOTLIN, false, 0, 5, favorite = true),
            topic(3, TopicCategory.KOTLIN, true, 1, 0, favorite = true),
            topic(4, TopicCategory.KOTLIN, true, 2, 0, favorite = false),
        )
        val ids = TopicOrdering.arrange(topics, TopicFilter.FAVORITES) { error("no shuffle") }
            .map { it.id }
        assertEquals(listOf(3L, 2L, 1L), ids)
    }

    @Test
    fun homeUsesTheProvidedShuffle() {
        val topics = listOf(
            topic(1, TopicCategory.KOTLIN, true, 1, 0),
            topic(2, TopicCategory.ANDROID, true, 1, 0),
        )
        val ids = TopicOrdering.arrange(topics, TopicFilter.ALL) { it.reversed() }.map { it.id }
        assertEquals(listOf(2L, 1L), ids)
    }
}
```

- [ ] **Step 2: Run the test and confirm it fails**

Run: `./gradlew :app:testDebugUnitTest --tests com.example.androidtutorial.feature.topics.list.TopicOrderingTest`

Expected: FAIL, `TopicOrdering` is unresolved.

- [ ] **Step 3: Write the model and ordering**

`TopicCategory.kt`:

```kotlin
package com.example.androidtutorial.core.model

enum class TopicCategory { KOTLIN, ANDROID }
```

`Topic.kt`:

```kotlin
package com.example.androidtutorial.core.model

data class Topic(
    val id: Long,
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
```

`NewTopic.kt`:

```kotlin
package com.example.androidtutorial.core.model

data class NewTopic(
    val title: String,
    val summary: String,
    val body: String,
    val codeSample: String,
    val category: TopicCategory,
)
```

`TopicFilter.kt`:

```kotlin
package com.example.androidtutorial.core.model

enum class TopicFilter { ALL, KOTLIN, ANDROID, FAVORITES }
```

`TopicOrdering.kt`:

```kotlin
package com.example.androidtutorial.feature.topics.list

import com.example.androidtutorial.core.model.Topic
import com.example.androidtutorial.core.model.TopicCategory
import com.example.androidtutorial.core.model.TopicFilter

object TopicOrdering {
    fun arrange(
        topics: List<Topic>,
        filter: TopicFilter,
        shuffle: (List<Topic>) -> List<Topic>,
    ): List<Topic> {
        val filtered = when (filter) {
            TopicFilter.ALL -> topics
            TopicFilter.KOTLIN -> topics.filter { it.category == TopicCategory.KOTLIN }
            TopicFilter.ANDROID -> topics.filter { it.category == TopicCategory.ANDROID }
            TopicFilter.FAVORITES -> topics.filter { it.isFavorite }
        }
        if (filter == TopicFilter.ALL) return shuffle(filtered)
        return when (filter) {
            TopicFilter.KOTLIN -> block(TopicCategory.KOTLIN, filtered)
            TopicFilter.ANDROID -> block(TopicCategory.ANDROID, filtered)
            TopicFilter.FAVORITES -> block(TopicCategory.KOTLIN, filtered) + block(TopicCategory.ANDROID, filtered)
            TopicFilter.ALL -> error("home is shuffled above")
        }
    }

    private fun block(category: TopicCategory, topics: List<Topic>): List<Topic> {
        val matching = topics.filter { it.category == category }
        val builtIn = matching.filter { it.isBuiltIn }.sortedBy { it.sortOrder }
        val own = matching.filter { !it.isBuiltIn }.sortedBy { it.createdAt }
        return builtIn + own
    }
}
```

- [ ] **Step 4: Run the test and confirm it passes**

Run: `./gradlew :app:testDebugUnitTest --tests com.example.androidtutorial.feature.topics.list.TopicOrderingTest`

Expected: PASS, 3 tests.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/example/androidtutorial/core/model app/src/main/java/com/example/androidtutorial/feature/topics/list/TopicOrdering.kt app/src/test/java/com/example/androidtutorial/feature/topics/list/TopicOrderingTest.kt
git commit -m "feat: order catalog topics and favorites"
```

---

### Task 2: Room, seed, and repository

**Files:**
- Modify: `gradle/libs.versions.toml`
- Modify: `build.gradle.kts`
- Modify: `app/build.gradle.kts`
- Create: `app/src/main/java/com/example/androidtutorial/core/data/local/TopicEntity.kt`
- Create: `app/src/main/java/com/example/androidtutorial/core/data/local/TopicDao.kt`
- Create: `app/src/main/java/com/example/androidtutorial/core/data/local/TopicSeed.kt`
- Create: `app/src/main/java/com/example/androidtutorial/core/data/local/MathimataDatabase.kt`
- Create: `app/src/main/java/com/example/androidtutorial/core/data/repository/Clock.kt`
- Create: `app/src/main/java/com/example/androidtutorial/core/data/repository/TopicRepository.kt`
- Create: `app/src/main/java/com/example/androidtutorial/core/data/repository/RoomTopicRepository.kt`
- Test: `app/src/androidTest/java/com/example/androidtutorial/core/data/RoomTopicRepositoryTest.kt`

**Interfaces:**
- Consumes: `Topic`, `NewTopic`, `TopicCategory`
- Produces:
  - `fun interface Clock { fun now(): Long }`
  - `interface TopicRepository { fun observeAll(): Flow<List<Topic>>; suspend fun add(draft: NewTopic); suspend fun deleteUserTopic(id: Long): Boolean; suspend fun restore(topic: Topic); suspend fun setFavorite(id: Long, favorite: Boolean) }`
  - `MathimataDatabase`, `SeedCallback`, `TopicDao`

- [ ] **Step 1: Add Room and the failing instrumented test**

Add to `gradle/libs.versions.toml` versions: `ksp = "2.2.10-2.0.2"`, `room = "2.8.5"`, `coroutines = "1.10.2"`.

Add libraries:

```toml
androidx-room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
androidx-room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
androidx-room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
kotlinx-coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
kotlinx-coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
```

Add plugin: `ksp = { id = "com.google.devtools.ksp", version.ref = "ksp" }`.

In the root `build.gradle.kts` plugins block add `alias(libs.plugins.ksp) apply false`.

In `app/build.gradle.kts` apply `alias(libs.plugins.ksp)` and add:

```kotlin
implementation(libs.androidx.room.runtime)
implementation(libs.androidx.room.ktx)
implementation(libs.kotlinx.coroutines.android)
ksp(libs.androidx.room.compiler)
```

Create `RoomTopicRepositoryTest.kt`:

```kotlin
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
```

- [ ] **Step 2: Run the test and confirm it fails**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.androidtutorial.core.data.RoomTopicRepositoryTest`

Expected: FAIL to compile because `MathimataDatabase` does not exist. A device or emulator must be connected.

- [ ] **Step 3: Implement Room and the repository**

`TopicEntity` table `topics`, primary key `id` autogenerate, columns `title`, `summary`, `body`, `codeSample`, `category`, `isFavorite`, `isBuiltIn`, `sortOrder`, `createdAt`.

`TopicCategoryConverter` stores `TopicCategory.name`.

`TopicDao`:

```kotlin
@Dao
interface TopicDao {
    @Query("SELECT * FROM topics")
    fun observeAll(): Flow<List<TopicEntity>>

    @Insert
    suspend fun insert(entity: TopicEntity): Long

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun restore(entity: TopicEntity)

    @Query("DELETE FROM topics WHERE id = :id AND isBuiltIn = 0")
    suspend fun deleteUser(id: Long): Int

    @Query("UPDATE topics SET isFavorite = :favorite WHERE id = :id")
    suspend fun updateFavorite(id: Long, favorite: Boolean)
}
```

`TopicSeed.insert` writes one bound `INSERT` per entry in `entries`. `isBuiltIn` is 1, `isFavorite` is 0, and `createdAt` is 0. `summary`, `body`, and `codeSample` are copied character for character from the matching numbered item in `docs/superpowers/specs/2026-11-17-mathimata-design.md` under `Каталог`. The titles and order below are the contract the test checks; do not rename them:

Kotlin: `Стирание типов и reified`, `Дисперсия in и out`, `Structured concurrency`, `Кооперативная отмена`, `Dispatchers и withContext`, `Общее изменяемое состояние`, `Flow, StateFlow, SharedFlow`, `Исключения и supervisorScope`, `Continuation`, `Делегирование`.

Android: `Ментальная модель Compose`, `Рекомпозиция и стабильность`, `Три фазы кадра`, `Жизненный цикл composable`, `Побочные эффекты`, `Где хранить состояние`, `Смерть процесса`, `Смена конфигурации`, `Главный поток`, `Сбор потока в UI`.

`SeedCallback.onCreate` calls `TopicSeed.insert(db)` on the same `SupportSQLiteDatabase` before it returns. Do not insert again from `onOpen`.

```kotlin
@Database(entities = [TopicEntity::class], version = 1, exportSchema = false)
@TypeConverters(TopicCategoryConverter::class)
abstract class MathimataDatabase : RoomDatabase() {
    abstract fun topicDao(): TopicDao
}
```

`RoomTopicRepository`:

- `observeAll` maps entities to `Topic`.
- `add` inserts `id = 0`, `isFavorite = false`, `isBuiltIn = false`, `sortOrder = 0`, `createdAt = clock.now()`.
- `deleteUserTopic` returns `deleteUser(id) > 0`.
- `restore` inserts the entity with the original id and stored fields.
- `setFavorite` calls `updateFavorite`.

- [ ] **Step 4: Run the test and confirm it passes**

Run the same `connectedDebugAndroidTest` command.

Expected: PASS. Also run `./gradlew :app:testDebugUnitTest --tests com.example.androidtutorial.feature.topics.list.TopicOrderingTest` and expect PASS.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts app/src/main/java/com/example/androidtutorial/core/data app/src/androidTest/java/com/example/androidtutorial/core/data
git commit -m "feat: store topics in Room with a built-in catalog"
```

---

### Task 3: List, editor, detail, and auth ViewModels

**Files:**
- Modify: `gradle/libs.versions.toml` and `app/build.gradle.kts` to add `lifecycle-viewmodel-compose` 2.11.0 and `kotlinx-coroutines-test`
- Create: the four contract files, four ViewModel files
- Create: `app/src/test/java/com/example/androidtutorial/MainDispatcherRule.kt`
- Create: `app/src/test/java/com/example/androidtutorial/feature/topics/FakeTopicRepository.kt`
- Test: `TopicListViewModelTest.kt`, `TopicEditorViewModelTest.kt`, `TopicDetailViewModelTest.kt`, `AuthViewModelTest.kt` under `app/src/test/java/com/example/androidtutorial/feature/`

**Interfaces:**
- Consumes: `TopicRepository`, `TopicOrdering`, `TopicFilter`, `NewTopic`, `Topic`
- Produces the contracts below. Screens in Task 4 collect `state` and `effects`.

```kotlin
fun interface RandomSource { fun next(): Random }

data class TopicListState(
    val topics: List<Topic> = emptyList(),
    val errorVisible: Boolean = false,
)

sealed interface TopicListIntent {
    data class Visit(val visit: Int) : TopicListIntent
    data class ToggleFavorite(val id: Long) : TopicListIntent
    data class Delete(val topic: Topic) : TopicListIntent
    data object UndoDelete : TopicListIntent
    data object DismissError : TopicListIntent
}

sealed interface TopicListEffect {
    data class ShowUndo(val topic: Topic) : TopicListEffect
    data object ShowError : TopicListEffect
}

data class TopicEditorState(
    val title: String = "",
    val summary: String = "",
    val body: String = "",
    val codeSample: String = "",
    val category: TopicCategory? = null,
    val titleError: Boolean = false,
    val summaryError: Boolean = false,
    val bodyError: Boolean = false,
    val codeError: Boolean = false,
    val categoryError: Boolean = false,
)

sealed interface TopicEditorIntent {
    data class Title(val value: String) : TopicEditorIntent
    data class Summary(val value: String) : TopicEditorIntent
    data class Body(val value: String) : TopicEditorIntent
    data class Code(val value: String) : TopicEditorIntent
    data class Category(val value: TopicCategory) : TopicEditorIntent
    data object Save : TopicEditorIntent
}

sealed interface TopicEditorEffect {
    data object Saved : TopicEditorEffect
    data object SaveFailed : TopicEditorEffect
}

data class TopicDetailState(val topic: Topic? = null)

sealed interface TopicDetailIntent {
    data object ToggleFavorite : TopicDetailIntent
}

sealed interface TopicDetailEffect {
    data object Close : TopicDetailEffect
    data object SaveFailed : TopicDetailEffect
}

data class AuthState(val login: String = "", val password: String = "")

sealed interface AuthIntent {
    data class Login(val value: String) : AuthIntent
    data class Password(val value: String) : AuthIntent
    data object Submit : AuthIntent
}

sealed interface AuthEffect {
    data object Entered : AuthEffect
}
```

- [ ] **Step 1: Write failing ViewModel tests**

`MainDispatcherRule` sets `Dispatchers.Main` to `UnconfinedTestDispatcher` in `starting` and calls `Dispatchers.resetMain()` in `finished`.

`FakeTopicRepository` keeps a `MutableStateFlow<List<Topic>>`. `add` appends a `Topic` with the next id, `isBuiltIn = false`, `sortOrder = 0`, `createdAt = 50`, `isFavorite = false`. `deleteUserTopic` removes a non-built-in id and returns whether it removed one. `restore` inserts that topic again. `setFavorite` updates the matching row. A `fail` flag makes every suspend call throw `IllegalStateException("db")`.

`TopicListViewModelTest` uses `runTest` and the rule. Seed four topics, ids 1 and 2 Kotlin built-in sort 1 and 2, id 3 Android built-in, id 4 a user Kotlin topic. `RandomSource` returns `Random(seeds.removeFirst())` from a deque `7`, then `8`.

- `Visit(1)` on `TopicFilter.ALL` equals `topics.shuffled(Random(7))` ids.
- Replacing the flow with the same topics reversed keeps that id order.
- `Visit(2)` equals `topics.shuffled(Random(8))` ids.
- `TopicFilter.KOTLIN` yields built-ins 1, 2 then user 4, and does not call the shuffler. Assert this by using a `RandomSource` that throws if `next()` is called.
- `Delete` of topic 4 removes it and the collected effect is `ShowUndo` of that topic. `UndoDelete` calls `restore` and topic 4 is back.
- `Delete` of built-in topic 1 does not change the list and emits no `ShowUndo`.
- With `fail = true`, `ToggleFavorite` emits `ShowError` and leaves `isFavorite` false.

`TopicEditorViewModelTest`:

- Save with blank body and null category does not call `add`. `bodyError` and `categoryError` are true.
- Save of `"  Имя  "` with the other fields `"описание"`, `"разбор"`, `"код"` and `KOTLIN` calls `add` with trimmed title `Имя` and emits `Saved`.
- `fail = true` on a valid form emits `SaveFailed` and does not emit `Saved`.

`TopicDetailViewModelTest`:

- A known id exposes that topic.
- Removing it from the fake flow emits `Close`.
- `ToggleFavorite` flips the repository flag.
- `fail = true` emits `SaveFailed` and the flag stays unchanged.

`AuthViewModelTest`: `Submit` with empty fields emits `Entered`. Login and password intents update `AuthState` and still emit `Entered` on submit.

- [ ] **Step 2: Run the tests and confirm they fail**

Run: `./gradlew :app:testDebugUnitTest --tests com.example.androidtutorial.feature.topics.list.TopicListViewModelTest --tests com.example.androidtutorial.feature.topics.editor.TopicEditorViewModelTest --tests com.example.androidtutorial.feature.topics.detail.TopicDetailViewModelTest --tests com.example.androidtutorial.feature.auth.AuthViewModelTest`

Expected: FAIL, ViewModel classes unresolved.

- [ ] **Step 3: Implement the ViewModels**

Each ViewModel exposes `StateFlow` state and `Flow` effects from a `Channel(BUFFERED)`. Collect repository flows in `init` with `viewModelScope`. Catch repository exceptions, leave the previous successful state in place, and emit the failure effect.

`TopicListViewModel` constructor arguments are `TopicRepository`, `RandomSource`, and `TopicFilter`. Keep the latest raw list, the current visit, the home id order, and the last `Random`. `Visit` with a new number clears the id order and, only for `TopicFilter.ALL`, takes `randomSource.next()`. Until the first `Visit` on `ALL`, publish an empty list. After that, home order is the stored ids, dropping missing ids and appending ids that were not in the stored order. Other filters call `TopicOrdering.arrange(raw, filter) { it }` and never request a random. `Delete` ignores `isBuiltIn`. On a true result, emit `ShowUndo`. `UndoDelete` restores the topic from the last `ShowUndo` only. `DismissError` sets `errorVisible` false. `ToggleFavorite` writes the opposite flag.

`TopicEditorViewModel` optionally starts with `initialCategory: TopicCategory?`. `Save` trims all texts. Any blank text or a null category sets the matching error flags and does not call `add`. A valid draft clears errors, calls `add`, and emits `Saved`.

`TopicDetailViewModel` takes `topicId: Long`. It maps `observeAll()` to the matching topic. The first null after a collected list emits `Close` once. `ToggleFavorite` uses the current topic.

`AuthViewModel` stores the two strings and emits `Entered` on every `Submit`.

- [ ] **Step 4: Run the tests and confirm they pass**

Run the same unit-test command. Expected: PASS. Then rerun `TopicOrderingTest`. Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml app/build.gradle.kts app/src/main/java/com/example/androidtutorial/feature app/src/test/java/com/example/androidtutorial
git commit -m "feat: add MVI view models for the study book"
```

---

### Task 4: Book UI, navigation, and screens

**Files:**
- Modify: `gradle/libs.versions.toml`, root `build.gradle.kts`, `app/build.gradle.kts`, `AndroidManifest.xml`, `MainActivity.kt`, `strings.xml`
- Create: `MathimataApp.kt`, `core/design/*`, `core/data/di/DataModule.kt`, `navigation/*`, every remaining screen file from the file map
- Delete: `app/src/main/java/com/example/androidtutorial/ui/theme/`
- Test: `app/src/androidTest/java/com/example/androidtutorial/feature/topics/list/TopicCardTest.kt`

**Interfaces:**
- Consumes: every contract and repository from Tasks 1–3
- Produces: `MathimataTheme`, `bookEnter()`, `bookExit()`, `bookPopEnter()`, `bookPopExit()`, `AppDestination` routes, `DataModule`

Routes in `AppDestination`:

```kotlin
object AppDestination {
    const val Auth = "auth"
    const val Shell = "shell"
    const val Home = "topics/all"
    const val Kotlin = "topics/kotlin"
    const val Android = "topics/android"
    const val Study = "study"
    const val Favorites = "topics/favorites"
    const val Detail = "topic/{topicId}"
    const val Editor = "editor?category={category}"
    const val Profile = "profile"
    fun detail(id: Long) = "topic/$id"
    fun editor(category: TopicCategory?) =
        if (category == null) "editor?category=" else "editor?category=${category.name}"
}
```

- [ ] **Step 1: Write the failing card test**

`TopicCardTest` uses `createComposeRule`. Render a built-in Kotlin topic and a user Android topic inside `MathimataTheme`.

- The built-in card has content description `Тема Kotlin` and no content description `Своя тема`.
- The user card has content description `Своя тема` and no letter badge.
- Clicking content description `В избранном` invokes the favorite callback and does not invoke the open callback.
- Clicking the title invokes the open callback.

- [ ] **Step 2: Run the test and confirm it fails**

Run: `./gradlew :app:connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.example.androidtutorial.feature.topics.list.TopicCardTest`

Expected: FAIL, `TopicCard` is unresolved.

- [ ] **Step 3: Implement the UI**

Add versions `hilt = "2.60.1"`, `hiltNavigation = "1.4.0"`, `navigation = "2.10.2"`. Add `hilt-android`, `hilt-compiler`, `hilt-navigation-compose`, `navigation-compose`, `lifecycle-viewmodel-compose`, `lifecycle-runtime-compose`, and `material-icons-extended` (Compose BOM, no version). Apply Hilt and KSP in `app/build.gradle.kts`. Apply both plugins with `apply false` in the root file. Add `implementation` and `ksp` lines.

`MathimataApp` is `@HiltAndroidApp`. The manifest `application` tag gets `android:name=".MathimataApp"`. `MainActivity` is `@AndroidEntryPoint`, keeps `enableEdgeToEdge()`, and sets `MathimataTheme { AppNavHost() }`. Remove `Greeting`.

`DataModule` is `@InstallIn(SingletonComponent::class)` and provides a `@Singleton` database named `mathimata.db` with `SeedCallback`, the dao, `Clock { System.currentTimeMillis() }`, `RandomSource { Random.Default }`, and `RoomTopicRepository`.

`BookColors` holds the global-constraint colors. `Type.kt` sets `Typography` headlines to `FontFamily.Serif` and body to `FontFamily.SansSerif`. `MathimataTheme` builds a Material 3 `lightColorScheme` with background and surface `#F6F0E6`, `onBackground` `#3C322A`, and primary `#6D4AD4`. Force the light scheme.

`BookTransition.kt`:

```kotlin
fun bookEnter(): EnterTransition =
    slideInHorizontally(tween(320)) { it } + fadeIn(tween(320)) +
        scaleIn(tween(320), initialScale = 0.98f)

fun bookExit(): ExitTransition =
    slideOutHorizontally(tween(320)) { -it / 4 } + fadeOut(tween(320))

fun bookPopEnter(): EnterTransition =
    slideInHorizontally(tween(320)) { -it / 4 } + fadeIn(tween(320))

fun bookPopExit(): ExitTransition =
    slideOutHorizontally(tween(320)) { it } + fadeOut(tween(320))
```

`TopicDetailScreen` adds the rotation the slide does not express:

```kotlin
val turn = remember { Animatable(-8f) }
LaunchedEffect(Unit) { turn.animateTo(0f, tween(320)) }
Modifier.graphicsLayer {
    rotationY = turn.value
    cameraDistance = 12f * density
}
```

`TopicCard` lays out a paper card `#FFFBF6` with 16 dp corners. Built-in badge is a 26 dp rounded square: `K` or `A` with the category colors and content description `Тема Kotlin` or `Тема Android`. User badge is a 26 dp circle, person icon, background `#EFE7FB` or `#E3F3EA`, content description `Своя тема`. Heart button uses filled `#3C9A62` or outlined `#3C322A` at alpha 0.28. Its content description is `В избранном` or `Не в избранном`. The heart `clickable` consumes the click. The card click uses the remaining area.

`AppNavHost` starts at `Auth`. Auth submit pops auth inclusive and opens `Shell`. `Shell` hosts `MainShell`. Detail, editor, and profile are siblings of the shell, so the bottom bar is not composed there. Detail uses the book transitions. Editor and profile use the same slide functions. Tab destinations inside `MainShell` use `fadeIn(tween(150))` and `fadeOut(tween(150))`.

`MainShell` uses `Scaffold` with a beige container, a top title for the current tab, a person action that navigates to `Profile`, and a `NavigationBar` with Главная, Kotlin, Android, Изучать, Избранное. Tab navigation uses `launchSingleTop`, `restoreState`, and `popUpTo(Home) { saveState = true }`. The shell remembers `SnackbarHostState` and a `visit` counter per tab route. Each time a list route becomes selected, increment that route's visit and pass it as `TopicListIntent.Visit`. Collect that destination's `ShowUndo` in `rememberCoroutineScope`: show snackbar text `Тема удалена` with action `Отменить` and `SnackbarDuration.Short`. `ActionPerformed` sends `UndoDelete` to that same ViewModel. `ShowError` shows `Не получилось сохранить`.

List screens for Home, Kotlin, Android, and Favorites share `TopicListScreen`. The floating button is present only for the first three. Home passes a null editor category. Kotlin and Android pass their category. Favorites with an empty list shows `В книге пока нет закладок`. `SwipeToDismissBox` enables both directions only when `isBuiltIn` is false. When `currentValue` leaves `Settled`, send `Delete`.

`StudyScreen` text: `Вопросы по темам появятся здесь.` `ProfileScreen` text: `Здесь будет видно, что ты уже разбирал.`

`AuthScreen` has labels `Логин`, `Пароль`, and button `Войти`.

`TopicEditorScreen` reads the category argument. Empty or unknown means null. It shows five fields and two category buttons. Error flags outline those fields. Save sends `TopicEditorIntent.Save`. `Saved` pops the editor. `SaveFailed` shows `Не получилось сохранить`. Back pops without saving.

`TopicDetailScreen` shows the same badge rules, title, heart, body, and code on `#EFE4D6` in `FontFamily.Monospace` with horizontal scroll. `Close` pops. `SaveFailed` shows the same error snackbar.

Kotlin and Android list headers use their title colors. Behind the header, a 90 dp vertical gradient uses that section's wash and then becomes `#F6F0E6`. Home has no wash. Cards stay `#FFFBF6` on every tab.

- [ ] **Step 4: Run the tests and confirm they pass**

Run the card instrumentation command. Expected: PASS.

Run: `./gradlew :app:testDebugUnitTest`

Expected: PASS for `TopicOrderingTest` and all four ViewModel tests.

Run: `./gradlew :app:connectedDebugAndroidTest`

Expected: PASS for `RoomTopicRepositoryTest` and `TopicCardTest`.

- [ ] **Step 5: Commit**

```bash
git add gradle/libs.versions.toml build.gradle.kts app/build.gradle.kts app/src/main app/src/androidTest
git commit -m "feat: open the study book in Compose"
```

---

## Spec coverage

- Auth without validation, shell tabs, detail, editor, study, and profile are Task 4.
- Room seed, user rows, favorite flag, and guarded delete are Task 2.
- Ordering, shuffle visits, undo, and form rules are Task 3.
- Quiet-paper colors, badges, avatar, heart, and page motion are Task 4.
- Repository instrumentation and ViewModel JVM tests are Tasks 2 and 3.
- Remote sync, quizzes, profile history, editing, syntax highlighting, real auth, and Gradle splits are absent on purpose.

## Self-review notes

- `TopicRepository` has no filter method. ViewModels filter through `TopicOrdering`.
- `deleteUserTopic` returns `Boolean` and does not throw for a built-in id.
- Home does not shuffle until `Visit`, so returning from detail with the same visit keeps ids.
- The shell, not the list destination, owns the snackbar coroutine.
