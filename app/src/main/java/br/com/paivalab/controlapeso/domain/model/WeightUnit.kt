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
        String.format(Locale.getDefault(), "%.2f %s", fromKilograms(weightKg), symbol)

    fun formatInputFromKilograms(weightKg: Double): String =
        String.format(Locale.getDefault(), "%.2f", fromKilograms(weightKg))

    /**
     * Converts an already-entered value when the input unit changes. Keeping
     * this at the unit boundary prevents a form from silently reinterpreting
     * the same number as a different physical weight.
     */
    fun convertInput(value: String, from: WeightUnit): String {
        if (from == this || value.isBlank()) return value
        val parsed = value.trim().replace(',', '.').toDoubleOrNull()
            ?.takeIf(Double::isFinite)
            ?: return value
        return String.format(Locale.getDefault(), "%.2f", fromKilograms(from.toKilograms(parsed)))
    }

    companion object {
        const val POUNDS_PER_KILOGRAM = 2.2046226218
    }
}
