package app.gaborbiro.freelancecalculator.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.Store
import app.gaborbiro.freelancecalculator.WEEKS_PER_MONTH
import app.gaborbiro.freelancecalculator.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.currency.CurrencyRepository
import app.gaborbiro.freelancecalculator.div
import app.gaborbiro.freelancecalculator.format
import app.gaborbiro.freelancecalculator.minus
import app.gaborbiro.freelancecalculator.times
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_DOUBLE
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE
import app.gaborbiro.freelancecalculator.ui.view.FlowCard
import app.gaborbiro.freelancecalculator.ui.view.SelectableContainer
import app.gaborbiro.freelancecalculator.util.Lce
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.math.BigDecimal


@ExperimentalMaterial3Api
@Composable
fun MoneyOverTimeContent(
    moneyPerWeek: BigDecimal?,
    daysPerWeek: BigDecimal?,
    onMoneyPerWeekChange: (value: BigDecimal?) -> Unit,
) {
    FocusPinnedInputField(
        modifier = Modifier
            .wrapContentSize(),
        label = "Total per month",
        value = (moneyPerWeek * WEEKS_PER_MONTH).format(decimalCount = 0),
        clearButtonVisible = true,
    ) { newValue ->
        onMoneyPerWeekChange(newValue / WEEKS_PER_MONTH)
    }
    FocusPinnedInputField(
        modifier = Modifier
            .wrapContentSize(),
        label = "per year",
        value = (moneyPerWeek * WEEKS_PER_YEAR).format(decimalCount = 0),
    ) { newValue ->
        onMoneyPerWeekChange(newValue / WEEKS_PER_YEAR)
    }
    FocusPinnedInputField(
        modifier = Modifier
            .wrapContentSize(),
        label = "per week",
        value = moneyPerWeek.format(decimalCount = 0),
    ) { newValue ->
        onMoneyPerWeekChange(newValue)
    }
    FocusPinnedInputField(
        modifier = Modifier
            .wrapContentSize(),
        label = "per day",
        value = (moneyPerWeek / daysPerWeek).format(decimalCount = 2),
    ) { newValue ->
        onMoneyPerWeekChange(newValue * daysPerWeek)
    }
}

@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Composable
fun MoneyOverTimeSection(
    containerModifier: Modifier,
    selected: Boolean,
    moneyPerWeek: BigDecimal?,
    store: Store,
    currencyRepository: CurrencyRepository,
    onSelected: () -> Unit,
    onMoneyPerWeekChange: (value: BigDecimal?) -> Unit,
) {
    SelectableContainer(
        modifier = containerModifier,
        selected = selected,
        onSelected = onSelected,
    ) { modifier ->
        Column(
            modifier = modifier
                .padding(bottom = MARGIN_LARGE)
        ) {
            val daysPerWeek: State<BigDecimal?> = store.daysPerWeek.collectAsState(initial = null)
            val fee: State<BigDecimal?> = store.fee.collectAsState(initial = null)

            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.End),
                label = "Days per week",
                value = daysPerWeek.value.format(decimalCount = 0),
                clearButtonVisible = true,
                onValueChange = { value ->
                    CoroutineScope(Dispatchers.IO).launch {
                        store.daysPerWeek = flowOf(value)
                    }
                },
            )
            FlowCard(modifier = Modifier) {
                MoneyOverTimeContent(
                    moneyPerWeek,
                    daysPerWeek.value,
                    onMoneyPerWeekChange
                )
            }
            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = MARGIN_LARGE),
                label = "Fee/reduction %",
                value = fee.value.format(decimalCount = 2),
                clearButtonVisible = true,
                onValueChange = { value ->
                    CoroutineScope(Dispatchers.IO).launch {
                        store.fee = flowOf(value)
                    }
                },
            )
            FlowCard(modifier = Modifier) {
                val feeMultiplier: BigDecimal? = BigDecimal.ONE - fee.value.div(BigDecimal(100))

                MoneyOverTimeContent(
                    moneyPerWeek = moneyPerWeek * feeMultiplier,
                    daysPerWeek.value,
                ) {
                    onMoneyPerWeekChange(it / feeMultiplier)
                }
            }
            Text(
                modifier = Modifier
                    .padding(top = MARGIN_DOUBLE, start = MARGIN_LARGE, end = MARGIN_LARGE),
                text = "Currency exchange",
                style = MaterialTheme.typography.bodyMedium,
            )
            Row {
                CurrencySelector(Modifier.weight(1f), currencyRepository)
                CurrencySelector(Modifier.weight(1f), currencyRepository)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CurrencySelector(
    modifier: Modifier,
    currencyRepository: CurrencyRepository,
) {
    var expanded by remember { mutableStateOf(false) }
    var selectedText by remember { mutableStateOf("") }
    var enabled by remember { mutableStateOf(true) }
    var currencies by remember { mutableStateOf(listOf("")) }
    var error: String? by remember { mutableStateOf(null) }

    DisposableEffect(Unit) {
        val subscription = currencyRepository.getCurrencies()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                when (it) {
                    is Lce.Data -> {
                        currencies = it.data.asList()
                        enabled = true
                    }

                    is Lce.Error -> {
                        error = it.throwable.message
                    }

                    Lce.Loading -> {
                        enabled = false
                    }
                }
            }
        onDispose {
            subscription.dispose()
        }
    }

    if (error != null) {
        Toast.makeText(
            LocalContext.current,
            error,
            Toast.LENGTH_SHORT
        ).show()
        error = null
    }

    ExposedDropdownMenuBox(
        modifier = modifier
            .wrapContentSize()
            .padding(MARGIN_LARGE),
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
    ) {
        TextField(
            value = selectedText,
            enabled = enabled,
            onValueChange = {
                selectedText = it
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )

        val filteredCurrencies =
            currencies.filter { it.contains(selectedText, ignoreCase = true) }
        if (filteredCurrencies.isNotEmpty()) {
            ExposedDropdownMenu(
                modifier = Modifier
                    .wrapContentSize(),
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                filteredCurrencies.forEach { currency ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .wrapContentSize(),
                        text = { Text(currency) },
                        onClick = {
                            expanded = false
                            selectedText = currency
                        },
                    )
                }
            }
        }
    }
}


@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Preview
@Composable
private fun MoneyOverTimePreview() {
    MoneyOverTimeSection(
        containerModifier = Modifier
            .fillMaxWidth()
            .padding(MARGIN_LARGE),
        moneyPerWeek = BigDecimal.valueOf(355.0),
        onMoneyPerWeekChange = { },
        store = Store.dummyImplementation(),
        currencyRepository = CurrencyRepository.getDummyImplementation(),
        selected = true,
        onSelected = { },
    )
}