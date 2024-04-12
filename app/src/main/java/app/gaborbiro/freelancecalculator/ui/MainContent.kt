@file:OptIn(ExperimentalMaterial3Api::class)

package app.gaborbiro.freelancecalculator.ui

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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.theme.FreelanceCalculatorTheme
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.SelectableContainer
import app.gaborbiro.freelancecalculator.ui.view.SingleInputContainer
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.resolve
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch


@Composable
fun CalculatorContent(
    store: Store,
    currencyRepository: CurrencyRepository,
) {
    val selectedIndex: State<Int?> = store.selectedIndex.collectAsState(initial = null)
    var indexCounter = -1

    fun selectionUpdater(indexCounter: Int): () -> Unit = {
        store.selectedIndex = flowOf(indexCounter)
    }

    val selectableContainerModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = PADDING_LARGE)


    val feePerHour: State<ArithmeticChain?> = store.feePerHour.collectAsState(initial = null)

    val hoursPerWeek: State<ArithmeticChain?> = store.hoursPerWeek.collectAsState(initial = null)

    val moneyPerWeek: ArithmeticChain? by remember(feePerHour.value, hoursPerWeek.value) {
        mutableStateOf(feePerHour.value * hoursPerWeek.value)
    }

    @Suppress("KotlinConstantConditions")
    SingleInputContainer(
        containerModifier = selectableContainerModifier,
        label = "Fee per hour",
        value = feePerHour.value.resolve().format(decimalCount = 2),
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { newValue: Double? ->
            CoroutineScope(Dispatchers.IO).launch {
                if (selectedIndex.value == 1) {
                    store.hoursPerWeek = flowOf((moneyPerWeek / newValue)?.simplify())
                }
                store.feePerHour = flowOf(newValue.chainify())
            }
        },
    )

    SingleInputContainer(
        containerModifier = selectableContainerModifier,
        label = "Hours per week",
        value = hoursPerWeek.value.resolve().format(decimalCount = 0),
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { newValue: Double? ->
            CoroutineScope(Dispatchers.IO).launch {
                if (selectedIndex.value == 0) {
                    store.feePerHour = flowOf((moneyPerWeek / newValue)?.simplify())
                }
                store.hoursPerWeek = flowOf(newValue.chainify())
            }
        },
    )

    SelectableContainer(
        modifier = selectableContainerModifier,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
    ) { modifier ->
        Column(
            modifier = modifier
                .padding(bottom = PADDING_LARGE)
        ) {
            ResultsSection(
                store = store,
                currencyRepository = currencyRepository,
                onMoneyPerWeekChange = { newValue: ArithmeticChain? ->
                    CoroutineScope(Dispatchers.IO).launch {
                        when (selectedIndex.value) {
                            0 -> store.feePerHour =
                                flowOf((newValue / hoursPerWeek.value)?.simplify())

                            1 -> store.hoursPerWeek =
                                flowOf((newValue / feePerHour.value)?.simplify())
                        }
                    }
                },
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