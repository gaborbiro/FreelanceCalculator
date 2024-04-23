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
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_ID_BASE
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.sections.currency.CurrencySection
import app.gaborbiro.freelancecalculator.ui.sections.daysperweek.DaysPerWeekSection
import app.gaborbiro.freelancecalculator.ui.sections.fee.FeeSection
import app.gaborbiro.freelancecalculator.ui.sections.tax.TaxAndNetIncomeSection
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.resolve
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
        title = "Currency conversion",
        store = store,
        currencyRepository = currencyRepository,
        onMoneyPerWeekChanged = { newMoneyPerWeek ->
            CoroutineScope(Dispatchers.IO).launch {
                flowOf(newMoneyPerWeek)
                    .zip(store.registry["time_off:${DATA_ID_FEE}"]) { newMoneyPerWeek, fee ->
                        val feeMultiplier = fee
                            .resolve()
                            ?.toDouble()
                            ?.let { 1.0 - (it / 100.0) }
                        newMoneyPerWeek to feeMultiplier
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
                        val feeMultiplier = fee
                            .resolve()
                            ?.toDouble()
                            ?.let { 1.0 - (it / 100.0) }
                        newMoneyPerWeek to feeMultiplier
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