package br.com.paivalab.controlapeso.bluetooth

object BleHexFormatter {
    private const val HEX_DIGITS = "0123456789ABCDEF"

    fun format(bytes: ByteArray): String {
        if (bytes.isEmpty()) return ""

        return buildString(bytes.size * 3 - 1) {
            bytes.forEachIndexed { index, byte ->
                if (index > 0) append(' ')
                val value = byte.toInt() and 0xFF
                append(HEX_DIGITS[value ushr 4])
                append(HEX_DIGITS[value and 0x0F])
            }
        }
    }
}
