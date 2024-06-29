package app.gaborbiro.freelancecalculator.util

import androidx.annotation.Keep
import app.gaborbiro.freelancecalculator.util.hide.safelyCalculate1
import app.gaborbiro.freelancecalculator.util.hide.safelyCalculate2
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Encapsulates a chain of multiplication/division operations. The [resolve] call will first
 * check for any operands that cancel out and remove them, before calculating the result.
 *
 * Using * or / signs is supported (with doubles or other [ArithmeticChain]s), but parentheses have no effect.
 * [ArithmeticChain]s are simply concatenated.
 */
@Keep
class ArithmeticChain private constructor(private val operands: List<Operand>) {

    constructor(number: Double) : this(listOf(Operand(number, inverse = false)))

    operator fun times(other: Double): ArithmeticChain {
        return times(Operand(other, inverse = false))
    }

    operator fun times(chain: ArithmeticChain): ArithmeticChain {
        var result = ArithmeticChain(operands)
        chain.operands.forEach {
            result = result.times(it)
        }
        return result
    }

    operator fun div(other: Double): ArithmeticChain {
        return times(Operand(other, inverse = true))
    }

    operator fun div(chain: ArithmeticChain): ArithmeticChain {
        var result = ArithmeticChain(operands)
        chain.operands.forEach {
            result = result.times(it.inverse())
        }
        return result
    }

    private fun times(operand: Operand): ArithmeticChain {
        val inverse = operand.inverse()
        val index = operands.indexOf(inverse)
        return if (index >= 0) {
            ArithmeticChain(
                operands = operands.minusElement(inverse)
            )
        } else {
            ArithmeticChain(
                operands = operands + operand
            )
        }
    }

    fun resolve(): BigDecimal {
        var result = BigDecimal.ONE
        operands.forEach {
            result = if (it.inverse) {
                result
                    .setScale(result.scale().coerceAtLeast(4))
                    .divide(BigDecimal(it.value), RoundingMode.HALF_UP)
            } else {
                result.multiply(BigDecimal(it.value))
            }
        }
        return result
    }

    fun simplify(): ArithmeticChain {
        return resolve().toDouble().chainify()
    }

    override fun toString(): String {
        return operands.joinToString(" * ")
    }

    private data class Operand(val value: Double, val inverse: Boolean) {

        init {
            if (value == 0.0) throw IllegalArgumentException("Operand can never be 0 (inverse: $inverse)")
        }

        fun inverse(): Operand {
            return this.copy(inverse = inverse.not())
        }

        override fun toString(): String {
            return if (inverse) {
                "1/${value}"
            } else {
                value.toString()
            }
        }
    }
}

fun Double.chainify() = this.let(::ArithmeticChain)

fun ArithmeticChain?.simplify(): ArithmeticChain? {
    return this?.simplify()
}

fun ArithmeticChain?.resolve(): BigDecimal? {
    return this?.resolve()
}

operator fun ArithmeticChain?.times(other: ArithmeticChain?): ArithmeticChain? {
    return safelyCalculate1(this, other) { dis: ArithmeticChain, otha: ArithmeticChain ->
        dis * otha
    }
}

operator fun ArithmeticChain?.times(other: Double?): ArithmeticChain? {
    return safelyCalculate1(this, other) { dis: ArithmeticChain, otha: Double ->
        dis * otha
    }
}

operator fun Double?.times(other: ArithmeticChain?): ArithmeticChain? {
    return safelyCalculate2(this, other) { dis: Double, otha: ArithmeticChain ->
        ArithmeticChain(dis) * otha
    }
}

operator fun ArithmeticChain?.div(other: ArithmeticChain?): ArithmeticChain? {
    return safelyCalculate1(this, other) { dis: ArithmeticChain, otha: ArithmeticChain ->
        dis / otha
    }
}

operator fun ArithmeticChain?.div(other: Double?): ArithmeticChain? {
    return safelyCalculate1(this, other) { dis: ArithmeticChain, otha: Double ->
        dis / otha
    }
}

operator fun Double?.div(other: ArithmeticChain?): ArithmeticChain? {
    return safelyCalculate2(this, other) { dis: Double, otha: ArithmeticChain ->
        ArithmeticChain(dis) / otha
    }
}
