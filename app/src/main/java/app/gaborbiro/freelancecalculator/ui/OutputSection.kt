package app.gaborbiro.freelancecalculator.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.SelectableContainer
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.Lce
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


@Composable
fun OutputSection(
    containerModifier: Modifier,
    selected: Boolean,
    moneyPerWeek: ArithmeticChain?,
    store: Store,
    currencyRepository: CurrencyRepository,
    onSelected: () -> Unit,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    SelectableContainer(
        modifier = containerModifier,
        selected = selected,
        onSelected = onSelected,
    ) { modifier ->
        Column(
            modifier = modifier
                .padding(bottom = PADDING_LARGE)
        ) {
            val daysPerWeek: State<Double?> = store.daysPerWeek
                .collectAsState(initial = null)

            DaysPerWeekSection(store)

            BasicSection(store, moneyPerWeek, daysPerWeek.value, onMoneyPerWeekChange)

            FeeSection(store, moneyPerWeek, daysPerWeek.value, onMoneyPerWeekChange)

            var exchangeRate: Double? by rememberSaveable {
                mutableStateOf(null)
            }
            val fee: State<Double?> = store.fee
                .collectAsState(initial = null)
            val feeAndExchangeRateMultiplier: ArithmeticChain? =
                remember(fee.value, exchangeRate) {
                    fee.value?.let { 1.0 - (it / 100.0) }.chainify() * exchangeRate
                }

            CurrencySection(
                store = store,
                currencyRepository = currencyRepository,
                moneyPerWeek = moneyPerWeek * feeAndExchangeRateMultiplier,
                daysPerWeek = daysPerWeek.value,
                onExchangeRateChanged = {
                    exchangeRate = it
                },
            ) {
                onMoneyPerWeekChange(it / feeAndExchangeRateMultiplier)
            }

            val toCurrency by store.toCurrency
                .collectAsState(initial = null)

            if (toCurrency == "GBP") {
                TaxAndNetIncomeSection(
                    store,
                    moneyPerWeek,
                    daysPerWeek.value,
                    feeAndExchangeRateMultiplier,
                    onMoneyPerWeekChange,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.BasicSection(
    store: Store,
    moneyPerWeek: ArithmeticChain?,
    daysPerWeek: Double?,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    MoneyOverTime(
        sectionId = "BASE",
        title = "Basic",
        store = store,
        moneyPerWeek = moneyPerWeek,
        daysPerWeek = daysPerWeek,
        onMoneyPerWeekChange = onMoneyPerWeekChange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.FeeSection(
    store: Store,
    moneyPerWeek: ArithmeticChain?,
    daysPerWeek: Double?,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    val fee: State<Double?> = store.fee
        .collectAsState(initial = null)
    val feeMultiplier: Double? = remember(moneyPerWeek, fee.value) {
        fee.value?.let { 1.0 - (it / 100.0) }
    }

    MoneyOverTime(
        sectionId = "FEE",
        title = "Fee / Reduction",
        store = store,
        moneyPerWeek = moneyPerWeek * feeMultiplier,
        daysPerWeek = daysPerWeek,
        extraContent = {
            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "%",
                value = fee.value.format(decimalCount = 2),
                outlined = false,
                clearButtonVisible = true,
                onValueChange = { value ->
                    CoroutineScope(Dispatchers.IO).launch {
                        store.fee = flowOf(value)
                    }
                },
            )
        }
    ) { newValue: ArithmeticChain? ->
        onMoneyPerWeekChange(newValue / feeMultiplier)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.DaysPerWeekSection(store: Store) {
    val daysPerWeek: State<Double?> = store.daysPerWeek
        .collectAsState(initial = null)

    FocusPinnedInputField(
        modifier = Modifier
            .wrapContentSize()
            .align(Alignment.End),
        label = "Days per week",
        value = daysPerWeek.value.format(decimalCount = 0),
        outlined = false,
        clearButtonVisible = true,
        onValueChange = { value ->
            CoroutineScope(Dispatchers.IO).launch {
                store.daysPerWeek = flowOf(value)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.CurrencySection(
    store: Store,
    currencyRepository: CurrencyRepository,
    moneyPerWeek: ArithmeticChain?,
    daysPerWeek: Double?,
    onExchangeRateChanged: (rate: Double) -> Unit,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    val fromCurrency by store.fromCurrency
        .collectAsState(initial = null)
    val toCurrency by store.toCurrency
        .collectAsState(initial = null)

    if (fromCurrency != null && toCurrency != null) {
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
                onExchangeRateChanged(newRate.data)
            }

            is Lce.Error -> {
                Toast.makeText(
                    LocalContext.current,
                    newRate.throwable.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    MoneyOverTime(
        sectionId = "CURRENCY",
        title = "Currency conversion",
        store = store,
        moneyPerWeek = moneyPerWeek,
        daysPerWeek = daysPerWeek,
        extraContent = {
            Row {
                CurrencySelector(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(PADDING_LARGE),
                    selectedCurrency = fromCurrency,
                    label = "From",
                    currencyRepository = currencyRepository,
                ) {
                    store.fromCurrency = flowOf(it)
                }
                CurrencySelector(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(PADDING_LARGE),
                    selectedCurrency = toCurrency,
                    label = "To",
                    currencyRepository = currencyRepository
                ) {
                    store.toCurrency = flowOf(it)
                }
            }
        }
    ) { newValue: ArithmeticChain? ->
        onMoneyPerWeekChange(newValue)
    }
}

@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Preview
@Composable
private fun MoneyOverTimePreview() {
    OutputSection(
        containerModifier = Modifier
            .fillMaxWidth()
            .padding(PADDING_LARGE),
        moneyPerWeek = 355.0.chainify(),
        onMoneyPerWeekChange = { },
        store = Store.dummyImplementation(),
        currencyRepository = CurrencyRepository.dummyImplementation(),
        selected = true,
        onSelected = { },
    )
}