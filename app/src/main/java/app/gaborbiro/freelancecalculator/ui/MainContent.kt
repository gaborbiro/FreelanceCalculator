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
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.FEE_PER_HOUR
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.HOURS_PER_WEEK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.MONEY_PER_WEEK
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SECTION_BASE
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.theme.FreelanceCalculatorTheme
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.SelectableContainer
import app.gaborbiro.freelancecalculator.ui.view.singleInputContainer
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.resolve
import app.gaborbiro.freelancecalculator.util.simplify
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map


@SuppressLint("FlowOperatorInvokedInComposition")
@Composable
fun CalculatorContent(
    store: Store,
    currencyRepository: CurrencyRepository,
) {
    val selectedIndex: Int = store.selectedIndex.collectAsState(initial = null).value ?: 2
    var indexCounter = -1

    fun selectionUpdater(indexCounter: Int): () -> Unit = {
        store.selectedIndex = flowOf(indexCounter)
    }

    val selectableContainerModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = PADDING_LARGE)

    val feePerHour by store
        .registry["${SECTION_BASE}:${FEE_PER_HOUR}"]
        .collectAsState(initial = null)

    val hoursPerWeek by store
        .registry["${SECTION_BASE}:${HOURS_PER_WEEK}"]
        .collectAsState(initial = null)

    LaunchedEffect(selectedIndex) {
        val reverseFlow = when (selectedIndex) {
            0 -> {
                store.registry["${SECTION_BASE}:${MONEY_PER_WEEK}"]
                    .combine(store.registry["${SECTION_BASE}:${HOURS_PER_WEEK}"]) { moneyPerWeek, hoursPerWeek ->
                        moneyPerWeek / hoursPerWeek
                    }
                    .map { it.simplify() }
            }

            1 -> {
                store.registry["${SECTION_BASE}:${MONEY_PER_WEEK}"]
                    .combine(store.registry["${SECTION_BASE}:${FEE_PER_HOUR}"]) { moneyPerWeek, feePerHour ->
                        moneyPerWeek / feePerHour
                    }
                    .map { it.simplify() }
            }

            2 -> {
                combine(
                    store.registry["${SECTION_BASE}:${FEE_PER_HOUR}"],
                    store.registry["${SECTION_BASE}:${HOURS_PER_WEEK}"]
                ) { feePerHour, hoursPerWeek ->
                    feePerHour * hoursPerWeek
                }
            }

            else -> emptyFlow()
        }
        val key = when (selectedIndex) {
            0 -> "${SECTION_BASE}:${FEE_PER_HOUR}"
            1 -> "${SECTION_BASE}:${HOURS_PER_WEEK}"
            2 -> "${SECTION_BASE}:${MONEY_PER_WEEK}"
            else -> null
        }
        key?.let {
            store.registry.put(key, reverseFlow)
        }
    }

    @Suppress("KotlinConstantConditions")
    val newFeePerHour = singleInputContainer(
        containerModifier = selectableContainerModifier,
        label = "Fee per hour",
        value = feePerHour.resolve().format(decimalCount = 2),
        clearButtonVisible = true,
        selected = selectedIndex == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
    )
    LaunchedEffect(Unit) {
        store.registry.put(
            "${SECTION_BASE}:${FEE_PER_HOUR}",
            newFeePerHour
                .map { it?.chainify() }
        )
    }

    val newHoursPerWeek = singleInputContainer(
        containerModifier = selectableContainerModifier,
        label = "Hours per week",
        value = hoursPerWeek.resolve().format(decimalCount = 0),
        clearButtonVisible = true,
        selected = selectedIndex == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
    )

    LaunchedEffect(newHoursPerWeek) {
        store.registry.put(
            "${SECTION_BASE}:${HOURS_PER_WEEK}",
            newHoursPerWeek
                .map { it?.chainify() }
        )
    }

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