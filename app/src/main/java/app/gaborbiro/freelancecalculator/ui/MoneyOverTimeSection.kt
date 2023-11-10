package app.gaborbiro.freelancecalculator.ui

import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.WEEKS_PER_MONTH
import app.gaborbiro.freelancecalculator.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.div
import app.gaborbiro.freelancecalculator.format
import app.gaborbiro.freelancecalculator.times
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE
import app.gaborbiro.freelancecalculator.ui.view.Container
import app.gaborbiro.freelancecalculator.ui.view.FlowCard
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