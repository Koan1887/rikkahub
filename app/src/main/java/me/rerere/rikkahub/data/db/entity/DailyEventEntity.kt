package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "daily_events",
    foreignKeys = [
        ForeignKey(
            entity = DailyEntryEntity::class,
            parentColumns = ["id"],
            childColumns = ["entry_id"],
            onDelete = ForeignKey.CASCADE,
        )
    ],
    indices = [Index("entry_id")],
)
data class DailyEventEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("entry_id")
    val entryId: String,
    @ColumnInfo("type")
    val type: String,
    @ColumnInfo("content")
    val content: String,
    @ColumnInfo("certainty")
    val certainty: String,
    @ColumnInfo("occurred_at")
    val occurredAt: Long? = null,
    @ColumnInfo("start_time")
    val startTime: Long? = null,
    @ColumnInfo("end_time")
    val endTime: Long? = null,
) {
    companion object {
        const val TYPE_EVENT = "event"
        const val TYPE_TASK = "task"
        const val TYPE_FEELING = "feeling"
        const val TYPE_PERSON = "person"
        const val TYPE_PLACE = "place"
        const val CERTAINTY_EXPLICIT = "explicit"
        const val CERTAINTY_INFERRED = "inferred"
    }
}
