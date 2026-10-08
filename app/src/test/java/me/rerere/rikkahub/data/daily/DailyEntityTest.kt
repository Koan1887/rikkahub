package me.rerere.rikkahub.data.daily

import me.rerere.rikkahub.data.db.entity.DailyEntryEntity
import me.rerere.rikkahub.data.db.entity.DailyEventEntity
import me.rerere.rikkahub.data.db.entity.JournalDraftEntity
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyEntityTest {
    @Test
    fun `entry keeps raw text independently from generated fields`() {
        val entry = DailyEntryEntity(
            id = "entry-1",
            sourceMessageId = "message-1",
            createdAt = 100L,
            occurredAt = 200L,
            rawText = "我今天上了算法课",
            summary = "上算法课",
            moodText = "平静",
            tags = "[\"学习\"]",
            people = "[]",
            location = "学校",
            status = "draft",
            isPrivate = true,
        )

        assertEquals("我今天上了算法课", entry.rawText)
        assertEquals("上算法课", entry.summary)
        assertEquals("draft", entry.status)
        assertTrue(entry.isPrivate)
    }

    @Test
    fun `event distinguishes explicit and inferred certainty`() {
        val event = DailyEventEntity(
            id = "event-1",
            entryId = "entry-1",
            type = "event",
            content = "上算法课",
            certainty = "explicit",
            startTime = null,
            endTime = null,
        )

        assertEquals("explicit", event.certainty)
        assertEquals("entry-1", event.entryId)
    }

    @Test
    fun `journal draft stores source ids and editable status`() {
        val draft = JournalDraftEntity(
            id = "journal-1",
            date = "2026-10-08",
            content = "今天上了算法课。",
            sourceEntryIds = "[\"entry-1\"]",
            status = "edited",
        )

        assertEquals("2026-10-08", draft.date)
        assertEquals("[\"entry-1\"]", draft.sourceEntryIds)
        assertEquals("edited", draft.status)
    }
}
