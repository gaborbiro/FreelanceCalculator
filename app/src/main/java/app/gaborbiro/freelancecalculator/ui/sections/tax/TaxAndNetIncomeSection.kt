package app.gaborbiro.freelancecalculator.ui.sections.tax

import TaxCalculator_England_23_24
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.repo.tax.TaxCalculator
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.times

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FlowOperatorInvokedInComposition")
@Composable
fun ColumnScope.TaxAndNetIncomeSection(
    inputId: String,
    sectionId: String,
    store: Store,
    onMoneyPerWeekChanged: (ArithmeticChain?) -> Unit,
) {
    val selectedCurrency by store.currencies[inputId]
        .collectAsState(initial = null)
    val (fromCurrency, toCurrency) = selectedCurrency ?: (null to null)

    if (fromCurrency == null || toCurrency != "GBP") {
        return
    }

    val moneyPerWeek by store
        .registry["${inputId}:${Store.DATA_ID_MONEY_PER_WEEK}"]
        .collectAsState(initial = null)

    val taxCalculator: TaxCalculator = remember { TaxCalculator_England_23_24() }

    val taxInfo = remember(moneyPerWeek) {
        val perYear: Double? = (moneyPerWeek * WEEKS_PER_YEAR)
            ?.resolve()
            ?.toDouble()
        perYear?.let {
            val incomeTax = taxCalculator.calculateTax(it)
            val nic2Tax = taxCalculator.calculateNIC2(it)
            val nic4Tax = taxCalculator.calculateNIC4(it)
            val totalTax = incomeTax.totalTax + nic2Tax.totalTax + nic4Tax.totalTax

            if (incomeTax.breakdown.isNotEmpty() && nic4Tax.breakdown.isNotEmpty()) {
                val afterTaxPerWeek: ArithmeticChain? = (it - totalTax) / WEEKS_PER_YEAR

                TaxBreakdownUIModel(
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

    store.registry["${sectionId}:${Store.DATA_ID_MONEY_PER_WEEK}"] = taxInfo?.afterTaxPerWeek

    taxInfo?.let {
        MoneyOverTime(
            collapseId = "tax_net",
            title = "Tax (UK 23/24)",
            store = store,
            moneyPerWeek = taxInfo.afterTaxPerWeek,
            extraContent = {
                TaxBreakdownSection(
                    taxModel = taxInfo,
                    sectionExpander = store.sectionExpander,
                )
            }
        ) { newValue: ArithmeticChain? ->
            val perYear = newValue * WEEKS_PER_YEAR
            val newMoneyPerWeek = perYear?.resolve()?.let {
                taxCalculator.calculateIncomeFromBrut(it.toDouble())
            } / WEEKS_PER_YEAR
            onMoneyPerWeekChanged(newMoneyPerWeek)
        }
    }
}

@Preview
@Composable
private fun TaxBreakdownSectionPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = PADDING_LARGE)
    ) {
        TaxAndNetIncomeSection(
            inputId = "",
            sectionId = "",
            store = Store.dummyImplementation(),
            onMoneyPerWeekChanged = {},
        )
    }
}