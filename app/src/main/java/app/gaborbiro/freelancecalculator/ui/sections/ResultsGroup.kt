package app.gaborbiro.freelancecalculator.ui.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.MONEY_PER_WEEK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_BASE
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_CURRENCY1
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_PT
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_TIMEOFF
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_UK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SUB_SECTION_CURRENCY
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SUB_SECTION_TAX
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.TYPE_FEE
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.sections.currency.currencySection
import app.gaborbiro.freelancecalculator.ui.sections.daysperweek.DaysPerWeekSection
import app.gaborbiro.freelancecalculator.ui.sections.fee.feeSection
import app.gaborbiro.freelancecalculator.ui.sections.fee.toFeeMultiplier
import app.gaborbiro.freelancecalculator.ui.sections.tax.ukTaxSection
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.moneyBreakdown
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.safelyCalculate2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.ResultsGroup(
    store: Store,
    currencyRepository: CurrencyRepository,
) {
    DaysPerWeekSection(
        modifier = Modifier
            .wrapContentSize()
            .align(Alignment.End),
        store = store,
    )

    val baseMoneyPerWeek = store
        .registry["${SECTION_BASE}:${MONEY_PER_WEEK}"]
        .collectAsState(initial = null)

    val baseOutput = moneyBreakdown(
        collapseId = "$SECTION_BASE:collapse",
        title = "Base",
        store = store,
        moneyPerWeek = baseMoneyPerWeek.value,
    )

    val currency1_Output = currencySection(
        inputId = SECTION_BASE,
        sectionId = SECTION_CURRENCY1,
        title = "Currency",
        store = store,
        currencyRepository = currencyRepository,
    )

    val timeOffOutput = feeSection(
        inputId = SECTION_CURRENCY1,
        sectionId = SECTION_TIMEOFF,
        title = "Time off",
        store = store,
    )

    val selectedCurrency by store.currencySelections[SECTION_CURRENCY1]
        .collectAsState(initial = null)
    val (fromCurrency, toCurrency) = selectedCurrency ?: (null to null)

    val ptTaxOutput = if (fromCurrency != null && toCurrency == "EUR") {
        feeSection(
            inputId = SECTION_TIMEOFF,
            sectionId = "$SECTION_PT/$SUB_SECTION_TAX",
            title = "After tax (PT '24)",
            store = store,
        )
    } else {
        emptyFlow()
    }

    val ptTaxCurrencyOutput = if (fromCurrency != null && toCurrency == "EUR") {
        currencySection(
            inputId = "$SECTION_PT/$SUB_SECTION_TAX",
            sectionId = "$SECTION_PT/$SUB_SECTION_CURRENCY",
            title = "Currency",
            store = store,
            currencyRepository = currencyRepository,
        )
    } else {
        emptyFlow()
    }

    val ukTaxOutput = if (fromCurrency != null && toCurrency == "GBP") {
        ukTaxSection(
            inputId = SECTION_TIMEOFF,
            sectionId = "$SECTION_UK/$SUB_SECTION_TAX",
            store = store,
        )
    } else {
        emptyFlow()
    }

    val ukTaxCurrencyOutput = if (fromCurrency != null && toCurrency == "GBP") {
        currencySection(
            inputId = "$SECTION_UK/$SUB_SECTION_TAX",
            sectionId = "$SECTION_UK/$SUB_SECTION_CURRENCY",
            title = "Currency",
            store = store,
            currencyRepository = currencyRepository,
        )
    } else {
        emptyFlow()
    }

    LaunchedEffect(Unit) {
        val reverseMoneyPerWeek: Flow<ArithmeticChain?> = merge(
            baseOutput,
            timeOffOutput,
            combine(
                currency1_Output,
                store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
            ) { newMoneyPerWeek, timeOff ->
                newMoneyPerWeek / timeOff.toFeeMultiplier()
            },
            combine(
                ptTaxOutput,
                store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                store.exchangeRates[SECTION_CURRENCY1],
            ) { ptTax, timeOff, currency1 ->
                ptTax / timeOff.toFeeMultiplier() / currency1?.rate
            },
            combine(
                ptTaxCurrencyOutput,
                store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                store.exchangeRates[SECTION_CURRENCY1],
                store.registry["$SECTION_PT/$SUB_SECTION_TAX:$TYPE_FEE"],
            ) { ptTaxCurrency, timeOff, currency1, ptTaxRate ->
                ptTaxCurrency / timeOff.toFeeMultiplier() / currency1?.rate / ptTaxCurrency.toFeeMultiplier()
            },
            combine(
                ukTaxOutput,
                store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                store.exchangeRates[SECTION_CURRENCY1],
            ) { ukTaxOutput, timeOff, currency1 ->
                ukTaxOutput / timeOff.toFeeMultiplier() / currency1?.rate
            },
            combine(
                ukTaxCurrencyOutput,
                store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                store.exchangeRates[SECTION_CURRENCY1],
                store.registry["$SECTION_UK/$SUB_SECTION_TAX"],
            ) { ukTaxCurrencyOutput, timeOff, currency1, ukTax ->
                val n = ukTaxCurrencyOutput / timeOff.toFeeMultiplier() / currency1?.rate
                safelyCalculate2(n, ukTax) { n, ukTax ->
                    val newMoneyPerYear = (n * WEEKS_PER_YEAR).resolve().toDouble()
                    val taxD = ukTax.resolve().toDouble()
                    ((newMoneyPerYear + taxD) / WEEKS_PER_YEAR)!!
                }
            }
        )

        store.registry["$SECTION_BASE:$MONEY_PER_WEEK"] = reverseMoneyPerWeek.distinctUntilChanged()
    }
}

@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Preview
@Composable
private fun ResultsContentPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PADDING_LARGE)
    ) {
        ResultsGroup(
            store = Store.dummyImplementation(),
            currencyRepository = CurrencyRepository.dummyImplementation(),
        )
    }
}