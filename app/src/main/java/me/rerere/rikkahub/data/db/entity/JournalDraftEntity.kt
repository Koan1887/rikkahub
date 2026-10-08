package me.rerere.rikkahub.data.db.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "journal_drafts",
    indices = [Index(value = ["date"], unique = true)],
)
data class JournalDraftEntity(
    @PrimaryKey
    val id: String,
    @ColumnInfo("date")
    val date: String,
    @ColumnInfo("content")
    val content: String,
    @ColumnInfo("source_entry_ids")
    val sourceEntryIds: String = "[]",
    @ColumnInfo("status")
    val status: String = STATUS_DRAFT,
) {
    companion object {
        const val STATUS_DRAFT = "draft"
        const val STATUS_EDITED = "edited"
        const val STATUS_SAVED = "saved"
    }
}
