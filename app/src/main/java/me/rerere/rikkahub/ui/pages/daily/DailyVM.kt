package me.rerere.rikkahub.ui.pages.daily

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.rerere.rikkahub.data.db.entity.DailyEntryEntity
import me.rerere.rikkahub.data.db.entity.DailyEventEntity
import me.rerere.rikkahub.data.db.entity.JournalDraftEntity
import me.rerere.rikkahub.data.repository.DailyRepository
import me.rerere.rikkahub.data.repository.MemoryRepository
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class DailyVM(
    private val repository: DailyRepository,
    private val memoryRepository: MemoryRepository,
) : ViewModel() {
    val entries: StateFlow<List<DailyEntryEntity>> = repository
        .observeEntries()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun observeEntry(id: String) = repository.observeEntry(id)

    fun observeEvents(entryId: String) = repository.observeEvents(entryId)

    fun saveManual(rawText: String, onSaved: (DailyEntryEntity) -> Unit = {}) {
        viewModelScope.launch {
            if (rawText.isBlank()) return@launch
            onSaved(repository.createManualEntry(rawText.trim()))
        }
    }

    fun saveEntry(entry: DailyEntryEntity, events: List<DailyEventEntity> = emptyList()) {
        viewModelScope.launch {
            repository.updateEntry(entry)
            repository.replaceEvents(entry.id, events)
        }
    }

    fun confirm(entry: DailyEntryEntity) {
        viewModelScope.launch { repository.confirmEntry(entry) }
    }

    fun delete(id: String, onDeleted: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deleteEntry(id)
            onDeleted()
        }
    }

    fun promoteToLongTermMemory(entry: DailyEntryEntity) {
        viewModelScope.launch {
            val content = entry.summary.ifBlank { entry.rawText }.trim()
            if (content.isNotBlank()) {
                memoryRepository.addMemory(MemoryRepository.GLOBAL_MEMORY_ID, content)
            }
        }
    }

    fun setRoomListening(conversationId: String, enabled: Boolean) {
        viewModelScope.launch { repository.setListeningEnabled(conversationId, enabled) }
    }

    fun observeListening(conversationId: String) = repository.observeListeningEnabled(conversationId)

    fun observeJournal(date: String) = repository.observeJournalDraft(date)

    fun saveJournal(date: String, content: String, sourceEntryIds: String, status: String) {
        viewModelScope.launch {
            repository.saveJournalDraft(
                JournalDraftEntity(
                    id = UUID.nameUUIDFromBytes(date.toByteArray()).toString(),
                    date = date,
                    content = content,
                    sourceEntryIds = sourceEntryIds,
                    status = status,
                )
            )
        }
    }

    fun composeLocalJournal(date: LocalDate): String {
        val zone = ZoneId.systemDefault()
        val start = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val end = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val today = entries.value.filter {
            val time = it.occurredAt ?: it.createdAt
            time in start until end && it.status == DailyEntryEntity.STATUS_CONFIRMED
        }
        return today.joinToString("\n\n") { entry ->
            entry.summary.ifBlank { entry.rawText }
        }
    }
}
