package me.rerere.rikkahub.data.daily

import kotlinx.serialization.Serializable
import me.rerere.rikkahub.data.db.entity.DailyEntryEntity
import me.rerere.rikkahub.data.repository.DailyRepository
import java.time.LocalDate
import java.time.ZoneId

@Serializable
data class PlannerTaskContext(
    val id: String,
    val title: String,
    val dueAt: Long? = null,
    val priority: String = "normal",
    val estimatedMinutes: Int? = null,
)

@Serializable
data class PlannerDailyEntryContext(
    val id: String,
    val occurredAt: Long?,
    val summary: String,
    val moodText: String,
    val tags: String,
    val rawText: String,
)

/** Explicit boundary for Hermes planning input. Drafts and archived entries never enter it. */
@Serializable
data class PlannerContext(
    val currentState: Map<String, String> = emptyMap(),
    val tasks: List<PlannerTaskContext> = emptyList(),
    val relevantDailyEntries: List<PlannerDailyEntryContext> = emptyList(),
    val confirmedMemories: List<String> = emptyList(),
)

class PlannerContextBuilder(private val dailyRepository: DailyRepository) {
    suspend fun build(
        date: LocalDate,
        currentState: Map<String, String> = emptyMap(),
        tasks: List<PlannerTaskContext> = emptyList(),
        confirmedMemories: List<String> = emptyList(),
    ): PlannerContext {
        val zone = ZoneId.systemDefault()
        val from = date.atStartOfDay(zone).toInstant().toEpochMilli()
        val to = date.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli()
        val entries = dailyRepository.getConfirmedEntries(from, to)
        return PlannerContext(
            currentState = currentState,
            tasks = tasks,
            relevantDailyEntries = entries.map(DailyEntryEntity::toPlannerContext),
            confirmedMemories = confirmedMemories,
        )
    }
}

private fun DailyEntryEntity.toPlannerContext() = PlannerDailyEntryContext(
    id = id,
    occurredAt = occurredAt ?: createdAt,
    summary = summary,
    moodText = moodText,
    tags = tags,
    rawText = rawText,
)
