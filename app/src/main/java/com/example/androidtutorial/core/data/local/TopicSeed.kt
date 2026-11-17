package com.example.androidtutorial.core.data.local

import androidx.sqlite.db.SupportSQLiteDatabase

object TopicSeed {
    private class Entry(
        val title: String,
        val summary: String,
        val body: String,
        val codeSample: String,
        val category: String,
        val sortOrder: Int,
    )

    private val entries = listOf(
        Entry(
            title = "Стирание типов и reified",
            summary = "На JVM параметр типа стирается, а `reified` видит его в месте вызова.",
            body = "На JVM параметр типа стирается до верхней границы. В рантайме список строк и список чисел выглядят одинаково, поэтому проверка `is List<String>` не компилируется. Обычная функция не может спросить `is T`. `inline` с `reified` подставляет конкретный тип туда, где функцию вызвали, и такая проверка становится обычной.",
            codeSample = "inline fun <reified T> List<*>.firstIs(): Boolean = firstOrNull() is T",
            category = "KOTLIN",
            sortOrder = 1,
        ),
        Entry(
            title = "Дисперсия in и out",
            summary = "`out` отдаёт значения наружу, `in` принимает их внутрь, изменяемая коллекция инвариантна.",
            body = "Если тип только отдаёт значения, его можно читать как более общий. Если только принимает, в него можно передать более конкретное. `List` отдаёт элементы и объявлен как `out`. `MutableList` и читает, и пишет, поэтому звезда или другой аргумент типа туда не подставляются.",
            codeSample = "fun lengths(items: List<out CharSequence>): List<Int> = items.map { it.length }",
            category = "KOTLIN",
            sortOrder = 2,
        ),
        Entry(
            title = "Structured concurrency",
            summary = "Корутина живёт в scope: отмена родителя отменяет детей, scope дожидается их.",
            body = "Корутину не запускают в пустоту. Она принадлежит scope, и отмена этого scope отменяет детей. `coroutineScope` ждёт все дочерние задачи. Если одна падает, остальные отменяются, а ошибка выходит из функции наружу.",
            codeSample = "suspend fun load(): Pair<String, String> = coroutineScope {\n    val chapter = async { readChapter() }\n    val notes = async { readNotes() }\n    chapter.await() to notes.await()\n}",
            category = "KOTLIN",
            sortOrder = 3,
        ),
        Entry(
            title = "Кооперативная отмена",
            summary = "Отмена срабатывает в точках, где корутина сама проверяет свой Job.",
            body = "Отменённый Job сам не выдёргивает поток из чужого цикла. `delay`, `yield` и функции `kotlinx.coroutines` проверяют отмену сами. Свой долгий цикл остаётся живым, пока в нём нет `ensureActive()` или другой точки приостановки, которая смотрит на Job.",
            codeSample = "suspend fun poll() {\n    while (true) {\n        ensureActive()\n        readPacket()\n    }\n}",
            category = "KOTLIN",
            sortOrder = 4,
        ),
        Entry(
            title = "Dispatchers и withContext",
            summary = "`withContext` переносит блок на другой диспетчер и возвращает результат обратно.",
            body = "`Main` нужен интерфейсу, `Default` — вычислениям, `IO` — ожиданию файлов и сети. `withContext` выполняет блок в новом контексте. Результат блока возвращается туда, где функцию вызвали, без ручной передачи между потоками.",
            codeSample = "suspend fun parse(file: File): String = withContext(Dispatchers.IO) {\n    file.readText()\n}",
            category = "KOTLIN",
            sortOrder = 5,
        ),
        Entry(
            title = "Общее изменяемое состояние",
            summary = "Общий `var` либо живёт в одном потоке, либо закрыт `Mutex`.",
            body = "Две корутины могут прочитать одно значение и записать его обратно, потеряв чужое изменение. Примитивный `var` этого не запрещает. Состояние держат в одном диспетчере или защищают критическую секцию. `Mutex` пропускает в секцию одну корутину и отпускает её без блокировки потока.",
            codeSample = "val mutex = Mutex()\nvar count = 0\n\nsuspend fun inc() {\n    mutex.withLock { count++ }\n}",
            category = "KOTLIN",
            sortOrder = 6,
        ),
        Entry(
            title = "Flow, StateFlow, SharedFlow",
            summary = "Холодный Flow считается заново, StateFlow хранит значение, SharedFlow раздаёт события.",
            body = "Обычный Flow холодный: работа внутри `flow { }` начинается для каждого сборщика отдельно. StateFlow горячий и всегда помнит последнее значение, поэтому новый сборщик получает его сразу. SharedFlow тоже горячий, но предназначен для событий. Он не запускает источник сам, у него есть подписчики и настройка повтора.",
            codeSample = "fun ticks(): Flow<Int> = flow {\n    var n = 0\n    while (true) {\n        emit(n++)\n        delay(1_000)\n    }\n}",
            category = "KOTLIN",
            sortOrder = 7,
        ),
        Entry(
            title = "Исключения и supervisorScope",
            summary = "`supervisorScope` не отменяет соседей, но ошибку ребёнка всё равно нужно обработать.",
            body = "В `coroutineScope` падение одной дочерней корутины отменяет остальных, и функция завершается с ошибкой. `supervisorScope` изолирует это падение: соседние задачи продолжаются. Ошибка ребёнка при этом не исчезает. Её ловят вокруг конкретной задачи или отдают `CoroutineExceptionHandler`, иначе она уходит обработчику контекста.",
            codeSample = "suspend fun loadIndependently() = supervisorScope {\n    launch { risky() }\n    launch { steady() }\n}",
            category = "KOTLIN",
            sortOrder = 8,
        ),
        Entry(
            title = "Continuation",
            summary = "`suspend`-функция останавливается на точке приостановки и продолжается позже.",
            body = "Приостановка не блокирует поток. Компилятор превращает `suspend`-функцию в состояние, которое можно остановить и снова вызвать. После `delay` продолжение может пойти на другом потоке, если так устроен контекст. В коде это выглядит как обычный последовательный вызов.",
            codeSample = "suspend fun step() {\n    work()\n    delay(100)\n    work()\n}",
            category = "KOTLIN",
            sortOrder = 9,
        ),
        Entry(
            title = "Делегирование",
            summary = "`by` передаёт интерфейс другой реализации, а у свойства заменяет ручной геттер.",
            body = "Делегирование интерфейса пересылает его методы выбранному объекту. Класс остаётся этим интерфейсом и добавляет только своё. Делегированное свойство прячет хранение и первый расчёт. `lazy` считает значение при первом чтении, `observable` узнаёт о каждой новой записи.",
            codeSample = "interface Store {\n    fun read(): String\n}\n\nclass Repo(private val store: Store) : Store by store",
            category = "KOTLIN",
            sortOrder = 10,
        ),
        Entry(
            title = "Ментальная модель Compose",
            summary = "Экран — функция состояния. Новый state описывает кадр заново.",
            body = "Composable описывает, что должно быть на экране при текущем состоянии. При новом state функция выполняется снова, и дерево сравнивается с предыдущим. Виджеты не хранят скрытые изменения, которые обошли состояние. То, что должно сохраниться, лежит в state, а не в полях вью.",
            codeSample = "@Composable\nfun TitleCard(title: String) {\n    Text(title)\n}",
            category = "ANDROID",
            sortOrder = 1,
        ),
        Entry(
            title = "Рекомпозиция и стабильность",
            summary = "Заново выполняется участок, прочитавший новое состояние. Стабильные параметры позволяют пропуск.",
            body = "Compose запоминает, какие composable прочитали изменившееся состояние, и вызывает их снова. Сосед, который это состояние не читал, может быть пропущен. Пропуск возможен, когда параметры сравнимы и не изменились. Нестабильный параметр заставляет Compose перепроверять поддерево чаще.",
            codeSample = "@Composable\nfun Page(title: String, count: Int) {\n    Header(title)\n    Counter(count)\n}",
            category = "ANDROID",
            sortOrder = 2,
        ),
        Entry(
            title = "Три фазы кадра",
            summary = "Кадр проходит Composition, Layout и Drawing. Состояние читают в нужной фазе.",
            body = "Composition строит дерево функций. Layout измеряет детей и раскладывает их. Drawing рисует уже размещённое. Чтение состояния во время композиции связывает его с пересборкой дерева. Смещение, которое нужно только при размещении, читают в лямбде layout, и тогда изменение не пересобирает composable заново.",
            codeSample = "Modifier.offset { IntOffset(x.roundToPx(), 0) }",
            category = "ANDROID",
            sortOrder = 3,
        ),
        Entry(
            title = "Жизненный цикл composable",
            summary = "Composable входит в композицию и выходит из неё. `DisposableEffect` снимает работу на выходе.",
            body = "Вызов composable оставляет его в дереве, пока родитель продолжает его вызывать. Когда вызов исчез или сменился ключ, composable выходит из композиции. `DisposableEffect` запускает подписку на входе и обязан отпустить её в `onDispose`. Иначе слушатель переживёт экран, которого уже нет.",
            codeSample = "DisposableEffect(player) {\n    player.start()\n    onDispose { player.stop() }\n}",
            category = "ANDROID",
            sortOrder = 4,
        ),
        Entry(
            title = "Побочные эффекты",
            summary = "`LaunchedEffect` живёт вместе с композицией и ключом. Из тела composable эффект не запускают.",
            body = "Тело composable может выполниться много раз за кадр и при каждой рекомпозиции. Сетевой запрос, запущенный прямо оттуда, повторится вместе с ним. `LaunchedEffect` запускает корутину, пока composable в композиции, и отменяет её на выходе. Новый ключ отменяет старую корутину и запускает новую.",
            codeSample = "LaunchedEffect(topicId) {\n    repository.load(topicId)\n}",
            category = "ANDROID",
            sortOrder = 5,
        ),
        Entry(
            title = "Где хранить состояние",
            summary = "`remember` живёт в композиции, `rememberSaveable` переживает поворот, ViewModel хранит состояние экрана.",
            body = "`remember` возвращает то же значение, пока composable в дереве. Поворот Activity создаёт композицию заново, и такое значение пропадает. `rememberSaveable` кладёт то, что можно сохранить, в механизм saved instance state. Данные экрана и загрузка живут во ViewModel: он остаётся при пересоздании интерфейса.",
            codeSample = "val draft = rememberSaveable { mutableStateOf(\"\") }",
            category = "ANDROID",
            sortOrder = 6,
        ),
        Entry(
            title = "Смерть процесса",
            summary = "Процесс могут убить. `SavedStateHandle` возвращает минимум для восстановления экрана.",
            body = "ViewModel не переживает смерть процесса. Система может сохранить небольшой набор, который приложение само записало. `SavedStateHandle` читает этот набор при новом создании ViewModel. Туда кладут id открытой темы или другой ключ, без которого экран нельзя собрать заново.",
            codeSample = "class ReaderViewModel(saved: SavedStateHandle) : ViewModel() {\n    val topicId = saved.getStateFlow(\"topicId\", 0L)\n}",
            category = "ANDROID",
            sortOrder = 7,
        ),
        Entry(
            title = "Смена конфигурации",
            summary = "При повороте Activity создаётся заново, ViewModel того же scope остаётся.",
            body = "Смена конфигурации уничтожает Activity и создаёт новую. Scope ViewModel, привязанный к владельцу экрана, при этом сохраняется. Новая композиция читает уже загруженное состояние и не повторяет запрос только потому, что повернулся экран.",
            codeSample = "@HiltViewModel\nclass TopicsViewModel @Inject constructor(\n    private val repository: TopicRepository\n) : ViewModel()",
            category = "ANDROID",
            sortOrder = 8,
        ),
        Entry(
            title = "Главный поток",
            summary = "Композиция идёт на main. В кадр попадает готовое состояние, а не долгая работа.",
            body = "Чтение базы, разбор файла и сеть на главном потоке задерживают кадр. Такую работу запускают в корутине на подходящем диспетчере. Composable читает уже полученное состояние. Пока результата нет, экран показывает прежнее состояние или пометку ожидания, а не считает работу внутри композиции.",
            codeSample = "viewModelScope.launch {\n    _state.value = repository.load()\n}",
            category = "ANDROID",
            sortOrder = 9,
        ),
        Entry(
            title = "Сбор потока в UI",
            summary = "`collectAsStateWithLifecycle` собирает Flow, пока экран виден, и останавливает сборку в фоне.",
            body = "Обычный `collect` в композиции может держать работу, когда Activity уже на паузе. `collectAsStateWithLifecycle` привязывает сборку к жизненному циклу и по умолчанию собирает поток не ниже `STARTED`. Уход экрана в фон отменяет сборку. Возврат запускает её снова.",
            codeSample = "val topics by repository.observeAll().collectAsStateWithLifecycle()",
            category = "ANDROID",
            sortOrder = 10,
        ),
    )

    fun insert(db: SupportSQLiteDatabase) {
        entries.forEach { entry ->
            db.execSQL(
                "INSERT INTO topics (title, summary, body, codeSample, category, isFavorite, isBuiltIn, sortOrder, createdAt) " +
                    "VALUES (?, ?, ?, ?, ?, 0, 1, ?, 0)",
                arrayOf<Any?>(
                    entry.title,
                    entry.summary,
                    entry.body,
                    entry.codeSample,
                    entry.category,
                    entry.sortOrder,
                ),
            )
        }
    }
}
