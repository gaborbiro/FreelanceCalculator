package app.gaborbiro.freelancecalculator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.Store
import app.gaborbiro.freelancecalculator.WEEKS_PER_MONTH
import app.gaborbiro.freelancecalculator.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.div
import app.gaborbiro.freelancecalculator.format
import app.gaborbiro.freelancecalculator.times
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE
import app.gaborbiro.freelancecalculator.ui.view.Card
import app.gaborbiro.freelancecalculator.ui.view.SelectableContainer
import app.gaborbiro.freelancecalculator.ui.view.FlowCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.math.BigDecimal


@ExperimentalMaterial3Api
@Composable
fun MoneyOverTimeContent(
    moneyPerWeek: BigDecimal?,
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
}

@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Composable
fun MoneyOverTimeSection(
    containerModifier: Modifier,
    selected: Boolean,
    moneyPerWeek: BigDecimal?,
    store: Store,
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
        ) {
            val daysPerWeek: State<BigDecimal?> = store.daysPerWeek.collectAsState(initial = null)
            FlowCard(modifier = Modifier) {
                MoneyOverTimeContent(moneyPerWeek, onMoneyPerWeekChange)
            }
            FocusPinnedInputField(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(MARGIN_LARGE),
                label = "Days per week",
                value = daysPerWeek.value.format(decimalCount = 0),
                clearButtonVisible = true,
                onValueChange = { value ->
                    CoroutineScope(Dispatchers.IO).launch {
                        store.daysPerWeek = flowOf(value)
                    }
                },
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .padding(MARGIN_LARGE)
            ) {
                Row {
                    FocusPinnedInputField(
                        modifier = Modifier
                            .wrapContentSize(),
                        label = "per day",
                        value = (moneyPerWeek / daysPerWeek.value).format(decimalCount = 2),
                    ) { newValue ->
                        onMoneyPerWeekChange(newValue * daysPerWeek.value)
                    }
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
        store = Store.DUMMY_IMPL,
        selected = true,
        onSelected = { },
    )
}