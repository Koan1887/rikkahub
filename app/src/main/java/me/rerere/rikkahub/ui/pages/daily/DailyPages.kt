package me.rerere.rikkahub.ui.pages.daily

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeFlexibleTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.rerere.hugeicons.HugeIcons
import me.rerere.hugeicons.stroke.Book01
import me.rerere.hugeicons.stroke.Delete01
import me.rerere.hugeicons.stroke.PencilEdit01
import me.rerere.hugeicons.stroke.Settings03
import me.rerere.hugeicons.stroke.Sparkles
import me.rerere.hugeicons.stroke.Tick01
import me.rerere.rikkahub.Screen
import me.rerere.rikkahub.data.db.entity.DailyEntryEntity
import me.rerere.rikkahub.data.db.entity.DailyEventEntity
import me.rerere.rikkahub.ui.components.nav.BackButton
import me.rerere.rikkahub.ui.context.LocalNavController
import me.rerere.rikkahub.ui.context.Navigator
import me.rerere.rikkahub.ui.theme.CustomColors
import me.rerere.rikkahub.utils.plus
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val dailyDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

@Composable
fun DailyHomeScreen(vm: DailyVM = koinViewModel()) {
    val navController = LocalNavController.current
    val entries by vm.entries.collectAsStateWithLifecycle()
    var showInput by remember { mutableStateOf(false) }
    val today = LocalDate.now()
    val todayEntries = entries.filter { it.isOn(today) }

    Scaffold(
        topBar = {
            LargeFlexibleTopAppBar(
                title = { Text("Daily 日常") },
                navigationIcon = { BackButton() },
                colors = CustomColors.topBarColors,
            )
        },
        containerColor = CustomColors.topBarColors.containerColor,
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(
                    text = "把今天发生的事留下来，原文会一直保留。",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 4.dp),
                )
            }
            item {
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(onClick = { showInput = true }, modifier = Modifier.weight(1f)) {
                        Icon(HugeIcons.PencilEdit01, contentDescription = null)
                        Text("记一下", modifier = Modifier.padding(start = 6.dp))
                    }
                    Button(
                        onClick = { navController.navigate(Screen.DailyTimeline) },
                        modifier = Modifier.weight(1f),
                    ) {
                        Icon(HugeIcons.Book01, contentDescription = null)
                        Text("今日回顾", modifier = Modifier.padding(start = 6.dp))
                    }
                }
            }
            item {
                DailySectionCard(title = "今天", subtitle = "${todayEntries.size} 条记录") {
                    if (todayEntries.isEmpty()) {
                        Text("还没有确认过的日常记录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        todayEntries.take(3).forEach { entry ->
                            DailyEntryRow(entry = entry, onClick = {
                                navController.navigate(Screen.DailyEntryDetail(entry.id))
                            })
                        }
                    }
                }
            }
            item {
                DailySectionCard(title = "待确认", subtitle = "AI 生成的记录需要你确认") {
                    val drafts = entries.filter { it.status == DailyEntryEntity.STATUS_DRAFT }
                    if (drafts.isEmpty()) {
                        Text("没有待确认记录。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else {
                        drafts.take(3).forEach { entry ->
                            DailyEntryRow(entry = entry, onClick = {
                                navController.navigate(Screen.DailyEntryDetail(entry.id))
                            })
                        }
                    }
                }
            }
            item {
                Button(
                    onClick = { navController.navigate(Screen.JournalDraft(today.toString())) },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Icon(HugeIcons.Sparkles, contentDescription = null)
                    Text("整理今天", modifier = Modifier.padding(start = 6.dp))
                }
            }
            item {
                TextButton(
                    onClick = { navController.navigate(Screen.MemoryReview) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text("检查哪些记录可以进入长期记忆") }
            }
        }
    }

    if (showInput) {
        var text by remember { mutableStateOf("") }
        AlertDialog(
            onDismissRequest = { showInput = false },
            title = { Text("记一下") },
            text = {
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 4,
                    placeholder = { Text("写下今天发生的事") },
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.saveManual(text) { entry ->
                            showInput = false
                            navController.navigate(Screen.DailyEntryDetail(entry.id))
                        }
                    },
                    enabled = text.isNotBlank(),
                ) { Text("保存原文") }
            },
            dismissButton = { TextButton(onClick = { showInput = false }) { Text("取消") } },
        )
    }
}

@Composable
fun DailyTimelineScreen(vm: DailyVM = koinViewModel()) {
    val navController = LocalNavController.current
    val entries by vm.entries.collectAsStateWithLifecycle()
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("日常时间线") },
                navigationIcon = { BackButton() },
                actions = {
                    IconButton(onClick = { navController.navigate(Screen.JournalDraft(LocalDate.now().toString())) }) {
                        Icon(HugeIcons.Book01, contentDescription = "整理今天")
                    }
                },
            )
        },
    ) { padding ->
        if (entries.isEmpty()) {
            EmptyDailyState("还没有日常记录", padding)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding + PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(entries, key = { it.id }) { entry ->
                    DailyTimelineCard(entry) {
                        navController.navigate(Screen.DailyEntryDetail(entry.id))
                    }
                }
            }
        }
    }
}

@Composable
fun DailyEntryDetailScreen(id: String, vm: DailyVM = koinViewModel()) {
    val navController = LocalNavController.current
    val entry by vm.observeEntry(id).collectAsStateWithLifecycle(initialValue = null)
    val events by vm.observeEvents(id).collectAsStateWithLifecycle(initialValue = emptyList())
    var summary by remember(id) { mutableStateOf("") }
    var mood by remember(id) { mutableStateOf("") }
    var location by remember(id) { mutableStateOf("") }
    var tags by remember(id) { mutableStateOf("") }
    var editableEvents = remember(id) { mutableStateListOf<DailyEventEntity>() }
    var showDelete by remember { mutableStateOf(false) }

    LaunchedEffect(entry) {
        entry?.let {
            summary = it.summary
            mood = it.moodText
            location = it.location
            tags = it.tags.removePrefix("[").removeSuffix("]").replace("\"", "")
        }
    }
    LaunchedEffect(events) {
        editableEvents.clear()
        editableEvents.addAll(events)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("记录详情") },
                navigationIcon = { BackButton() },
                actions = {
                    IconButton(onClick = { showDelete = true }) {
                        Icon(HugeIcons.Delete01, contentDescription = "删除记录")
                    }
                },
            )
        },
    ) { padding ->
        val current = entry
        if (current == null) {
            EmptyDailyState("记录不存在", padding)
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = padding + PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                item {
                    DailySectionCard(title = "原始内容", subtitle = "原文不会被 AI 摘要替换") {
                        Text(current.rawText, style = MaterialTheme.typography.bodyLarge)
                    }
                }
                item {
                    DailySectionCard(title = "AI 字段", subtitle = "可以分别编辑；状态：${current.status}") {
                        OutlinedTextField(summary, { summary = it }, label = { Text("摘要") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(mood, { mood = it }, label = { Text("感受") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(location, { location = it }, label = { Text("地点") }, modifier = Modifier.fillMaxWidth())
                        OutlinedTextField(tags, { tags = it }, label = { Text("标签，用逗号分隔") }, modifier = Modifier.fillMaxWidth())
                    }
                }
                item {
                    DailySectionCard(title = "事件", subtitle = "certainty 会保留 explicit / inferred") {
                        if (editableEvents.isEmpty()) {
                            Text("没有抽取到事件。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        } else {
                            editableEvents.forEachIndexed { index, event ->
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    OutlinedTextField(
                                        value = event.content,
                                        onValueChange = { editableEvents[index] = event.copy(content = it) },
                                        label = { Text("${event.type} · ${event.certainty}") },
                                        modifier = Modifier.weight(1f),
                                    )
                                    IconButton(onClick = { editableEvents.removeAt(index) }) {
                                        Icon(HugeIcons.Delete01, contentDescription = "删除事件")
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                vm.saveEntry(
                                    current.copy(
                                        summary = summary,
                                        moodText = mood,
                                        location = location,
                                        tags = tags.split(',').map(String::trim).filter(String::isNotBlank)
                                            .joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"")
                                            .let { if (it == "[\"\"]") "[]" else it },
                                    ),
                                    editableEvents.toList(),
                                )
                            },
                            modifier = Modifier.weight(1f),
                        ) { Text("保存修改") }
                        if (current.status == DailyEntryEntity.STATUS_DRAFT) {
                            Button(onClick = { vm.confirm(current) }, modifier = Modifier.weight(1f)) {
                                Icon(HugeIcons.Tick01, contentDescription = null)
                                Text("确认", modifier = Modifier.padding(start = 4.dp))
                            }
                        }
                    }
                }
            }
        }
    }

    if (showDelete) {
        AlertDialog(
            onDismissRequest = { showDelete = false },
            title = { Text("删除这条记录？") },
            text = { Text("原始内容和 AI 字段都会被删除，操作不可撤销。") },
            confirmButton = {
                TextButton(onClick = { vm.delete(id) { navController.popBackStack() } }) { Text("删除") }
            },
            dismissButton = { TextButton(onClick = { showDelete = false }) { Text("取消") } },
        )
    }
}

@Composable
fun JournalDraftScreen(date: String, vm: DailyVM = koinViewModel()) {
    val draft by vm.observeJournal(date).collectAsStateWithLifecycle(initialValue = null)
    var content by remember(date) { mutableStateOf("") }
    val entries by vm.entries.collectAsStateWithLifecycle()
    LaunchedEffect(draft, entries) {
        if (draft != null) content = draft!!.content
        else if (content.isBlank()) content = vm.composeLocalJournal(LocalDate.parse(date))
    }
    val navController = LocalNavController.current
    Scaffold(
        topBar = {
            TopAppBar(title = { Text("日记草稿 · $date") }, navigationIcon = { BackButton() })
        },
    ) { padding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text("这是由已确认记录整理出的本地草稿，保存前可以自由编辑。", color = MaterialTheme.colorScheme.onSurfaceVariant)
            OutlinedTextField(
                value = content,
                onValueChange = { content = it },
                modifier = Modifier.fillMaxWidth().weight(1f),
                minLines = 10,
                label = { Text("日记内容") },
            )
            Button(
                onClick = {
                    val ids = entries.filter { it.isOn(LocalDate.parse(date)) && it.status == DailyEntryEntity.STATUS_CONFIRMED }
                        .joinToString(prefix = "[\"", postfix = "\"]", separator = "\",\"")
                        .let { if (it == "[\"\"]") "[]" else it }
                    vm.saveJournal(date, content, ids, JournalDraftStatusFor(content, draft?.content))
                    navController.popBackStack()
                },
                modifier = Modifier.fillMaxWidth(),
            ) { Text("保存日记") }
        }
    }
}

@Composable
fun RoomSettingsScreen(conversationId: String, vm: DailyVM = koinViewModel()) {
    val enabled by vm.observeListening(conversationId).collectAsStateWithLifecycle(initialValue = false)
    Scaffold(
        topBar = { TopAppBar(title = { Text("聊天室设置") }, navigationIcon = { BackButton() }) },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("旁听记录", style = MaterialTheme.typography.titleMedium)
                        Text(
                            "开启后只读取这个聊天室的用户消息，生成 Daily 待确认草稿；Daily 不会主动在聊天室发言。",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                    Switch(
                        checked = enabled == true,
                        onCheckedChange = { vm.setRoomListening(conversationId, it) },
                    )
                }
            }
        }
    }
}

@Composable
fun MemoryReviewScreen(vm: DailyVM = koinViewModel()) {
    val entries by vm.entries.collectAsStateWithLifecycle()
    Scaffold(topBar = { TopAppBar(title = { Text("长期记忆确认") }, navigationIcon = { BackButton() }) }) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = padding + PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            item { Text("Daily 记录不会自动变成长期记忆。这里先展示已确认记录，后续由你逐条选择。", color = MaterialTheme.colorScheme.onSurfaceVariant) }
            items(entries.filter { it.status == DailyEntryEntity.STATUS_CONFIRMED }, key = { it.id }) { entry ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
                    shape = MaterialTheme.shapes.extraLarge,
                ) {
                    Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(entry.summary.ifBlank { entry.rawText }, style = MaterialTheme.typography.titleMedium)
                        Text("只有点击确认才会写入长期记忆。", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Button(onClick = { vm.promoteToLongTermMemory(entry) }) {
                            Icon(HugeIcons.Tick01, contentDescription = null)
                            Text("加入长期记忆", modifier = Modifier.padding(start = 6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DailySectionCard(title: String, subtitle: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(subtitle, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
            content()
        }
    }
}

@Composable
private fun DailyEntryRow(entry: DailyEntryEntity, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        color = Color.Transparent,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier.padding(vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(HugeIcons.Sparkles, null, modifier = Modifier.size(20.dp))
            Column(modifier = Modifier.weight(1f).padding(start = 10.dp)) {
                Text(entry.summary.ifBlank { entry.rawText }, maxLines = 2, overflow = TextOverflow.Ellipsis)
                Text(entry.status, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun DailyTimelineCard(entry: DailyEntryEntity, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainerLow),
        shape = MaterialTheme.shapes.extraLarge,
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(entry.timeText(), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(entry.summary.ifBlank { entry.rawText }, style = MaterialTheme.typography.titleMedium)
            if (entry.summary.isNotBlank()) {
                Text(entry.rawText, maxLines = 3, overflow = TextOverflow.Ellipsis, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(entry.status, style = MaterialTheme.typography.labelSmall)
                if (entry.isPrivate) Text("仅本人", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmptyDailyState(message: String, padding: PaddingValues) {
    Column(
        modifier = Modifier.fillMaxSize().padding(padding),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) { Text(message, color = MaterialTheme.colorScheme.onSurfaceVariant) }
}

private fun DailyEntryEntity.isOn(date: LocalDate): Boolean {
    val millis = occurredAt ?: createdAt
    val localDate = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
    return localDate == date
}

private fun DailyEntryEntity.timeText(): String =
    Instant.ofEpochMilli(occurredAt ?: createdAt).atZone(ZoneId.systemDefault()).format(dailyDateFormatter)

private fun JournalDraftStatusFor(content: String, original: String?): String =
    when {
        original == null -> "draft"
        content == original -> "saved"
        else -> "edited"
    }
