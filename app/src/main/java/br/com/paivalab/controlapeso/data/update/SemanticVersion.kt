package br.com.paivalab.controlapeso.data.update

/**
 * Strict Semantic Versioning parser used for release tags and versionName.
 * A leading `v` is accepted only as a conventional Git tag prefix.
 */
data class SemanticVersion(
    val major: Long,
    val minor: Long,
    val patch: Long,
    val preRelease: List<Identifier> = emptyList()
) : Comparable<SemanticVersion> {
    val isStable: Boolean get() = preRelease.isEmpty()

    override fun compareTo(other: SemanticVersion): Int {
        compareValues(major, other.major).takeIf { it != 0 }?.let { return it }
        compareValues(minor, other.minor).takeIf { it != 0 }?.let { return it }
        compareValues(patch, other.patch).takeIf { it != 0 }?.let { return it }
        if (preRelease.isEmpty() && other.preRelease.isEmpty()) return 0
        if (preRelease.isEmpty()) return 1
        if (other.preRelease.isEmpty()) return -1
        val common = minOf(preRelease.size, other.preRelease.size)
        for (index in 0 until common) {
            val comparison = preRelease[index].compareTo(other.preRelease[index])
            if (comparison != 0) return comparison
        }
        return preRelease.size.compareTo(other.preRelease.size)
    }

    override fun toString(): String = buildString {
        append("$major.$minor.$patch")
        if (preRelease.isNotEmpty()) append("-").append(preRelease.joinToString("."))
    }

    class Identifier private constructor(
        private val value: String,
        private val numeric: Long?
    ) : Comparable<Identifier> {
        override fun compareTo(other: Identifier): Int = when {
            numeric != null && other.numeric != null -> numeric.compareTo(other.numeric)
            numeric != null -> -1
            other.numeric != null -> 1
            else -> value.compareTo(other.value)
        }

        override fun toString(): String = value

        override fun equals(other: Any?): Boolean =
            other is Identifier && value == other.value

        override fun hashCode(): Int = value.hashCode()

        companion object {
            fun parse(value: String): Identifier? {
                if (!IDENTIFIER.matches(value)) return null
                val numeric = if (value.all(Char::isDigit)) {
                    if (value.length > 1 && value.startsWith('0')) return null
                    value.toLongOrNull() ?: return null
                } else {
                    null
                }
                return Identifier(value, numeric)
            }

            private val IDENTIFIER = Regex("[0-9A-Za-z-]+")
        }
    }

    companion object {
        private val VERSION = Regex(
            "^(0|[1-9]\\d*)\\.(0|[1-9]\\d*)\\.(0|[1-9]\\d*)" +
                "(?:-([0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*))?" +
                "(?:\\+[0-9A-Za-z-]+(?:\\.[0-9A-Za-z-]+)*)?$"
        )

        fun parse(raw: String): SemanticVersion? {
            val normalized = raw.trim().removePrefix("v")
            val match = VERSION.matchEntire(normalized) ?: return null
            val major = match.groupValues[1].toLongOrNull() ?: return null
            val minor = match.groupValues[2].toLongOrNull() ?: return null
            val patch = match.groupValues[3].toLongOrNull() ?: return null
            val preRelease = match.groupValues[4]
                .takeIf(String::isNotEmpty)
                ?.split('.')
                ?.map(Identifier::parse)
                ?.takeIf { identifiers -> identifiers.none { it == null } }
                ?.filterNotNull()
                ?: if (match.groupValues[4].isEmpty()) emptyList() else return null
            return SemanticVersion(major, minor, patch, preRelease)
        }
    }
}
