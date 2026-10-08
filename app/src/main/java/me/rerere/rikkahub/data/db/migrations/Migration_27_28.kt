package me.rerere.rikkahub.data.db.migrations

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/** Adds personal daily records without touching existing conversation or memory data. */
val Migration_27_28 = object : Migration(27, 28) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS daily_entries (
                id TEXT NOT NULL,
                source_message_id TEXT,
                created_at INTEGER NOT NULL,
                occurred_at INTEGER,
                raw_text TEXT NOT NULL,
                summary TEXT NOT NULL,
                mood_text TEXT NOT NULL,
                tags TEXT NOT NULL,
                people TEXT NOT NULL,
                location TEXT NOT NULL,
                status TEXT NOT NULL,
                is_private INTEGER NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_entries_occurred_at ON daily_entries(occurred_at)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_entries_status ON daily_entries(status)")
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_entries_source_message_id ON daily_entries(source_message_id)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS daily_events (
                id TEXT NOT NULL,
                entry_id TEXT NOT NULL,
                type TEXT NOT NULL,
                content TEXT NOT NULL,
                certainty TEXT NOT NULL,
                occurred_at INTEGER,
                start_time INTEGER,
                end_time INTEGER,
                PRIMARY KEY(id),
                FOREIGN KEY(entry_id) REFERENCES daily_entries(id) ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS index_daily_events_entry_id ON daily_events(entry_id)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS journal_drafts (
                id TEXT NOT NULL,
                date TEXT NOT NULL,
                content TEXT NOT NULL,
                source_entry_ids TEXT NOT NULL,
                status TEXT NOT NULL,
                PRIMARY KEY(id)
            )
            """.trimIndent()
        )
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_journal_drafts_date ON journal_drafts(date)")
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS daily_room_settings (
                conversation_id TEXT NOT NULL,
                listening_enabled INTEGER NOT NULL,
                updated_at INTEGER NOT NULL,
                PRIMARY KEY(conversation_id)
            )
            """.trimIndent()
        )
    }
}
