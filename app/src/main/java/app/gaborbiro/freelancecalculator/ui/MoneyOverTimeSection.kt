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
import java.math.BigDecimal


@ExperimentalMaterial3Api
@Composable
fun MoneyOverTimeContent(
    moneyPerWeek: BigDecimal?,
    daysPerWeek: BigDecimal?,
    onMoneyPerWeekChange: (value: BigDecimal?) -> Unit,
) {
    val perMonth by rememberSaveable(moneyPerWeek) {
        mutableStateOf(moneyPerWeek * WEEKS_PER_MONTH)
    }
    InputFieldWrapper(
        label = "Total per month",
        clearButtonVisible = true,
        value = perMonth,
        decimalCount = 0,
    ) {
        onMoneyPerWeekChange(it / WEEKS_PER_MONTH)
    }

    val perYear by rememberSaveable(moneyPerWeek) {
        mutableStateOf(moneyPerWeek * WEEKS_PER_YEAR)
    }
    InputFieldWrapper(
        label = "per year",
        value = perYear,
        decimalCount = 0,
    ) {
        onMoneyPerWeekChange(it / WEEKS_PER_YEAR)
    }

    InputField(
        modifier = Modifier
            .wrapContentSize(),
        label = "per week",
        value = moneyPerWeek.format(0),
        decimalCount = 0,
        onValueChange = { value ->
            onMoneyPerWeekChange(value.parse())
        },
    )

    val perDay by rememberSaveable(moneyPerWeek, daysPerWeek) {
        mutableStateOf(moneyPerWeek / daysPerWeek)
    }

    InputFieldWrapper(
        label = "per day",
        value = perDay,
        decimalCount = 2,
    ) {
        onMoneyPerWeekChange(it * daysPerWeek)
    }
}

@ExperimentalMaterial3Api
@Composable
private fun InputFieldWrapper(
    label: String,
    value: BigDecimal?,
    decimalCount: Int = 2,
    clearButtonVisible: Boolean = false,
    onValueChange: (value: BigDecimal?) -> Unit
) {
    InputField(
        modifier = Modifier
            .wrapContentSize(),
        label = label,
        value = value?.format(decimalCount) ?: "",
        clearButtonVisible = clearButtonVisible,
        decimalCount = 2,
        onValueChange = {
            onValueChange(it.parse())
        },
    )
}

@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Composable
fun MoneyOverTimeSection(
    containerModifier: Modifier,
    selected: Boolean,
    moneyPerWeek: BigDecimal?,
    daysPerWeek: BigDecimal?,
    onSelected: () -> Unit,
    onMoneyPerWeekChange: (value: BigDecimal?) -> Unit,
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
    MoneyOverTimeSection(
        containerModifier = Modifier
            .fillMaxWidth()
            .padding(MARGIN_LARGE),
        moneyPerWeek = BigDecimal.valueOf(355.0),
        daysPerWeek = BigDecimal.valueOf(5.0),
        onMoneyPerWeekChange = { },
        selected = true,
        onSelected = { },
    )
}