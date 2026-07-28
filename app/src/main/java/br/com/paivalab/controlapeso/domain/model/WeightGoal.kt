package br.com.paivalab.controlapeso.domain.model

import java.time.Instant
import java.time.LocalDate

enum class GoalStatus {
    ACTIVE,
    PAUSED,
    COMPLETED
}

data class WeightGoal(
    val id: String,
    val profileId: String,
    val startWeightKg: Double,
    val targetWeightKg: Double,
    val startDate: LocalDate,
    val targetDate: LocalDate?,
    val status: GoalStatus,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    val isActive: Boolean
        get() = status == GoalStatus.ACTIVE
}
