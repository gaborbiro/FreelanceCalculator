package app.gaborbiro.freelancecalculator.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.TypedSubStore
import app.gaborbiro.freelancecalculator.ui.view.FlowCard
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_MONTH
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.div
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.hide.times
import java.math.BigDecimal

@OptIn(ExperimentalLayoutApi::class)
@ExperimentalMaterial3Api
@Composable
fun ColumnScope.MoneyOverTime(
    sectionId: String,
    title: String? = null,
    store: Store,
    moneyPerWeek: BigDecimal?,
    daysPerWeek: BigDecimal?,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
    onMoneyPerWeekChange: (value: BigDecimal?) -> Unit,
) {
    val sectionExpander: TypedSubStore<Boolean> = store.sectionExpander()
    val expanded: Boolean? by sectionExpander[sectionId].collectAsState(initial = false)
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
            value = (moneyPerWeek * WEEKS_PER_YEAR).format(decimalCount = 0),
        ) { newValue ->
            onMoneyPerWeekChange(newValue / WEEKS_PER_YEAR)
        }
        FocusPinnedInputField(
            modifier = Modifier
                .wrapContentSize(),
            label = "per month",
            value = (moneyPerWeek * WEEKS_PER_MONTH).format(decimalCount = 0),
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
                value = moneyPerWeek.format(decimalCount = 0),
            ) { newValue ->
                onMoneyPerWeekChange(newValue)
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
                value = (moneyPerWeek / daysPerWeek).format(decimalCount = 2),
            ) { newValue ->
                onMoneyPerWeekChange(newValue * daysPerWeek)
            }
        }

        CollapseExpandButton(
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .weight(1f),
            id = sectionId,
            sectionExpander = sectionExpander
        )
    }
}

