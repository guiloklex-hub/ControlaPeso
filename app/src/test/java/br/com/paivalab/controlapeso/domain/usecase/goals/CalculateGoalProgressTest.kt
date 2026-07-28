package br.com.paivalab.controlapeso.domain.usecase.goals

import br.com.paivalab.controlapeso.domain.model.GoalStatus
import br.com.paivalab.controlapeso.domain.model.WeightGoal
import java.time.Instant
import java.time.LocalDate
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateGoalProgressTest {
    @Test
    fun lossAndGainGoals_useDirectionWithoutMedicalJudgment() {
        val loss = CalculateGoalProgress(goal(80.0, 70.0), 75.0)
        assertEquals(0.5, loss?.progressFraction ?: -1.0, 0.0)
        assertEquals(5.0, loss?.remainingKg ?: -1.0, 0.0)

        val gain = CalculateGoalProgress(goal(60.0, 70.0), 71.0)
        assertTrue(gain?.reached == true)
        assertEquals(1.0, gain?.progressFraction ?: -1.0, 0.0)
    }

    @Test
    fun equalStartAndTargetOnlyCountsAsReachedAtThatWeight() {
        val notReached = CalculateGoalProgress(goal(75.0, 75.0), 80.0)
        val reached = CalculateGoalProgress(goal(75.0, 75.0), 75.0)

        assertTrue(notReached?.reached == false)
        assertEquals(5.0, notReached?.remainingKg ?: -1.0, 0.0)
        assertTrue(reached?.reached == true)
    }

    private fun goal(start: Double, target: Double) = WeightGoal(
        id = "g",
        profileId = "p",
        startWeightKg = start,
        targetWeightKg = target,
        startDate = LocalDate.of(2026, 7, 1),
        targetDate = null,
        status = GoalStatus.ACTIVE,
        createdAt = Instant.EPOCH,
        updatedAt = Instant.EPOCH
    )
}
