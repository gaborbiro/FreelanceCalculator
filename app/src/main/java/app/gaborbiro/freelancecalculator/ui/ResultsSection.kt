package app.gaborbiro.freelancecalculator.ui

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
import app.gaborbiro.freelancecalculator.ui.view.MoneyBreakdown
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.safelyCalculate2
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.merge


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.ResultsSection(
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

    val baseOutput = MoneyBreakdown(
        collapseId = "$SECTION_BASE:collapse",
        title = "Base",
        store = store,
        moneyPerWeek = baseMoneyPerWeek.value,
    )

    val timeOffOutput = feeSection(
        inputId = SECTION_BASE,
        sectionId = SECTION_TIMEOFF,
        title = "Time off",
        store = store,
    )

    val currency1Output = currencySection(
        inputId = SECTION_TIMEOFF,
        sectionId = SECTION_CURRENCY1,
        title = "Currency",
        store = store,
        currencyRepository = currencyRepository,
    )

    val selectedCurrency by store.currencies[SECTION_CURRENCY1]
        .collectAsState(initial = null)
    val (fromCurrency, toCurrency) = selectedCurrency ?: (null to null)

    val ptTaxOutput = if (fromCurrency != null && toCurrency == "EUR") {
        feeSection(
            inputId = SECTION_CURRENCY1,
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
            inputId = SECTION_CURRENCY1,
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

    LaunchedEffect(key1 = Unit) {
        val reverseMoneyPerWeek = merge(
            baseOutput,
            timeOffOutput,
            currency1Output
                .combine(
                    store.registry["$SECTION_TIMEOFF:$TYPE_FEE"]
                ) { newMoneyPerWeek, timeOff ->
                    newMoneyPerWeek / timeOff.toFeeMultiplier()
                },
            ptTaxOutput
                .combine(store.registry["$SECTION_TIMEOFF:$TYPE_FEE"]) { n, timeOff ->
                    n / timeOff.toFeeMultiplier()
                }
                .combine(store.exchangeRates[SECTION_CURRENCY1]) { n, currency1 ->
                    n / currency1?.rate
                },
            ptTaxCurrencyOutput
                .combine(store.registry["$SECTION_TIMEOFF:$TYPE_FEE"]) { n, timeOff ->
                    n / timeOff.toFeeMultiplier()
                }
                .combine(store.exchangeRates[SECTION_CURRENCY1]) { n, currency1 ->
                    n / currency1?.rate
                }
                .combine(store.registry["$SECTION_PT/$SUB_SECTION_TAX:$TYPE_FEE"]) { n, ptTaxRate ->
                    n / ptTaxRate.toFeeMultiplier()
                },
            ukTaxOutput
                .combine(store.registry["$SECTION_TIMEOFF:$TYPE_FEE"]) { n, timeOff ->
                    n / timeOff.toFeeMultiplier()
                }
                .combine(store.exchangeRates[SECTION_CURRENCY1]) { n, currency1 ->
                    n / currency1?.rate
                },
            ukTaxCurrencyOutput
                .combine(store.registry["$SECTION_TIMEOFF:$TYPE_FEE"]) { n, timeOff ->
                    n / timeOff.toFeeMultiplier()
                }
                .combine(store.exchangeRates[SECTION_CURRENCY1]) { n, currency1 ->
                    n / currency1?.rate
                }
                .combine(store.registry["$SECTION_UK/$SUB_SECTION_TAX"]) { n, ukTax ->
                    safelyCalculate2(n, ukTax) { n, ukTax ->
                        val newMoneyPerYear = (n * WEEKS_PER_YEAR).resolve().toDouble()
                        val taxD = ukTax.resolve().toDouble()
                        ((newMoneyPerYear + taxD) / WEEKS_PER_YEAR)!!
                    }
                }
        )

        store.registry.put(
            "$SECTION_BASE:$MONEY_PER_WEEK",
            reverseMoneyPerWeek
                .distinctUntilChanged()
        )
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
        ResultsSection(
            store = Store.dummyImplementation(),
            currencyRepository = CurrencyRepository.dummyImplementation(),
        )
    }
}