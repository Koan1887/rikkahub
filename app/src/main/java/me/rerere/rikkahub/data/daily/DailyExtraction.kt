package me.rerere.rikkahub.data.daily

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import me.rerere.rikkahub.utils.JsonInstant

@Serializable
data class DailyExtractionResult(
    val summary: String = "",
    val events: List<DailyExtractionEvent> = emptyList(),
    val tasks: List<String> = emptyList(),
    val feelings: List<String> = emptyList(),
    val people: List<String> = emptyList(),
    val places: List<String> = emptyList(),
    val tags: List<String> = emptyList(),
    val needsConfirmation: Boolean = true,
)

@Serializable
data class DailyExtractionEvent(
    val type: String = "event",
    val content: String = "",
    val certainty: String = "inferred",
    val occurredAt: Long? = null,
)

object DailyExtractionParser {
    fun parse(text: String): DailyExtractionResult? {
        val json = text.trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
        if (json.isBlank()) return null
        return runCatching {
            JsonInstant.decodeFromString<DailyExtractionResult>(json).normalize()
        }.getOrNull()
    }

    private fun DailyExtractionResult.normalize(): DailyExtractionResult {
        return copy(
            summary = summary.trim(),
            events = events
                .filter { it.content.isNotBlank() }
                .map { event ->
                    event.copy(
                        type = event.type.takeIf { it in ALLOWED_EVENT_TYPES } ?: "event",
                        certainty = event.certainty.takeIf { it in ALLOWED_CERTAINTIES } ?: "inferred",
                        content = event.content.trim(),
                    )
                },
            tasks = tasks.map(String::trim).filter(String::isNotBlank),
            feelings = feelings.map(String::trim).filter(String::isNotBlank),
            people = people.map(String::trim).filter(String::isNotBlank),
            places = places.map(String::trim).filter(String::isNotBlank),
            tags = tags.map(String::trim).filter(String::isNotBlank).distinct(),
        )
    }

    private val ALLOWED_EVENT_TYPES = setOf("event", "task", "feeling", "person", "place")
    private val ALLOWED_CERTAINTIES = setOf("explicit", "inferred")
}

object DailyRecorderPrompt {
    const val SYSTEM = """
You are Daily Recorder. Extract only what the user explicitly said or cautiously marked as inferred.
Do not turn words such as '可能', '打算', or '应该' into a completed fact.
Return JSON only with this shape:
{"summary":"","events":[{"type":"event","content":"","certainty":"explicit","occurredAt":null}],"tasks":[],"feelings":[],"people":[],"places":[],"tags":[],"needsConfirmation":true}
Every result needs user confirmation. Never claim to have saved or completed a task.
"""
}
