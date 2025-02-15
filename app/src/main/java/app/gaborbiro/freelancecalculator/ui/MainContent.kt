@file:OptIn(ExperimentalMaterial3Api::class)

package app.gaborbiro.freelancecalculator.ui

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.FEE_PER_HOUR
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.HOURS_PER_WEEK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.MONEY_PER_WEEK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_BASE
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.sections.ResultsGroup
import app.gaborbiro.freelancecalculator.ui.theme.FreelanceCalculatorTheme
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.SelectableContainer
import app.gaborbiro.freelancecalculator.ui.view.SingleInputContainer
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.resolve
import app.gaborbiro.freelancecalculator.util.simplify
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun CalculatorContent(
    store: Store,
    currencyRepository: CurrencyRepository,
) {
    val selectedIndex by remember { store.selectedIndex }.collectAsState(initial = 2)
    var directionJob: Job? = null

    LaunchedEffect(selectedIndex) {
        directionJob?.cancel()
        directionJob = launch {
            when (selectedIndex) {
                0 -> {
                    combine(
                        store.registry["$SECTION_BASE:$MONEY_PER_WEEK"],
                        store.registry["$SECTION_BASE:$HOURS_PER_WEEK"],
                    ) { moneyPerWeek, hoursPerWeek ->
                        moneyPerWeek / hoursPerWeek
                    }
                        .map { it.simplify() }
                        .collect {
                            store.registry["$SECTION_BASE:$FEE_PER_HOUR"] = it
                        }
                }

                1 -> {
                    combine(
                        store.registry["$SECTION_BASE:$MONEY_PER_WEEK"],
                        store.registry["$SECTION_BASE:$FEE_PER_HOUR"],
                    ) { moneyPerWeek, feePerHour ->
                        moneyPerWeek / feePerHour
                    }
                        .map { it.simplify() }
                        .collect {
                            store.registry["$SECTION_BASE:$HOURS_PER_WEEK"] = it
                        }
                }

                2 -> {
                    combine(
                        store.registry["$SECTION_BASE:$FEE_PER_HOUR"],
                        store.registry["$SECTION_BASE:$HOURS_PER_WEEK"],
                    ) { feePerHour, hoursPerWeek ->
                        feePerHour * hoursPerWeek
                    }
                        .map { it.simplify() }
                        .collect {
                            store.registry["$SECTION_BASE:$MONEY_PER_WEEK"] = it
                        }
                }
            }
        }
    }

    val feePerHour: ArithmeticChain? by remember {
        store.registry["$SECTION_BASE:$FEE_PER_HOUR"]
    }.collectAsState(initial = null)

    val feePerHourStr = remember(feePerHour) { feePerHour.resolve().format(decimalCount = 2) }

    SingleInputContainer(
        containerModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PADDING_LARGE),
        label = "Fee per hour",
        value = feePerHourStr,
        clearButtonVisible = true,
        selected = selectedIndex == 0,
        onSelected = { store.selectedIndex = flowOf(0) },
    ) { newFeePerHour ->
        store.registry["$SECTION_BASE:$FEE_PER_HOUR"] = newFeePerHour?.chainify()
    }

    val hoursPerWeek: ArithmeticChain? by remember {
        store.registry["$SECTION_BASE:$HOURS_PER_WEEK"]
    }.collectAsState(initial = null)

    val hoursPerWeekStr = remember(hoursPerWeek) { hoursPerWeek.resolve().format(decimalCount = 0) }

    SingleInputContainer(
        containerModifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PADDING_LARGE),
        label = "Hours per week",
        value = hoursPerWeekStr,
        clearButtonVisible = true,
        selected = selectedIndex == 1,
        onSelected = { store.selectedIndex = flowOf(1) },
    ) { newHoursPerWeek ->
        store.registry["$SECTION_BASE:$HOURS_PER_WEEK"] = newHoursPerWeek?.chainify()
    }

    SelectableContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = PADDING_LARGE),
        selected = selectedIndex == 2,
        onSelected = { store.selectedIndex = flowOf(2) },
    ) { modifier ->
        Column(
            modifier = modifier
                .padding(bottom = PADDING_LARGE)
        ) {
            ResultsGroup(
                store = store,
                currencyRepository = currencyRepository,
            )
        }
    }
}

@Preview
@Composable
private fun CalculatorContentPreview() {
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
                CalculatorContent(
                    Store.dummyImplementation(),
                    CurrencyRepository.dummyImplementation(),
                )
            }
        }
    }
}