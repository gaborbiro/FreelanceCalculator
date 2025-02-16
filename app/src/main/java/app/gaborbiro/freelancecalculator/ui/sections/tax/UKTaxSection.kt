package app.gaborbiro.freelancecalculator.ui.sections.tax

import TaxCalculator_England_23_24
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.MONEY_PER_WEEK
import app.gaborbiro.freelancecalculator.repo.tax.TaxCalculator
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.MoneyBreakdown
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FlowOperatorInvokedInComposition")
@Composable
fun ColumnScope.UKTaxSection(
    inputId: String,
    sectionId: String,
    store: Store,
    onPerWeekValueChanged: (ArithmeticChain?) -> Unit,
) {
    val taxCalculator: TaxCalculator = remember { TaxCalculator_England_23_24() }

    val taxInfo: TaxBreakdownUIModel? = remember(inputId, sectionId) {
        store.registry["$inputId:$MONEY_PER_WEEK"].map {
            val perYear: Double? = (it * WEEKS_PER_YEAR)
                ?.resolve()
                ?.toDouble()
            perYear?.let {
                val incomeTax = taxCalculator.calculateTax(it)
                val nic2Tax = taxCalculator.calculateNIC2(it)
                val nic4Tax = taxCalculator.calculateNIC4(it)
                val totalTax = incomeTax.totalTax + nic2Tax.totalTax + nic4Tax.totalTax

                if (totalTax > 0) {
                    store.registry[sectionId] = totalTax.chainify()
                }

                if (incomeTax.breakdown.isNotEmpty() && nic4Tax.breakdown.isNotEmpty()) {
                    val afterTaxPerWeek: ArithmeticChain? = (it - totalTax) / WEEKS_PER_YEAR

                    store.registry["$sectionId:$MONEY_PER_WEEK"] = afterTaxPerWeek

                    TaxBreakdownUIModel(
                        incomeTax = "${incomeTax.totalTax.format(decimalCount = 2)} (allowance: ${incomeTax.breakdown[0].bracket.amount.format()})",
                        nic2Tax = nic2Tax.totalTax.format(decimalCount = 2),
                        nic4Tax = "${nic4Tax.totalTax.format(decimalCount = 2)} (allowance: ${nic4Tax.breakdown[0].bracket.amount.format()})",
                        totalTax = totalTax.format(decimalCount = 2),
                        afterTaxPerWeek = afterTaxPerWeek!!,
                        total = totalTax,
                    )
                } else {
                    null
                }
            }
        }
    }.collectAsState(initial = null).value

    taxInfo?.let {
        MoneyBreakdown(
            collapseId = "$sectionId/net:collapse",
            title = "After tax (UK 23/24, $inputId->$sectionId)",
            store = store,
            moneyPerWeek = it.afterTaxPerWeek,
            extraContent = {
                TaxBreakdownSection(
                    collapseId = "$sectionId/tax:collapse",
                    taxModel = it,
                    sectionExpander = store.sectionExpander,
                )
            },
            onPerWeekValueChanged = { newValue: ArithmeticChain? ->
                val perYear = newValue * WEEKS_PER_YEAR
                val gross = perYear?.resolve()?.let {
                    taxCalculator.calculateIncomeFromGross(it.toDouble())
                } / WEEKS_PER_YEAR
                onPerWeekValueChanged(gross)
            }
        )
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
        UKTaxSection(
            inputId = "",
            sectionId = "",
            store = Store.dummyImplementation(),
            onPerWeekValueChanged = {},
        )
    }
}