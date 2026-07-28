package br.com.paivalab.controlapeso.ui.preview

import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import java.time.Instant
import java.time.LocalDate

object PreviewGoals {
    val active = WeightGoal(
        id = "preview-goal",
        profileId = PreviewProfiles.primary.id,
        startWeightKg = 80.1,
        targetWeightKg = 75.0,
        startDate = LocalDate.of(2026, 6, 1),
        targetDate = LocalDate.of(2026, 12, 1),
        status = GoalStatus.ACTIVE,
        createdAt = Instant.parse("2026-06-01T12:00:00Z"),
        updatedAt = Instant.parse("2026-07-28T12:00:00Z")
    )
}

