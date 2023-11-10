package app.gaborbiro.freelancecalculator

import app.gaborbiro.freelancecalculator.util.BigDecimalUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

private const val bigDecimalScale = 20
private val bigDecimalRounding = RoundingMode.HALF_EVEN

val DAYS_PER_YEAR: BigDecimal =
    BigDecimal.valueOf(365.25).setScale(bigDecimalScale, bigDecimalRounding)

val WEEKS_PER_YEAR = DAYS_PER_YEAR / BigDecimal.valueOf(7) // 52.18

val WEEKS_PER_MONTH =
    DAYS_PER_YEAR / BigDecimal.valueOf(7) / BigDecimal.valueOf(12) // 4.35

private val locale = Locale.getDefault()
private val numberFormat = (NumberFormat.getNumberInstance(locale) as DecimalFormat).also {
    it.isParseBigDecimal = true
}
private val groupedFormat = NumberFormat.getNumberInstance(locale).also {
    it.isGroupingUsed = true
    it.minimumFractionDigits = 0
    it.maximumFractionDigits = 0
} as DecimalFormat

fun BigDecimal?.format(decimalCount: Int): String {
    val format = NumberFormat.getNumberInstance(locale).also {
        it.isGroupingUsed = true
        it.minimumFractionDigits = decimalCount
        it.maximumFractionDigits = decimalCount
    }
    return this
        ?.setScale(decimalCount, RoundingMode.DOWN)
        ?.let { format.format(it) }
        ?: ""
}

fun String.formatWithCommas(): String {
    val decimalSeparator = groupedFormat.decimalFormatSymbols.decimalSeparator
    val indexOfDecimalSeparator = indexOf(decimalSeparator)
    return when {
        indexOfDecimalSeparator > -1 -> {
            val integerPart = substring(0, indexOfDecimalSeparator)
            val decimalPart = substring(indexOfDecimalSeparator)
            numberFormat.strictParse(integerPart)
                ?.toLong()
                ?.let { groupedFormat.format(it) + decimalPart }
                ?: this
        }

        else -> {
            numberFormat.strictParse(this)
                ?.toLong()
                ?.let { groupedFormat.format(it) }
                ?: this
        }
    }
}

fun String?.parse(): BigDecimal? {
    return this
        ?.trim()
        ?.let { numberFormat.strictParse(this) }
}

private fun DecimalFormat.strictParse(text: String): BigDecimal? = runCatching {
    val parsePosition = ParsePosition(0)
    val result = numberFormat.parse(text, parsePosition)
    if (parsePosition.index == text.length) {
        (result as BigDecimal).setScale(bigDecimalScale, bigDecimalRounding)
    } else {
        null// not all of the text was consumed, there's some illegal character
    }
}.getOrNull()

operator fun BigDecimal?.times(other: BigDecimal?): BigDecimal? {
    return safelyCalculate(this, other) { dis, other ->
        BigDecimalUtils().multiply(dis, other).setScale(bigDecimalScale, bigDecimalRounding)
    }
}

operator fun BigDecimal?.div(other: BigDecimal?): BigDecimal? {
    return safelyCalculate(this, other) { dis, other ->
        BigDecimalUtils().divide(dis, other).setScale(bigDecimalScale, bigDecimalRounding)
    }
}

private fun safelyCalculate(
    first: BigDecimal?,
    second: BigDecimal?,
    runnable: (BigDecimal, BigDecimal) -> BigDecimal
): BigDecimal? {
    return if (first != null && second != null) {
        runnable.invoke(first, second)
    } else {
        null
    }
}
