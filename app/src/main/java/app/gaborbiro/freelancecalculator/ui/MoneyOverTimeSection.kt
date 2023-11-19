package app.gaborbiro.freelancecalculator.ui

import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import app.gaborbiro.freelancecalculator.Store
import app.gaborbiro.freelancecalculator.WEEKS_PER_MONTH
import app.gaborbiro.freelancecalculator.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.currency.CurrencyRepository
import app.gaborbiro.freelancecalculator.div
import app.gaborbiro.freelancecalculator.format
import app.gaborbiro.freelancecalculator.minus
import app.gaborbiro.freelancecalculator.times
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_DOUBLE
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.FlowCard
import app.gaborbiro.freelancecalculator.ui.view.SelectableContainer
import app.gaborbiro.freelancecalculator.util.Lce
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.math.BigDecimal


@OptIn(ExperimentalLayoutApi::class)
@ExperimentalMaterial3Api
@Composable
fun MoneyOverTimeContent(
    sectionId: String,
    store: Store,
    moneyPerWeek: BigDecimal?,
    daysPerWeek: BigDecimal?,
    onMoneyPerWeekChange: (value: BigDecimal?) -> Unit,
) {
    val sectionExpander = store.sectionExpander()
    val expanded: Boolean? by sectionExpander[sectionId].collectAsState(initial = false)

    FlowCard(modifier = Modifier.animateContentSize()) {
        FocusPinnedInputField(
            modifier = Modifier
                .wrapContentSize(),
            label = "per month",
            value = (moneyPerWeek * WEEKS_PER_MONTH).format(decimalCount = 0),
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

        AnimatedVisibility(enter = fadeIn(), exit = fadeOut(), visible = expanded != false) {
            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "per week",
                value = moneyPerWeek.format(decimalCount = 0),
            ) { newValue ->
                onMoneyPerWeekChange(newValue)
            }
        }

        AnimatedVisibility(enter = fadeIn(), exit = fadeOut(), visible = expanded != false) {
            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "per day",
                value = (moneyPerWeek / daysPerWeek).format(decimalCount = 2),
            ) { newValue ->
                onMoneyPerWeekChange(newValue * daysPerWeek)
            }
        }

        Box(
            Modifier
                .align(Alignment.CenterVertically)
                .weight(1f)
        ) {
            val modifier = Modifier
                .size(48.dp)
                .padding(PADDING_LARGE)
                .align(Alignment.CenterEnd)

            if (expanded != false) {
                Icon(
                    modifier = modifier
                        .clickable {
                            sectionExpander[sectionId] = false
                        },
                    imageVector = Icons.Outlined.KeyboardArrowLeft,
                    contentDescription = "collapse",
                )
            } else {
                Icon(
                    modifier = modifier
                        .clickable {
                            sectionExpander[sectionId] = true
                        },
                    imageVector = Icons.Outlined.KeyboardArrowRight,
                    contentDescription = "expand",
                )
            }
        }
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
                .padding(bottom = PADDING_LARGE)
        ) {
            val daysPerWeek: State<BigDecimal?> = store.daysPerWeek
                .collectAsState(initial = null)
            val fee: State<BigDecimal?> = store.fee
                .collectAsState(initial = null)
            val fromCurrency by store.fromCurrency
                .collectAsState(initial = null)
            val toCurrency by store.toCurrency
                .collectAsState(initial = null)

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

            MoneyOverTimeContent(
                sectionId = "BASE",
                store = store,
                moneyPerWeek = moneyPerWeek,
                daysPerWeek = daysPerWeek.value,
                onMoneyPerWeekChange = onMoneyPerWeekChange
            )

            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(start = PADDING_LARGE),
                label = "Fee/reduction %",
                value = fee.value.format(decimalCount = 2),
                clearButtonVisible = true,
                onValueChange = { value ->
                    CoroutineScope(Dispatchers.IO).launch {
                        store.fee = flowOf(value)
                    }
                },
            )

            val feeMultiplier: BigDecimal? = remember(moneyPerWeek, fee.value) {
                BigDecimal.ONE - fee.value.div(BigDecimal(100))
            }

            MoneyOverTimeContent(
                sectionId = "FEE",
                store = store,
                moneyPerWeek = moneyPerWeek * feeMultiplier,
                daysPerWeek = daysPerWeek.value,
            ) {
                onMoneyPerWeekChange(it / feeMultiplier)
            }

            Text(
                modifier = Modifier
                    .padding(top = PADDING_DOUBLE, start = PADDING_LARGE, end = PADDING_LARGE),
                text = "Currency exchange",
                style = MaterialTheme.typography.bodyMedium,
            )

            Row {
                CurrencySelector(
                    modifier = Modifier
                        .weight(1f)
                        .padding(PADDING_LARGE),
                    selectedCurrency = fromCurrency,
                    currencyRepository = currencyRepository,
                ) {
                    store.fromCurrency = flowOf(it)
                }
                CurrencySelector(
                    modifier = Modifier
                        .weight(1f)
                        .padding(PADDING_LARGE),
                    selectedCurrency = toCurrency,
                    currencyRepository = currencyRepository
                ) {
                    store.toCurrency = flowOf(it)
                }
            }

            val storeRate = remember { store.rate }
                .collectAsState(initial = null)

            var rate by rememberSaveable(storeRate.value) {
                mutableStateOf(storeRate.value)
            }

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
                        rate = BigDecimal(newRate.data)
                        store.rate = flowOf(BigDecimal(newRate.data))
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

            val currencyMultiplier: BigDecimal? = remember(moneyPerWeek, fee.value, rate) {
                (BigDecimal.ONE - fee.value.div(BigDecimal(100))) * rate
            }

            MoneyOverTimeContent(
                sectionId = "CURRENCY",
                store = store,
                moneyPerWeek = moneyPerWeek * currencyMultiplier,
                daysPerWeek = daysPerWeek.value,
            ) {
                onMoneyPerWeekChange(it / currencyMultiplier)
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
            .padding(PADDING_LARGE),
        moneyPerWeek = BigDecimal.valueOf(355.0),
        onMoneyPerWeekChange = { },
        store = Store.dummyImplementation(),
        currencyRepository = CurrencyRepository.dummyImplementation(),
        selected = true,
        onSelected = { },
    )
}