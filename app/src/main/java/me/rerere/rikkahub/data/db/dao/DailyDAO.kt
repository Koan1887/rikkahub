package me.rerere.rikkahub.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import me.rerere.rikkahub.data.db.entity.DailyEntryEntity
import me.rerere.rikkahub.data.db.entity.DailyEventEntity
import me.rerere.rikkahub.data.db.entity.DailyRoomSettingsEntity
import me.rerere.rikkahub.data.db.entity.JournalDraftEntity

@Dao
interface DailyDAO {
    @Query("SELECT * FROM daily_entries ORDER BY COALESCE(occurred_at, created_at) DESC")
    fun observeEntries(): Flow<List<DailyEntryEntity>>

    @Query("SELECT * FROM daily_entries WHERE id = :id")
    fun observeEntry(id: String): Flow<DailyEntryEntity?>

    @Query("SELECT * FROM daily_entries WHERE id = :id")
    suspend fun getEntry(id: String): DailyEntryEntity?

    @Query("SELECT * FROM daily_entries WHERE status = 'confirmed' AND COALESCE(occurred_at, created_at) >= :from AND COALESCE(occurred_at, created_at) < :to ORDER BY COALESCE(occurred_at, created_at) DESC")
    suspend fun getConfirmedEntries(from: Long, to: Long): List<DailyEntryEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: DailyEntryEntity)

    @Update
    suspend fun updateEntry(entry: DailyEntryEntity)

    @Query("DELETE FROM daily_entries WHERE id = :id")
    suspend fun deleteEntry(id: String)

    @Query("SELECT * FROM daily_events WHERE entry_id = :entryId ORDER BY COALESCE(occurred_at, start_time) ASC")
    fun observeEvents(entryId: String): Flow<List<DailyEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<DailyEventEntity>)

    @Query("DELETE FROM daily_events WHERE entry_id = :entryId")
    suspend fun deleteEvents(entryId: String)

    @Query("SELECT * FROM journal_drafts WHERE date = :date LIMIT 1")
    suspend fun getJournalDraft(date: String): JournalDraftEntity?

    @Query("SELECT * FROM journal_drafts WHERE date = :date LIMIT 1")
    fun observeJournalDraft(date: String): Flow<JournalDraftEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertJournalDraft(draft: JournalDraftEntity)

    @Query("SELECT * FROM daily_room_settings WHERE conversation_id = :conversationId")
    suspend fun getRoomSettings(conversationId: String): DailyRoomSettingsEntity?

    @Query("SELECT listening_enabled FROM daily_room_settings WHERE conversation_id = :conversationId")
    fun observeListeningEnabled(conversationId: String): Flow<Boolean?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRoomSettings(settings: DailyRoomSettingsEntity)
}
