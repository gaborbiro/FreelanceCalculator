package app.gaborbiro.freelancecalculator.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.WEEKS_PER_MONTH
import app.gaborbiro.freelancecalculator.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.div
import app.gaborbiro.freelancecalculator.format
import app.gaborbiro.freelancecalculator.parse
import app.gaborbiro.freelancecalculator.times
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE
import app.gaborbiro.freelancecalculator.ui.view.Container
import app.gaborbiro.freelancecalculator.ui.view.FlowCard
import app.gaborbiro.freelancecalculator.ui.view.InputField


@ExperimentalMaterial3Api
@Composable
fun MoneyOverTimeContent(
    moneyPerWeek: Double?,
    daysPerWeek: Double?,
    onMoneyPerWeekChange: (value: Double?) -> Unit,
) {
    InputFieldWrapper(
        label = "Total per month",
        clearButtonVisible = true,
        value = (moneyPerWeek * WEEKS_PER_MONTH).format(decimalCount = 0),
    ) {
        onMoneyPerWeekChange(it.parse() / WEEKS_PER_MONTH)
    }
    InputFieldWrapper(
        label = "per year",
        value = (moneyPerWeek * WEEKS_PER_YEAR).format(decimalCount = 0),
    ) {
        onMoneyPerWeekChange(it.parse() / WEEKS_PER_YEAR)
    }
    InputField(
        modifier = Modifier
            .wrapContentSize(),
        label = "per week",
        value = moneyPerWeek.format(0),
        onValueChange = { value ->
            onMoneyPerWeekChange(value.parse())
        },
    )
    InputFieldWrapper(
        label = "per day",
        value = (moneyPerWeek / daysPerWeek).format(decimalCount = 2),
    ) {
        onMoneyPerWeekChange(it.parse() * daysPerWeek)
    }
}

@ExperimentalMaterial3Api
@Composable
private fun InputFieldWrapper(
    label: String,
    value: String,
    clearButtonVisible: Boolean = false,
    onValueChange: (value: String) -> Unit
) {
    InputField(
        modifier = Modifier
            .wrapContentSize(),
        label = label,
        value = value,
        clearButtonVisible = clearButtonVisible,
        onValueChange = {
            onValueChange(it)
        },
    )
}

@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Composable
fun MoneyOverTime(
    containerModifier: Modifier,
    selected: Boolean,
    moneyPerWeek: Double?,
    daysPerWeek: Double?,
    onSelected: () -> Unit,
    onMoneyPerWeekChange: (value: Double?) -> Unit,
) {
    Container(
        modifier = containerModifier,
        selected = selected,
        onSelected = onSelected,
    ) { modifier ->
        FlowCard(modifier = modifier) {
            MoneyOverTimeContent(moneyPerWeek, daysPerWeek, onMoneyPerWeekChange)
        }
    }
}

@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Preview
@Composable
private fun MoneyOverTimePreview() {
    MoneyOverTime(
        containerModifier = Modifier
            .fillMaxWidth()
            .padding(MARGIN_LARGE),
        moneyPerWeek = 355.0,
        daysPerWeek = 5.0,
        onMoneyPerWeekChange = { },
        selected = true,
        onSelected = { },
    )
}