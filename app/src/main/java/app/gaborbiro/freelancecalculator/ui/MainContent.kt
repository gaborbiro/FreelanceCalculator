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
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.DATA_ID_FEE_PER_HOUR
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.DATA_ID_HOURS_PER_WEEK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.DATA_ID_MONEY_PER_WEEK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_ID_BASE
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.theme.FreelanceCalculatorTheme
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.SelectableContainer
import app.gaborbiro.freelancecalculator.ui.view.SingleInputContainer
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.resolve
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun CalculatorContent(
    store: Store,
    currencyRepository: CurrencyRepository,
) {
    val selectedIndex: Int? by store.selectedIndex.collectAsState(initial = null)
    var indexCounter = -1

    fun selectionUpdater(indexCounter: Int): () -> Unit = {
        store.selectedIndex = flowOf(indexCounter)
    }

    val selectableContainerModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = PADDING_LARGE)

    val feePerHour by store
        .registry["${SECTION_ID_BASE}:${DATA_ID_FEE_PER_HOUR}"]
        .collectAsState(initial = null)

    val hoursPerWeek by store
        .registry["${SECTION_ID_BASE}:${DATA_ID_HOURS_PER_WEEK}"]
        .collectAsState(initial = null)

    LaunchedEffect(Unit) {
        store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"]
            .drop(1)
            .collect { newMoneyPerWeek ->
                when (selectedIndex) {
                    0 -> {
                        store.registry["${SECTION_ID_BASE}:${DATA_ID_FEE_PER_HOUR}"] =
                            (newMoneyPerWeek / hoursPerWeek)?.simplify()
                    }

                    1 -> {
                        store.registry["${SECTION_ID_BASE}:${DATA_ID_HOURS_PER_WEEK}"] =
                            (newMoneyPerWeek / feePerHour)?.simplify()
                    }
                }
            }
    }

    @Suppress("KotlinConstantConditions")
    SingleInputContainer(
        containerModifier = selectableContainerModifier,
        label = "Fee per hour",
        value = feePerHour.resolve().format(decimalCount = 2),
        clearButtonVisible = true,
        selected = selectedIndex == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChanged = { newFeePerHour: Double? ->
            CoroutineScope(Dispatchers.IO).launch {
                if (selectedIndex == 2) {
                    store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"] =
                        hoursPerWeek * newFeePerHour
                } else if (selectedIndex == 1) {
                    store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"]
                        .collectLatest { moneyPerWeek ->
                            store.registry["${SECTION_ID_BASE}:${DATA_ID_HOURS_PER_WEEK}"] =
                                (moneyPerWeek / newFeePerHour)?.simplify()
                        }
                }
                store.registry["${SECTION_ID_BASE}:${DATA_ID_FEE_PER_HOUR}"] =
                    newFeePerHour?.chainify()
            }
        },
    )

    SingleInputContainer(
        containerModifier = selectableContainerModifier,
        label = "Hours per week",
        value = hoursPerWeek.resolve().format(decimalCount = 0),
        clearButtonVisible = true,
        selected = selectedIndex == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChanged = { newHoursPerWeek: Double? ->
            CoroutineScope(Dispatchers.IO).launch {
                if (selectedIndex == 2) {
                    store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"] =
                        newHoursPerWeek * feePerHour
                } else if (selectedIndex == 0) {
                    store.registry["${SECTION_ID_BASE}:${DATA_ID_MONEY_PER_WEEK}"]
                        .collectLatest { moneyPerWeek ->
                            store.registry["${SECTION_ID_BASE}:${DATA_ID_HOURS_PER_WEEK}"] =
                                (moneyPerWeek / newHoursPerWeek)?.simplify()
                        }
                }
                store.registry["${SECTION_ID_BASE}:${DATA_ID_HOURS_PER_WEEK}"] =
                    newHoursPerWeek?.chainify()
            }
        },
    )

    SelectableContainer(
        modifier = selectableContainerModifier,
        selected = selectedIndex == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
    ) { modifier ->
        Column(
            modifier = modifier
                .padding(bottom = PADDING_LARGE)
        ) {
            ResultsSection(
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