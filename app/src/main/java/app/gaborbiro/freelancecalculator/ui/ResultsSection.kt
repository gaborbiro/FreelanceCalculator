package app.gaborbiro.freelancecalculator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.DATA_ID_FEE
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.DATA_ID_MONEY_PER_WEEK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.DATA_ID_TAX
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_ID_BASE
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.sections.currency.CurrencySection
import app.gaborbiro.freelancecalculator.ui.sections.daysperweek.DaysPerWeekSection
import app.gaborbiro.freelancecalculator.ui.sections.fee.FeeSection
import app.gaborbiro.freelancecalculator.ui.sections.fee.toFeeMultiplier
import app.gaborbiro.freelancecalculator.ui.sections.tax.TaxAndNetIncomeSection
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.Quad
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.zip
import kotlinx.coroutines.launch


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
        .registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"]
        .collectAsState(initial = null)

    MoneyOverTimeSection(
        moneyPerWeek = baseMoneyPerWeek.value,
        sectionId = "base_display",
        title = "Base",
        store = store,
        onMoneyPerWeekChange = { newMoneyPerWeek ->
            store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"] =
                newMoneyPerWeek
        },
    )

    FeeSection(
        inputId = SECTION_ID_BASE,
        sectionId = "time_off",
        title = "Fee / Reduction",
        store = store,
        onMoneyPerWeekChanged = {
            store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"] = it
        }
    )

    CurrencySection(
        inputId = "time_off",
        sectionId = "currency1",
        title = "Currency conversion 1",
        store = store,
        currencyRepository = currencyRepository,
        onMoneyPerWeekChanged = { newMoneyPerWeek ->
            CoroutineScope(Dispatchers.IO).launch {
                flowOf(newMoneyPerWeek)
                    .zip(store.registry["time_off:${DATA_ID_FEE}"]) { newMoneyPerWeek, fee ->
                        newMoneyPerWeek to fee.toFeeMultiplier()
                    }
                    .collectLatest { (newMoneyPerWeek, feeMultiplier) ->
                        store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"] =
                            newMoneyPerWeek / feeMultiplier
                    }
            }
        }
    )

    TaxAndNetIncomeSection(
        inputId = "currency1",
        sectionId = "tax",
        store = store,
        onMoneyPerWeekChanged = { newMoneyPerWeek ->
            CoroutineScope(Dispatchers.IO).launch {
                flowOf(newMoneyPerWeek)
                    .zip(store.registry["time_off:${DATA_ID_FEE}"]) { newMoneyPerWeek, fee ->
                        newMoneyPerWeek to fee.toFeeMultiplier()
                    }
                    .zip(store.exchangeRates["currency1"]) { (newMoneyPerWeek, feeMultiplier), rate ->
                        Triple(
                            newMoneyPerWeek,
                            feeMultiplier,
                            rate?.rate,
                        )
                    }
                    .collectLatest { (newMoneyPerWeek, feeMultiplier, rate) ->
                        store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"] =
                            newMoneyPerWeek / feeMultiplier / rate
                    }
            }
        }
    )

    CurrencySection(
        inputId = "tax",
        sectionId = "currency2",
        title = "Currency conversion 2",
        store = store,
        currencyRepository = currencyRepository,
        onMoneyPerWeekChanged = { newMoneyPerWeek ->
            CoroutineScope(Dispatchers.IO).launch {
                flowOf(newMoneyPerWeek * WEEKS_PER_YEAR)
                    .zip(store.registry["time_off:${DATA_ID_FEE}"]) { newMoneyPerYear, fee ->
                        newMoneyPerYear to fee.toFeeMultiplier()
                    }
                    .zip(store.exchangeRates["currency1"]) { (newMoneyPerYear, feeMultiplier), rate ->
                        Triple(
                            newMoneyPerYear,
                            feeMultiplier,
                            rate?.rate,
                        )
                    }
                    .zip(store.registry["tax:${DATA_ID_TAX}"]) { (newMoneyPerYear, feeMultiplier, rate), tax ->
                        Quad(newMoneyPerYear, feeMultiplier, rate, tax)
                    }
                    .collectLatest { (newMoneyPerYear, feeMultiplier, rate, tax) ->
                        val taxD = tax?.resolve()?.toDouble() ?: 0.0
                        val gross = (newMoneyPerYear?.resolve()?.toDouble() ?: 0.0) + taxD
                        store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"] =
                            if (gross > 0) {
                                gross.chainify() / WEEKS_PER_YEAR / feeMultiplier / rate
                            } else {
                                null
                            }
                    }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.MoneyOverTimeSection(
    moneyPerWeek: ArithmeticChain?,
    sectionId: String,
    title: String,
    store: Store,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    MoneyOverTime(
        collapseId = "${sectionId}:moneyovertime/",
        title = title,
        store = store,
        moneyPerWeek = moneyPerWeek,
        onMoneyPerWeekChange = onMoneyPerWeekChange
    )
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