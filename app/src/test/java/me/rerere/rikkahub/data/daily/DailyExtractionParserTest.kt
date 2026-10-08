package me.rerere.rikkahub.data.daily

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DailyExtractionParserTest {
    @Test
    fun `parses structured extraction and keeps inferred certainty`() {
        val result = DailyExtractionParser.parse(
            """
            {
              "summary":"上算法课",
              "events":[{"type":"event","content":"上算法课","certainty":"explicit","occurredAt":null}],
              "tasks":[],"feelings":[],"people":[],"places":[],"tags":["学习"],"needsConfirmation":true
            }
            """.trimIndent()
        )

        assertTrue(result != null)
        assertEquals("上算法课", result?.summary)
        assertEquals("explicit", result?.events?.single()?.certainty)
        assertEquals(listOf("学习"), result?.tags)
        assertTrue(result?.needsConfirmation == true)
    }

    @Test
    fun `malformed extraction returns null so caller can preserve raw draft`() {
        assertNull(DailyExtractionParser.parse("not json"))
    }

    @Test
    fun `missing certainty is treated as inferred`() {
        val result = DailyExtractionParser.parse(
            """{"summary":"可能去跑步","events":[{"type":"event","content":"去跑步"}]}"""
        )

        assertEquals("inferred", result?.events?.single()?.certainty)
    }
}
