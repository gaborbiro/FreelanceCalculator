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
import androidx.compose.runtime.remember
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

/**
 * @param collapseId globally unique
 */
@OptIn(ExperimentalLayoutApi::class)
@ExperimentalMaterial3Api
@Composable
fun ColumnScope.MoneyBreakdown(
    collapseId: String,
    title: String? = null,
    store: Store,
    moneyPerWeek: ArithmeticChain?,
    onPerWeekValueChanged: (ArithmeticChain?) -> Unit,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
) {
    val expanded: Boolean? by remember {
        store.sectionExpander[collapseId]
    }.collectAsState(initial = true)

    val daysPerWeek by remember {
        store.daysPerWeek
    }.collectAsState(initial = null)

    FlowCard(
        modifier = Modifier
            .animateContentSize(),
        extraContent = extraContent,
        title = title,
    ) {
        val yearStr = remember(moneyPerWeek) {
            (moneyPerWeek * WEEKS_PER_YEAR)?.resolve().format(decimalCount = 0)
        }
        FocusPinnedInputField(
            modifier = Modifier
                .wrapContentSize(),
            label = "per year",
            value = yearStr,
            outlined = true,
        ) { perYear ->
            onPerWeekValueChanged(perYear / WEEKS_PER_YEAR)
        }

        val monthStr = remember(moneyPerWeek) {
            (moneyPerWeek * WEEKS_PER_MONTH)?.resolve().format(decimalCount = 0)
        }
        FocusPinnedInputField(
            modifier = Modifier
                .wrapContentSize(),
            label = "per month",
            value = monthStr,
            outlined = true,
        ) { perMonth ->
            onPerWeekValueChanged(perMonth / WEEKS_PER_MONTH)
        }

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = expanded != false
        ) {
            val weekStr = remember(moneyPerWeek) {
                moneyPerWeek?.resolve().format(decimalCount = 0)
            }
            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "per week",
                value = weekStr,
                outlined = true,
            ) { perWeek ->
                onPerWeekValueChanged(perWeek?.chainify())
            }
        }

        AnimatedVisibility(
            enter = fadeIn(),
            exit = fadeOut(),
            visible = expanded != false
        ) {
            val dayStr = remember(moneyPerWeek, daysPerWeek) {
                (moneyPerWeek / daysPerWeek)?.resolve().format(decimalCount = 2)
            }
            val perDay = FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "per day",
                value = dayStr,
                outlined = true,
            ) { perDay ->
                onPerWeekValueChanged(perDay?.chainify() * daysPerWeek)
            }
        }

        CollapseExpandButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            collapseId = collapseId,
            sectionExpander = store.sectionExpander,
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
                MoneyBreakdown(
                    collapseId = "dummy",
                    store = Store.dummyImplementation(),
                    moneyPerWeek = 1.0.chainify(),
                    onPerWeekValueChanged = {}
                )
            }
        }
    }
}