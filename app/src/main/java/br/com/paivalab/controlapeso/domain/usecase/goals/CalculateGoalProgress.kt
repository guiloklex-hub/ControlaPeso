package br.com.paivalab.controlapeso.domain.usecase.goals

import br.com.paivalab.controlapeso.domain.model.WeightGoal
import kotlin.math.abs

data class GoalProgress(
    val progressFraction: Double,
    val changeKg: Double,
    val remainingKg: Double,
    val reached: Boolean
)

object CalculateGoalProgress {
    operator fun invoke(goal: WeightGoal, currentWeightKg: Double): GoalProgress? {
        if (!currentWeightKg.isFinite()) return null
        val total = goal.targetWeightKg - goal.startWeightKg
        if (total == 0.0) {
            val reached = abs(currentWeightKg - goal.targetWeightKg) < WEIGHT_EPSILON_KG
            return GoalProgress(
                progressFraction = if (reached) 1.0 else 0.0,
                changeKg = currentWeightKg - goal.startWeightKg,
                remainingKg = if (reached) {
                    0.0
                } else {
                    abs(goal.targetWeightKg - currentWeightKg)
                },
                reached = reached
            )
        }
        val change = currentWeightKg - goal.startWeightKg
        val rawProgress = change / total
        val reached = if (total < 0) {
            currentWeightKg <= goal.targetWeightKg
        } else {
            currentWeightKg >= goal.targetWeightKg
        }
        return GoalProgress(
            progressFraction = rawProgress.coerceIn(0.0, 1.0),
            changeKg = change,
            remainingKg = if (reached) 0.0 else abs(goal.targetWeightKg - currentWeightKg),
            reached = reached
        )
    }

    private const val WEIGHT_EPSILON_KG = 0.0001
}
