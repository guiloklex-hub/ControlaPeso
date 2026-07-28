package br.com.paivalab.controlapeso.domain.model

import java.util.Locale

enum class WeightUnit(val symbol: String) {
    KILOGRAM("kg"),
    POUND("lb");

    fun fromKilograms(weightKg: Double): Double = when (this) {
        KILOGRAM -> weightKg
        POUND -> weightKg * POUNDS_PER_KILOGRAM
    }

    fun toKilograms(value: Double): Double = when (this) {
        KILOGRAM -> value
        POUND -> value / POUNDS_PER_KILOGRAM
    }

    fun formatFromKilograms(weightKg: Double): String =
        String.format(Locale.getDefault(), "%.1f %s", fromKilograms(weightKg), symbol)

    companion object {
        const val POUNDS_PER_KILOGRAM = 2.2046226218
    }
}
