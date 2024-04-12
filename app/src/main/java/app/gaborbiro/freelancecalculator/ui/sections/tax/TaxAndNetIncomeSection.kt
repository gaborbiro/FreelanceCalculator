package app.gaborbiro.freelancecalculator.ui.sections.tax

import Tax_England_23_24
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.repo.tax.TaxCalculator
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.Lce
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.times

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.TaxAndNetIncomeSection(
    store: Store,
    currencyRepository: CurrencyRepository,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    val fromCurrency by store.fromCurrency.collectAsState(initial = null)
    val toCurrency by store.toCurrency.collectAsState(initial = null)

    if (fromCurrency == null || toCurrency != "GBP") {
        return
    }

    val taxCalculator: TaxCalculator = remember { Tax_England_23_24() }

    val fee: State<Double?> = store.fee.collectAsState(initial = null)
    val feeMultiplier = fee.value?.let { 1.0 - (it / 100.0) }.chainify()

    var exchangeRate: Double? by remember { mutableStateOf(null) }

    val newRate = remember(fromCurrency, toCurrency) {
        currencyRepository.getRate(
            fromCurrency!!,
            toCurrency!!
        )
    }
        .subscribeAsState(Lce.Loading).value

    when (newRate) {
        is Lce.Loading -> {

        }

        is Lce.Data -> {
            exchangeRate = newRate.data.rate
        }

        is Lce.Error -> {
            // ResultsSection.kt already handles the error, no need to do it again
        }
    }

    val feeAndExchangeRateMultiplier = feeMultiplier * exchangeRate

    val feePerHour = store.feePerHour.collectAsState(initial = null)
    val hoursPerWeek = store.hoursPerWeek.collectAsState(initial = null)

    val taxInfo = remember(feePerHour.value, hoursPerWeek.value, feeAndExchangeRateMultiplier) {
        val perYear: Double? =
            (feePerHour.value * hoursPerWeek.value * feeAndExchangeRateMultiplier * WEEKS_PER_YEAR)?.resolve()
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

    taxInfo?.let {
        MoneyOverTime(
            sectionId = "net",
            title = "Tax (UK 23/24)",
            store = store,
            moneyPerWeek = taxInfo.afterTaxPerWeek,
            extraContent = {
                TaxBreakdownSection(
                    taxModel = taxInfo,
                    sectionExpander = store.sectionExpander(),
                )
            }
        ) { newValue: ArithmeticChain? ->
            val perYear = newValue * WEEKS_PER_YEAR
            onMoneyPerWeekChange(
                perYear?.resolve()?.let {
                    taxCalculator.calculateIncomeFromBrut(it.toDouble())
                } / feeAndExchangeRateMultiplier / WEEKS_PER_YEAR
            )
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
            store = Store.dummyImplementation(),
            currencyRepository = CurrencyRepository.dummyImplementation(),
            onMoneyPerWeekChange = { },
        )
    }
}