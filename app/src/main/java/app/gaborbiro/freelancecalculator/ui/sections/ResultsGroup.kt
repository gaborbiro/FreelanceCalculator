package app.gaborbiro.freelancecalculator.ui.sections

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.MONEY_PER_WEEK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_CURRENCY1
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_CURRENCY2
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_GROSS
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_TAX
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_TIMEOFF
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_UK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.TYPE_FEE
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.sections.currency.CurrencySection
import app.gaborbiro.freelancecalculator.ui.sections.fee.FeeSection
import app.gaborbiro.freelancecalculator.ui.sections.fee.toFeeMultiplier
import app.gaborbiro.freelancecalculator.ui.sections.tax.UKTaxSection
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.MoneyBreakdown
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.safelyCalculate2
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.merge


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.ResultsGroup(
    store: Store,
    currencyRepository: CurrencyRepository,
) {
    val outputMoneyPerWeek = store.registry["$SECTION_GROSS:$MONEY_PER_WEEK"].collectAsState()

    val grossOutput: MutableSharedFlow<ArithmeticChain?> =
        remember { MutableSharedFlow(extraBufferCapacity = 1) }
    val currency1Output: MutableSharedFlow<ArithmeticChain?> =
        remember { MutableSharedFlow(extraBufferCapacity = 1) }
    val timeOffOutput: MutableSharedFlow<ArithmeticChain?> =
        remember { MutableSharedFlow(extraBufferCapacity = 1) }
    val taxOutput: MutableSharedFlow<ArithmeticChain?> =
        remember { MutableSharedFlow(extraBufferCapacity = 1) }
    val taxCurrencyOutput: MutableSharedFlow<ArithmeticChain?> =
        remember { MutableSharedFlow(extraBufferCapacity = 1) }

    MoneyBreakdown(
        collapseId = "$SECTION_GROSS:collapse",
        title = "Gross",
        store = store,
        moneyPerWeek = outputMoneyPerWeek.value,
        onPerWeekValueChanged = {
            grossOutput.emit(it)
        }
    )

    CurrencySection(
        inputId = SECTION_GROSS,
        sectionId = SECTION_CURRENCY1,
        title = "Currency",
        store = store,
        currencyRepository = currencyRepository,
        onPerWeekValueChanged = {
            currency1Output.emit(it)
        }
    )

    LaunchedEffect(Unit) {
        store.currencySelections[SECTION_CURRENCY1]
            .combine(store.currencySelections[SECTION_CURRENCY2], { a, b -> a to b })
            .collect { (selectedCurrency1, selectedCurrency2) ->
                val (_, toCurrency1) = selectedCurrency1 ?: (null to null)
                val (_, toCurrency2) = selectedCurrency2 ?: (null to null)
                store.currencySelections[SECTION_CURRENCY2].emit(toCurrency1 to toCurrency2)
            }
    }

    FeeSection(
        inputId = SECTION_CURRENCY1,
        sectionId = SECTION_TIMEOFF,
        title = "Time off",
        store = store,
        onPerWeekValueChanged = {
            println(timeOffOutput.emit(it))
        }
    )

    val selectedCurrency1 by store.currencySelections[SECTION_CURRENCY1].collectAsState()
    val (fromCurrency1, toCurrency1) = selectedCurrency1 ?: (null to null)

    val isUKTax = toCurrency1 == "GBP"

    val taxSectionId = (if (isUKTax) "${SECTION_UK}/" else "") + SECTION_TAX

    if (fromCurrency1 != null) {
        if (isUKTax) {
            UKTaxSection(
                inputId = SECTION_TIMEOFF,
                sectionId = taxSectionId,
                store = store,
                onPerWeekValueChanged = {
                    taxOutput.emit(it)
                }
            )
        } else {
            FeeSection(
                inputId = SECTION_TIMEOFF,
                sectionId = taxSectionId,
                title = "After tax",
                store = store,
                onPerWeekValueChanged = {
                    taxOutput.emit(it)
                }
            )
        }
        CurrencySection(
            inputId = taxSectionId,
            sectionId = SECTION_CURRENCY2,
            title = "Currency",
            store = store,
            currencyRepository = currencyRepository,
            onPerWeekValueChanged = {
                taxCurrencyOutput.emit(it)
            }
        )
    }

    LaunchedEffect(Unit) {
        val reverseMoneyPerWeek: Flow<ArithmeticChain?> = merge(
            grossOutput,
            currency1Output,
            combine(
                timeOffOutput,
                store.exchangeRates[SECTION_CURRENCY1],
            ) { newMoneyPerWeek, currency1 ->
                newMoneyPerWeek / currency1?.rate
            },
            combine(
                taxOutput,
                store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                store.exchangeRates[SECTION_CURRENCY1],
            ) { tax, timeOff, currency1 ->
                tax / timeOff.toFeeMultiplier() / currency1?.rate
            },
            if (isUKTax) {
                combine(
                    taxCurrencyOutput,
                    store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                    store.exchangeRates[SECTION_CURRENCY1],
                    store.registry[taxSectionId],
                ) { ukTaxCurrencyOutput, timeOff, currency1, ukTax ->
                    val n = ukTaxCurrencyOutput / timeOff.toFeeMultiplier() / currency1?.rate
                    safelyCalculate2(n, ukTax) { n, ukTax ->
                        val newMoneyPerYear = (n * WEEKS_PER_YEAR).resolve().toDouble()
                        val taxD = ukTax.resolve().toDouble()
                        ((newMoneyPerYear + taxD) / WEEKS_PER_YEAR)!!
                    }
                }
            } else {
                combine(
                    taxCurrencyOutput,
                    store.registry["$SECTION_TIMEOFF:$TYPE_FEE"],
                    store.exchangeRates[SECTION_CURRENCY1],
                    store.registry["$taxSectionId:$TYPE_FEE"],
                ) { ptTaxCurrency, timeOff, currency1, ptTaxRate ->
                    ptTaxCurrency / timeOff.toFeeMultiplier() / currency1?.rate / ptTaxCurrency.toFeeMultiplier()
                }
            }
        )

        reverseMoneyPerWeek.distinctUntilChanged().collect {
            store.registry["$SECTION_GROSS:$MONEY_PER_WEEK"].emit(it)
        }
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