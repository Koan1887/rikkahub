package me.rerere.rikkahub.data.daily

import me.rerere.rikkahub.data.db.entity.DailyEntryEntity
import me.rerere.rikkahub.data.db.entity.DailyEventEntity
import me.rerere.rikkahub.data.repository.DailyRepository
import me.rerere.rikkahub.utils.JsonInstant
import java.util.UUID

sealed interface DailyRecorderResult {
    val entry: DailyEntryEntity

    data class Draft(
        override val entry: DailyEntryEntity,
        val parsed: Boolean,
    ) : DailyRecorderResult

    data class Failed(
        override val entry: DailyEntryEntity,
        val errorMessage: String,
    ) : DailyRecorderResult
}

/** Converts a model response into a reviewable draft and never discards the source text. */
class DailyRecorder(private val repository: DailyRepository) {
    suspend fun record(
        rawText: String,
        sourceMessageId: String? = null,
        occurredAt: Long? = System.currentTimeMillis(),
        generate: suspend (systemPrompt: String, userText: String) -> String?,
    ): DailyRecorderResult {
        require(rawText.isNotBlank()) { "Daily record cannot be blank" }

        val now = System.currentTimeMillis()
        val response = runCatching {
            generate(DailyRecorderPrompt.SYSTEM, rawText)
        }.getOrElse { error ->
            return saveFailure(rawText, sourceMessageId, occurredAt, now, error.message ?: "request failed")
        }

        val parsed = response?.let(DailyExtractionParser::parse)
        if (parsed == null) {
            val entry = saveDraft(
                rawText = rawText,
                sourceMessageId = sourceMessageId,
                occurredAt = occurredAt,
                createdAt = now,
            )
            return DailyRecorderResult.Draft(entry = entry, parsed = false)
        }

        val entry = saveDraft(
            rawText = rawText,
            sourceMessageId = sourceMessageId,
            occurredAt = occurredAt,
            createdAt = now,
            summary = parsed.summary,
            moodText = parsed.feelings.joinToString("、"),
            tags = JsonInstant.encodeToString(parsed.tags),
            people = JsonInstant.encodeToString(parsed.people),
            location = parsed.places.joinToString("、"),
        )
        repository.replaceEvents(
            entryId = entry.id,
            events = parsed.events.map { event ->
                DailyEventEntity(
                    id = UUID.randomUUID().toString(),
                    entryId = entry.id,
                    type = event.type,
                    content = event.content,
                    certainty = event.certainty,
                    occurredAt = event.occurredAt ?: occurredAt,
                )
            } + parsed.tasks.map { task ->
                DailyEventEntity(
                    id = UUID.randomUUID().toString(),
                    entryId = entry.id,
                    type = DailyEventEntity.TYPE_TASK,
                    content = task,
                    certainty = DailyEventEntity.CERTAINTY_INFERRED,
                    occurredAt = occurredAt,
                )
            }
        )
        return DailyRecorderResult.Draft(entry = entry, parsed = true)
    }

    private suspend fun saveDraft(
        rawText: String,
        sourceMessageId: String?,
        occurredAt: Long?,
        createdAt: Long,
        summary: String = "",
        moodText: String = "",
        tags: String = "[]",
        people: String = "[]",
        location: String = "",
    ): DailyEntryEntity {
        val entry = DailyEntryEntity(
            id = UUID.randomUUID().toString(),
            sourceMessageId = sourceMessageId,
            createdAt = createdAt,
            occurredAt = occurredAt,
            rawText = rawText,
            summary = summary,
            moodText = moodText,
            tags = tags,
            people = people,
            location = location,
            status = DailyEntryEntity.STATUS_DRAFT,
        )
        repository.saveEntry(entry)
        return entry
    }

    private suspend fun saveFailure(
        rawText: String,
        sourceMessageId: String?,
        occurredAt: Long?,
        createdAt: Long,
        message: String,
    ): DailyRecorderResult.Failed {
        val entry = saveDraft(rawText, sourceMessageId, occurredAt, createdAt)
        return DailyRecorderResult.Failed(entry, message)
    }
}
