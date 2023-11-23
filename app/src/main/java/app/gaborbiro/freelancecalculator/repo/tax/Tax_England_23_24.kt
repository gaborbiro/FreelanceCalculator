import app.gaborbiro.freelancecalculator.repo.tax.Bracket
import app.gaborbiro.freelancecalculator.repo.tax.TaxCalculationResult
import app.gaborbiro.freelancecalculator.repo.tax.TaxCalculator

private const val PERSONAL_ALLOWANCE = 12570

private const val INCOME_BASIC_BRACKET = 37700
private const val INCOME_BASIC_TAX = .2
private const val INCOME_HIGH_BRACKET = 125140
private const val INCOME_HIGH_TAX = .4
private const val INCOME_ADDITIONAL_BRACKET = Int.MAX_VALUE
private const val INCOME_ADDITIONAL_TAX = .45

private const val NIC2_THRESHOLD = 6725.0
private const val NIC2_TAX = 179.4

private const val NIC_4_PERSONAL_ALLOWANCE = 12570

private const val NIC4_BASIC_BRACKET = 37700
private const val NIC4_BASIC_TAX = 0.09

private const val NIC4_HIGH_BRACKET = Int.MAX_VALUE
private const val NIC4_HIGH_TAX = 0.02

class Tax_England_23_24 : TaxCalculator() {

    fun calculateTax(perYear: Double): TaxCalculationResult {
        return super.calculateIncomeTax(
            perYear,
            listOf(
                Bracket(personalAllowance(perYear).toInt(), 0.0),
                Bracket(INCOME_BASIC_BRACKET, INCOME_BASIC_TAX),
                Bracket(INCOME_HIGH_BRACKET, INCOME_HIGH_TAX),
                Bracket(INCOME_ADDITIONAL_BRACKET, INCOME_ADDITIONAL_TAX)
            )
        )
    }

    private fun personalAllowance(perYear: Double): Double {
        val reduction = (perYear - 100000).coerceAtLeast(0.0) / 2
        return (PERSONAL_ALLOWANCE - reduction).coerceAtLeast(0.0)
    }

    fun calculateNIC2(perYear: Double): TaxCalculationResult {
        return if (perYear > NIC2_THRESHOLD) {
            TaxCalculationResult(totalTax = NIC2_TAX, breakdown = emptyList())
        } else {
            TaxCalculationResult(totalTax = 0.0, breakdown = emptyList())
        }
    }

    fun calculateNIC4(perYear: Double): TaxCalculationResult {
        return super.calculateIncomeTax(
            perYear,
            listOf(
                Bracket(NIC_4_PERSONAL_ALLOWANCE, 0.0),
                Bracket(NIC4_BASIC_BRACKET, NIC4_BASIC_TAX),
                Bracket(NIC4_HIGH_BRACKET, NIC4_HIGH_TAX),
            )
        )
    }
}
