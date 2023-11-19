package app.gaborbiro.freelancecalculator.util.hide

import app.gaborbiro.freelancecalculator.util.BigDecimalUtils
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

private const val SCALE = 2
private val ROUNDING = RoundingMode.HALF_EVEN

val DAYS_PER_YEAR: BigDecimal =
    BigDecimal.valueOf(365.25).setScale(SCALE, ROUNDING)

val WEEKS_PER_YEAR = DAYS_PER_YEAR / BigDecimal.valueOf(7) // 52.18

val WEEKS_PER_MONTH =
    DAYS_PER_YEAR / BigDecimal.valueOf(7) / BigDecimal.valueOf(12) // 4.35

private val locale = Locale.getDefault()
private val parsingFormat = (NumberFormat.getNumberInstance(locale) as DecimalFormat).also {
    it.isParseBigDecimal = true
}

fun BigDecimal?.format(decimalCount: Int): String {
    val format = NumberFormat.getNumberInstance(locale).also {
        it.isGroupingUsed = true
        it.minimumFractionDigits = 0
        it.maximumFractionDigits = decimalCount
    }
    return this
        ?.setScale(decimalCount, RoundingMode.DOWN)
        ?.let { format.format(it) }
        ?: ""
}

private val groupedFormat = NumberFormat.getNumberInstance(locale).also {
    it.isGroupingUsed = true
    it.minimumFractionDigits = 0
    it.maximumFractionDigits = 0
} as DecimalFormat

fun String.ensureGrouping(): String {
    val decimalSeparator = groupedFormat.decimalFormatSymbols.decimalSeparator
    val indexOfDecimalSeparator = indexOf(decimalSeparator)
    return when {
        indexOfDecimalSeparator > -1 -> {
            val integerPart = substring(0, indexOfDecimalSeparator)
            val tail = substring(indexOfDecimalSeparator)
            parsingFormat.strictParse(integerPart)
                ?.toLong()
                ?.let { groupedFormat.format(it) + tail }
                ?: this
        }

        else -> {
            parsingFormat.strictParse(this)
                ?.toLong()
                ?.let { groupedFormat.format(it) }
                ?: this
        }
    }
}

fun String?.strictParse(): BigDecimal? {
    return this
        ?.trim()
        ?.let { parsingFormat.strictParse(this) }
}

private fun DecimalFormat.strictParse(text: String): BigDecimal? = runCatching {
    val parsePosition = ParsePosition(0)
    val result = parsingFormat.parse(text, parsePosition)
    if (parsePosition.index != text.length) {
        null// not all of the text was consumed, there's some illegal character
    } else {
        (result as BigDecimal).setScale(SCALE, ROUNDING)
    }
}.getOrNull()

fun String.tryGrouping(): String {
    return this
        .trim()
        .let { text ->
            parsingFormat.strictParse(text)
                ?.let { text.ensureGrouping() }
                ?: text
        }
}

operator fun BigDecimal?.times(other: BigDecimal?): BigDecimal? {
    return safelyCalculate(this, other) { dis, other ->
        BigDecimalUtils().multiply(dis, other).setScale(SCALE, ROUNDING)
    }
}

operator fun BigDecimal?.div(other: BigDecimal?): BigDecimal? {
    return safelyCalculate(this, other) { dis, other ->
        BigDecimalUtils().divide(dis, other).setScale(SCALE, ROUNDING)
    }
}

operator fun BigDecimal?.minus(other: BigDecimal?): BigDecimal? {
    return safelyCalculate(this, other) { dis, other ->
        BigDecimalUtils().subtract(dis, other).setScale(SCALE, ROUNDING)
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
