package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_entries",
    indices = [
        Index("occurred_at"),
        Index("status"),
        Index("source_message_id"),
    ],
)
data class DailyEntryEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("source_message_id")
    val sourceMessageId: String? = null,
    @ColumnInfo("created_at")
    val createdAt: Long,
    @ColumnInfo("occurred_at")
    val occurredAt: Long? = null,
    @ColumnInfo("raw_text")
    val rawText: String,
    @ColumnInfo("summary")
    val summary: String = "",
    @ColumnInfo("mood_text")
    val moodText: String = "",
    @ColumnInfo("tags")
    val tags: String = "[]",
    @ColumnInfo("people")
    val people: String = "[]",
    @ColumnInfo("location")
    val location: String = "",
    @ColumnInfo("status")
    val status: String = STATUS_DRAFT,
    @ColumnInfo("is_private")
    val isPrivate: Boolean = true,
) {
    companion object {
        const val STATUS_DRAFT = "draft"
        const val STATUS_CONFIRMED = "confirmed"
        const val STATUS_ARCHIVED = "archived"
    }
}
