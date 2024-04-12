package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.theme.FreelanceCalculatorTheme
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_MONTH
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.times

@OptIn(ExperimentalLayoutApi::class)
@ExperimentalMaterial3Api
@Composable
fun ColumnScope.MoneyOverTime(
    sectionId: String,
    title: String? = null,
    store: Store,
    moneyPerWeek: ArithmeticChain?,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
    onMoneyPerWeekChange: (newValue: ArithmeticChain?) -> Unit,
) {
    val expanded: Boolean? by store.sectionExpander()[sectionId].collectAsState(initial = true)
    val daysPerWeek by store.daysPerWeek.collectAsState(initial = null)

    FlowCard(
        modifier = Modifier
            .animateContentSize(),
        extraContent = extraContent,
        title = title,
    ) {
        FocusPinnedInputField(
            modifier = Modifier
                .wrapContentSize(),
            label = "per year",
            value = (moneyPerWeek * WEEKS_PER_YEAR)?.resolve().format(decimalCount = 0),
            outlined = true,
        ) { newValue ->
            onMoneyPerWeekChange(newValue / WEEKS_PER_YEAR)
        }
        FocusPinnedInputField(
            modifier = Modifier
                .wrapContentSize(),
            label = "per month",
            value = (moneyPerWeek * WEEKS_PER_MONTH)?.resolve().format(decimalCount = 0),
            outlined = true,
        ) { newValue ->
            onMoneyPerWeekChange(newValue / WEEKS_PER_MONTH)
        }

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = expanded != false
        ) {
            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "per week",
                value = moneyPerWeek?.resolve().format(decimalCount = 0),
                outlined = true,
            ) { newValue ->
                onMoneyPerWeekChange(newValue.chainify())
            }
        }

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = expanded != false
        ) {
            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "per day",
                value = (moneyPerWeek / daysPerWeek)?.resolve().format(decimalCount = 2),
                outlined = true,
            ) { newValue ->
                onMoneyPerWeekChange(newValue.chainify() * daysPerWeek)
            }
        }

        CollapseExpandButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            id = sectionId,
            sectionExpander = store.sectionExpander(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun MoneyOverTimePreview() {
    FreelanceCalculatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = PADDING_LARGE),
                verticalArrangement = Arrangement.spacedBy(PADDING_LARGE),
            ) {
                MoneyOverTime(
                    sectionId = "dummy",
                    store = Store.dummyImplementation(),
                    moneyPerWeek = 1.0.chainify(),
                    onMoneyPerWeekChange = { }
                )
            }
        }
    }
}