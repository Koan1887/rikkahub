package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "daily_room_settings")
data class DailyRoomSettingsEntity(
    @PrimaryKey
    @ColumnInfo("conversation_id")
    val conversationId: String,
    @ColumnInfo("listening_enabled")
    val listeningEnabled: Boolean = false,
    @ColumnInfo("updated_at")
    val updatedAt: Long,
)
