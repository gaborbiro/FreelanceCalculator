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
import app.gaborbiro.freelancecalculator.ui.sections.currency.CurrencySection
import app.gaborbiro.freelancecalculator.ui.sections.daysperweek.DaysPerWeekSection
import app.gaborbiro.freelancecalculator.ui.sections.fee.FeeSection
import app.gaborbiro.freelancecalculator.ui.sections.fee.toFeeMultiplier
import app.gaborbiro.freelancecalculator.ui.sections.tax.PTTaxSection
import app.gaborbiro.freelancecalculator.ui.sections.tax.UKTaxSection
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.times
import app.gaborbiro.freelancecalculator.util.zip
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
        .registry["${SECTION_BASE}:${MONEY_PER_WEEK}"]
        .collectAsState(initial = null)

    MoneyOverTimeSection(
        moneyPerWeek = baseMoneyPerWeek.value,
        sectionId = "base_display",
        title = "Base",
        store = store,
        onMoneyPerWeekChange = { newMoneyPerWeek ->
            store.registry["${SECTION_BASE}:${MONEY_PER_WEEK}"] =
                newMoneyPerWeek
        },
    )

    FeeSection(
        inputId = SECTION_BASE,
        sectionId = SECTION_TIMEOFF,
        title = "Fee / Reduction",
        store = store,
        onMoneyPerWeekChanged = {
            store.registry["${SECTION_BASE}:${MONEY_PER_WEEK}"] = it
        }
    )

    CurrencySection(
        inputId = SECTION_TIMEOFF,
        sectionId = SECTION_CURRENCY1,
        title = "Currency",
        store = store,
        currencyRepository = currencyRepository,
        onMoneyPerWeekChanged = { newMoneyPerWeek ->
            CoroutineScope(Dispatchers.IO).launch {
                flowOf(newMoneyPerWeek)
                    .zip(store.registry["$SECTION_TIMEOFF:$TYPE_FEE"]) { newMoneyPerWeek, timeOff ->
                        newMoneyPerWeek to timeOff.toFeeMultiplier()
                    }
                    .collectLatest { (newMoneyPerWeek, timeOff) ->
                        store.registry["${SECTION_BASE}:${MONEY_PER_WEEK}"] =
                            newMoneyPerWeek / timeOff
                    }
            }
        }
    )

    PTTaxSection(
        inputId = SECTION_CURRENCY1,
        sectionId = SECTION_PT,
        store = store,
        currencyRepository = currencyRepository,
        onMoneyPerWeekChangedTax = { newMoneyPerWeek ->
            CoroutineScope(Dispatchers.IO).launch {
                zip(
                    flowOf(newMoneyPerWeek),
                    store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                    store.exchangeRates[SECTION_CURRENCY1],
                ).collectLatest { (newMoneyPerWeek, timeOff, currency1) ->
                    store.registry["${SECTION_BASE}:${MONEY_PER_WEEK}"] =
                        newMoneyPerWeek / timeOff?.toFeeMultiplier() / currency1?.rate
                }
            }
        },
        onMoneyPerWeekChangedCurrency = { newMoneyPerWeek ->
            CoroutineScope(Dispatchers.IO).launch {
                zip(
                    flowOf(newMoneyPerWeek),
                    store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                    store.exchangeRates[SECTION_CURRENCY1],
                    store.registry["$SECTION_PT/$SUB_SECTION_TAX:$TYPE_FEE"],
                ).collectLatest { (newMoneyPerWeek, timeOff, currency1, ptTaxRate) ->
                    store.registry["$SECTION_BASE:$MONEY_PER_WEEK"] =
                        newMoneyPerWeek / ptTaxRate?.toFeeMultiplier() / currency1?.rate / timeOff?.toFeeMultiplier()
                }
            }
        }
    )

    UKTaxSection(
        inputId = SECTION_CURRENCY1,
        sectionId = SECTION_UK,
        store = store,
        currencyRepository = currencyRepository,
        onMoneyPerWeekChangedTax = { newMoneyPerWeek ->
            CoroutineScope(Dispatchers.IO).launch {
                zip(
                    flowOf(newMoneyPerWeek),
                    store.registry["$SECTION_TIMEOFF:${TYPE_FEE}"],
                    store.exchangeRates[SECTION_CURRENCY1],
                ).collectLatest { (newMoneyPerWeek, timeOff, currency1) ->
                    store.registry["${SECTION_BASE}:${MONEY_PER_WEEK}"] =
                        newMoneyPerWeek / timeOff?.toFeeMultiplier() / currency1?.rate
                }
            }
        },
        onMoneyPerWeekChangedCurrency = { newMoneyPerWeek ->
            CoroutineScope(Dispatchers.IO).launch {
                zip(
                    flowOf(newMoneyPerWeek * WEEKS_PER_YEAR),
                    store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                    store.exchangeRates[SECTION_CURRENCY1],
                    store.registry["$SECTION_UK/$SUB_SECTION_TAX"],
                ).collectLatest { (newMoneyPerYear, timeOff, currency1, ukTax) ->
                    val taxD = ukTax?.resolve()?.toDouble() ?: 0.0
                    val gross = (newMoneyPerYear?.resolve()?.toDouble() ?: 0.0) + taxD
                    store.registry["$SECTION_BASE:$MONEY_PER_WEEK"] =
                        if (gross > 0) {
                            gross.chainify() / WEEKS_PER_YEAR / timeOff?.toFeeMultiplier() / currency1?.rate
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
        title = "$title (->$sectionId)",
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