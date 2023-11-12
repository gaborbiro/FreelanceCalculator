@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)

package app.gaborbiro.freelancecalculator.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.Store
import app.gaborbiro.freelancecalculator.currency.CurrencyRepository
import app.gaborbiro.freelancecalculator.div
import app.gaborbiro.freelancecalculator.format
import app.gaborbiro.freelancecalculator.times
import app.gaborbiro.freelancecalculator.ui.theme.FreelanceCalculatorTheme
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.math.BigDecimal


@Composable
fun MainContent(
    store: Store,
    currencyRepository: CurrencyRepository,
) {
    val selectedIndex = rememberSaveable { mutableStateOf(2) }
    var indexCounter = -1

    fun selectionUpdater(indexCounter: Int): () -> Unit = {
        selectedIndex.value = indexCounter
    }

    val containerModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = MARGIN_LARGE)


    val feePerHour: State<BigDecimal?> = store.feePerHour.collectAsState(initial = null)

    val hoursPerWeek: State<BigDecimal?> = store.hoursPerWeek.collectAsState(initial = null)

    var moneyPerWeek: BigDecimal? by rememberSaveable(feePerHour.value, hoursPerWeek.value) {
        mutableStateOf(feePerHour.value * hoursPerWeek.value)
    }

    @Suppress("KotlinConstantConditions")
    SingleInputContainer(
        containerModifier = containerModifier,
        label = "Fee per hour",
        value = feePerHour.value.format(decimalCount = 2),
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            CoroutineScope(Dispatchers.IO).launch {
                store.feePerHour = flowOf(value)
            }
        },
    )

    SingleInputContainer(
        containerModifier = containerModifier,
        label = "Hours per week",
        value = hoursPerWeek.value.format(decimalCount = 0),
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            CoroutineScope(Dispatchers.IO).launch {
                store.hoursPerWeek = flowOf(value)
            }
        },
    )

    MoneyOverTimeSection(
        containerModifier = containerModifier,
        selected = selectedIndex.value == ++indexCounter,
        moneyPerWeek = moneyPerWeek,
        store = store,
        currencyRepository = currencyRepository,
        onSelected = selectionUpdater(indexCounter),
        onMoneyPerWeekChange = {
            moneyPerWeek = it
            CoroutineScope(Dispatchers.IO).launch {
                when (selectedIndex.value) {
                    0 -> store.feePerHour = flowOf(moneyPerWeek / hoursPerWeek.value)
                    1 -> store.hoursPerWeek = flowOf(moneyPerWeek / feePerHour.value)
                }
            }
        },
    )
}

@Preview
@Composable
private fun MainContentPreview() {
    FreelanceCalculatorTheme(dynamicColor = false) {
        Surface(
            modifier = Modifier
                .fillMaxSize(),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = MARGIN_LARGE),
                verticalArrangement = Arrangement.spacedBy(MARGIN_LARGE),
            ) {
                MainContent(
                    Store.dummyImplementation(),
                    CurrencyRepository.getDummyImplementation()
                )
            }
        }
    }
}