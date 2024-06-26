package app.gaborbiro.freelancecalculator.repo.tax

import kotlin.math.min
import kotlin.math.roundToInt


abstract class TaxCalculator {

    abstract fun calculateTax(perYear: Double): TaxCalculationResult

    abstract fun calculateNIC2(perYear: Double): TaxCalculationResult

    abstract fun calculateNIC4(perYear: Double): TaxCalculationResult

    abstract fun calculateIncomeFromGross(brut: Double): Double

    /**
     * @param brackets Pairs of bracket and the percent of tax paid within (under) that bracket.
     * Brackets are not cumulative. So a 12,500 pers. all. with 37,500/150,000 brackets would be
     * [12,500 -> 0.0, 37,500->0.2, 112,500 -> 0.4, Double.MAX_VALUE -> 0.45]
     */
    fun calculateTax(
        perYear: Double,
        brackets: List<Bracket>
    ): TaxCalculationResult {
        val breakdown: MutableList<BracketResult> = mutableListOf()
        var remaining: Double = perYear
        var totalTax = 0.0

        brackets.forEach { bracket ->
            val (amount, percentage) = bracket
            if (remaining > 0) {
                val taxable = min(remaining, amount.toDouble())
                totalTax += taxable * percentage
                breakdown.add(BracketResult(bracket, (taxable * percentage)))
                remaining -= taxable
            }
        }

        return TaxCalculationResult(totalTax, breakdown)
    }
}

data class Bracket(
    val amount: Int,
    val percentage: Double
) {
    override fun toString(): String {
        return "$amount - ${(percentage * 100.0).roundToInt()}%"
    }
}

data class TaxCalculationResult(
    val totalTax: Double,
    val breakdown: List<BracketResult>
)

data class BracketResult(
    val bracket: Bracket,
    val taxOfBracket: Double,
)