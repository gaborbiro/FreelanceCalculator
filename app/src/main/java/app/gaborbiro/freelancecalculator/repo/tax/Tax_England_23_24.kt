import app.gaborbiro.freelancecalculator.repo.tax.Bracket
import app.gaborbiro.freelancecalculator.repo.tax.TaxCalculationResult
import app.gaborbiro.freelancecalculator.repo.tax.TaxCalculator

private const val PERSONAL_ALLOWANCE = 12570

private const val INCOME_BASIC_BRACKET = 37700
private const val INCOME_BASIC_TAX = .2
private const val INCOME_HIGH_BRACKET = 87440
private const val INCOME_HIGH_TAX = .4
private const val INCOME_ADDITIONAL_BRACKET = Int.MAX_VALUE
private const val INCOME_ADDITIONAL_TAX = .45

private const val PA_LIMIT = 100000

private const val NIC2_THRESHOLD = 6725.0
private const val NIC2_TAX = 179.4

private const val NIC_4_PERSONAL_ALLOWANCE = 12570

private const val NIC4_BASIC_BRACKET = 37700
private const val NIC4_BASIC_TAX = 0.09

private const val NIC4_HIGH_BRACKET = Int.MAX_VALUE
private const val NIC4_HIGH_TAX = 0.02

class Tax_England_23_24 : TaxCalculator() {

    override fun calculateTax(perYear: Double): TaxCalculationResult {
        return super.calculateTax(
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
        val reduction = (perYear - PA_LIMIT).coerceAtLeast(0.0) / 2
        return (PERSONAL_ALLOWANCE - reduction).coerceAtLeast(0.0)
    }

    override fun calculateNIC2(perYear: Double): TaxCalculationResult {
        return if (perYear > NIC2_THRESHOLD && perYear > personalAllowance(perYear)) { // assuming 0 business expenses
            TaxCalculationResult(totalTax = NIC2_TAX, breakdown = emptyList())
        } else {
            TaxCalculationResult(totalTax = 0.0, breakdown = emptyList())
        }
    }

    override fun calculateNIC4(perYear: Double): TaxCalculationResult {
        return super.calculateTax(
            perYear,
            listOf(
                Bracket(NIC_4_PERSONAL_ALLOWANCE, 0.0),
                Bracket(NIC4_BASIC_BRACKET, NIC4_BASIC_TAX),
                Bracket(NIC4_HIGH_BRACKET, NIC4_HIGH_TAX),
            )
        )
    }

    override fun calculateIncomeFromBrut(brut: Double): Double {
        return when {
            brut <= PERSONAL_ALLOWANCE -> brut
            brut <= PERSONAL_ALLOWANCE + INCOME_BASIC_BRACKET -> 1.40845 * brut - 4881.55
            brut <= PA_LIMIT -> 1.72414 * brut - 17243.1
            brut <= INCOME_BASIC_BRACKET + INCOME_HIGH_BRACKET -> 2.63158 * brut - 78950
            else -> 1.88679 * brut - 21188.7
        }
    }
}
