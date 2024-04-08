package app.gaborbiro.freelancecalculator.ui

import Tax_England_23_24
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.repo.tax.TaxCalculator
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.times

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.TaxAndNetIncomeSection(
    store: Store,
    moneyPerWeek: ArithmeticChain?,
    daysPerWeek: Double?,
    feeAndExchangeRateMultiplier: ArithmeticChain?,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {

    val taxCalculator: TaxCalculator = remember { Tax_England_23_24() }

    val taxInfo = remember(moneyPerWeek, feeAndExchangeRateMultiplier) {
        val perYear: Double? =
            (moneyPerWeek * feeAndExchangeRateMultiplier * WEEKS_PER_YEAR)?.resolve()
                ?.toDouble()
        perYear?.let {
            val incomeTax = taxCalculator.calculateTax(it)
            val nic2Tax = taxCalculator.calculateNIC2(it)
            val nic4Tax = taxCalculator.calculateNIC4(it)
            val totalTax = incomeTax.totalTax + nic2Tax.totalTax + nic4Tax.totalTax

            if (incomeTax.breakdown.isNotEmpty() && nic4Tax.breakdown.isNotEmpty()) {
                val afterTaxPerWeek: ArithmeticChain? = (it - totalTax) / WEEKS_PER_YEAR

                TaxCalculatorUIModel(
                    incomeTax = "${incomeTax.totalTax.format(decimalCount = 2)} (allowance: ${incomeTax.breakdown[0].bracket.amount.format()})",
                    nic2Tax = nic2Tax.totalTax.format(decimalCount = 2),
                    nic4Tax = "${nic4Tax.totalTax.format(decimalCount = 2)} (allowance: ${nic4Tax.breakdown[0].bracket.amount.format()})",
                    totalTax = totalTax.format(decimalCount = 2),
                    afterTaxPerWeek = afterTaxPerWeek!!
                )
            } else {
                null
            }
        }
    }

    taxInfo?.let {
        TaxCalculatorSection(taxModel = taxInfo, store = store)
    }
    MoneyOverTime(
        sectionId = "TAX",
        title = "Net Income",
        store = store,
        moneyPerWeek = taxInfo?.afterTaxPerWeek,
        daysPerWeek = daysPerWeek,
    ) { newValue: ArithmeticChain? ->
        val perYear = newValue * WEEKS_PER_YEAR
        onMoneyPerWeekChange(
            perYear?.resolve()?.let {
                taxCalculator.calculateIncomeFromBrut(it.toDouble())
            } / feeAndExchangeRateMultiplier / WEEKS_PER_YEAR
        )
    }
}

@Preview
@Composable
private fun TaxSectionPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = PADDING_LARGE)
    ) {
        TaxAndNetIncomeSection(
            store = Store.dummyImplementation(),
            moneyPerWeek = 30.0.chainify(),
            daysPerWeek = 5.0,
            feeAndExchangeRateMultiplier = 8.7.chainify(),
            onMoneyPerWeekChange = { },
        )
    }
}