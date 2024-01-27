package app.gaborbiro.freelancecalculator.util.hide

import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.div
import java.math.BigDecimal
import java.text.DecimalFormat
import java.text.NumberFormat
import java.text.ParsePosition
import java.util.Locale


val DAYS_PER_YEAR = 365.25

val WEEKS_PER_YEAR = ArithmeticChain(DAYS_PER_YEAR) / 7.0 // 52.18

val WEEKS_PER_MONTH = ArithmeticChain(DAYS_PER_YEAR) / 7.0 / 12.0 // 4.35

private val locale = Locale.getDefault()
private val parsingFormat = (NumberFormat.getNumberInstance(locale) as DecimalFormat).also {
    it.isParseBigDecimal = false
}

fun BigDecimal?.format(decimalCount: Int): String {
    val format = NumberFormat.getNumberInstance(locale).also {
        it.isGroupingUsed = true
        it.minimumFractionDigits = decimalCount
        it.maximumFractionDigits = decimalCount
    }
    return this
        ?.let { format.format(it) }
        ?: ""
}

fun Double?.format(decimalCount: Int): String {
    val format = NumberFormat.getNumberInstance(locale).also {
        it.isGroupingUsed = true
        it.minimumFractionDigits = decimalCount
        it.maximumFractionDigits = decimalCount
    }
    return this
        ?.let { format.format(it) }
        ?: ""
}

fun Int.format(): String {
    val format = NumberFormat.getNumberInstance(locale).also {
        it.isGroupingUsed = true
        it.minimumFractionDigits = 0
        it.maximumFractionDigits = 0
    }
    return this.let { format.format(it) }
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

fun String?.strictParse(): Double? {
    return this
        ?.trim()
        ?.let { parsingFormat.strictParse(this) }
}

private fun DecimalFormat.strictParse(text: String): Double? = runCatching {
    val parsePosition = ParsePosition(0)
    val result = parsingFormat.parse(text, parsePosition)
    if (parsePosition.index != text.length) {
        null// not all of the text was consumed, there's some illegal character
    } else {
        result.toDouble()
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

fun <T, R> safelyCalculate1(
    first: T?,
    second: R?,
    runnable: (T, R) -> T
): T? {
    return if (first != null && second != null) {
        runnable.invoke(first, second)
    } else {
        null
    }
}

fun <T, R> safelyCalculate2(
    first: T?,
    second: R?,
    runnable: (T, R) -> R
): R? {
    return if (first != null && second != null) {
        runnable.invoke(first, second)
    } else {
        null
    }
}