package app.gaborbiro.freelancecalculator

import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale

const val DAYS_PER_YEAR = 365.25

const val WEEKS_PER_YEAR = DAYS_PER_YEAR / 7

const val WEEKS_PER_MONTH = DAYS_PER_YEAR / 7 / 12 // 4.348214285714286

private val locale = Locale.getDefault()
private val numberFormat = NumberFormat.getNumberInstance(locale) as DecimalFormat
private val groupedFormat = NumberFormat.getNumberInstance(locale).also {
    it.isGroupingUsed = true
    it.minimumFractionDigits = 0
    it.maximumFractionDigits = 0
} as DecimalFormat

fun Double?.format(decimalCount: Int): String {
    val format = NumberFormat.getNumberInstance(locale).also {
        it.isGroupingUsed = true
        it.minimumFractionDigits = decimalCount
    }
    return this
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

fun String?.parse(): Double? {
    return this
        ?.trim()
        ?.let { numberFormat.strictParse(this) }
        ?.toDouble()
}

private fun DecimalFormat.strictParse(text: String): Number? = runCatching {
    val parsePosition = ParsePosition(0)
    val result = numberFormat.parse(text, parsePosition)
    if (parsePosition.index == text.length) {
        result
    } else {
        null// not all of the text was consumed, there's some illegal character
    }
}.getOrNull()

operator fun Double?.times(other: Double?): Double? {
    return safelyCalculate(this, other) { dis, other ->
        dis * other
    }
}

operator fun Double?.div(other: Double?): Double? {
    return safelyCalculate(this, other) { dis, other ->
        dis / other
    }
}

private fun safelyCalculate(
    first: Double?,
    second: Double?,
    runnable: (Double, Double) -> Double
): Double? {
    return if (first != null && second != null) {
        runnable.invoke(first, second)
    } else {
        null
    }
}
