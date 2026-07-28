package br.com.paivalab.controlapeso.domain.usecase.statistics

object CalculateBmi {
    operator fun invoke(weightKg: Double, heightCm: Double?): Double? {
        if (!weightKg.isFinite() || weightKg <= 0.0) return null
        if (heightCm == null || !heightCm.isFinite() || heightCm !in 80.0..250.0) {
            return null
        }
        val heightMeters = heightCm / 100.0
        return weightKg / (heightMeters * heightMeters)
    }
}
