package me.rerere.rikkahub.data.repository

import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.dao.DailyDAO
import me.rerere.rikkahub.data.db.entity.DailyEntryEntity
import me.rerere.rikkahub.data.db.entity.DailyEventEntity
import me.rerere.rikkahub.data.db.entity.DailyRoomSettingsEntity
import me.rerere.rikkahub.data.db.entity.JournalDraftEntity
import java.util.UUID

/**
 * Persistence boundary for personal daily records.
 *
 * The repository deliberately exposes confirmed-entry queries separately so callers
 * cannot accidentally use drafts when building journal or planner context.
 */
class DailyRepository(private val dao: DailyDAO) {
    fun observeEntries(): Flow<List<DailyEntryEntity>> = dao.observeEntries()

    fun observeEntry(id: String): Flow<DailyEntryEntity?> = dao.observeEntry(id)

    fun observeEvents(entryId: String): Flow<List<DailyEventEntity>> = dao.observeEvents(entryId)

    suspend fun getEntry(id: String): DailyEntryEntity? = dao.getEntry(id)

    suspend fun getConfirmedEntries(from: Long, to: Long): List<DailyEntryEntity> =
        dao.getConfirmedEntries(from, to)

    suspend fun createManualEntry(rawText: String, now: Long = System.currentTimeMillis()): DailyEntryEntity {
        val entry = DailyEntryEntity(
            id = UUID.randomUUID().toString(),
            createdAt = now,
            occurredAt = now,
            rawText = rawText,
            status = DailyEntryEntity.STATUS_CONFIRMED,
        )
        dao.insertEntry(entry)
        return entry
    }

    suspend fun saveEntry(entry: DailyEntryEntity) {
        require(entry.rawText.isNotBlank()) { "Daily entry rawText cannot be blank" }
        dao.insertEntry(entry)
    }

    suspend fun updateEntry(entry: DailyEntryEntity) {
        require(entry.rawText.isNotBlank()) { "Daily entry rawText cannot be blank" }
        dao.updateEntry(entry)
    }

    suspend fun confirmEntry(entry: DailyEntryEntity) {
        dao.updateEntry(entry.copy(status = DailyEntryEntity.STATUS_CONFIRMED))
    }

    suspend fun archiveEntry(entry: DailyEntryEntity) {
        dao.updateEntry(entry.copy(status = DailyEntryEntity.STATUS_ARCHIVED))
    }

    suspend fun deleteEntry(id: String) = dao.deleteEntry(id)

    suspend fun replaceEvents(entryId: String, events: List<DailyEventEntity>) {
        dao.deleteEvents(entryId)
        if (events.isNotEmpty()) dao.insertEvents(events)
    }

    suspend fun getJournalDraft(date: String): JournalDraftEntity? = dao.getJournalDraft(date)

    fun observeJournalDraft(date: String): Flow<JournalDraftEntity?> = dao.observeJournalDraft(date)

    suspend fun saveJournalDraft(draft: JournalDraftEntity) = dao.upsertJournalDraft(draft)

    suspend fun getRoomSettings(conversationId: String): DailyRoomSettingsEntity? =
        dao.getRoomSettings(conversationId)

    fun observeListeningEnabled(conversationId: String): Flow<Boolean?> =
        dao.observeListeningEnabled(conversationId)

    suspend fun setListeningEnabled(
        conversationId: String,
        enabled: Boolean,
        now: Long = System.currentTimeMillis(),
    ) {
        dao.upsertRoomSettings(
            DailyRoomSettingsEntity(
                conversationId = conversationId,
                listeningEnabled = enabled,
                updatedAt = now,
            )
        )
    }
}
