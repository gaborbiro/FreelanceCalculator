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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.format
import app.gaborbiro.freelancecalculator.strictParse
import app.gaborbiro.freelancecalculator.times
import app.gaborbiro.freelancecalculator.ui.theme.FreelanceCalculatorTheme
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE
import java.math.BigDecimal


private lateinit var feePerHour: MutableState<BigDecimal?>
private lateinit var hoursPerWeek: MutableState<BigDecimal?>
private lateinit var daysPerWeek: MutableState<BigDecimal?>
private lateinit var moneyPerWeek: MutableState<BigDecimal?>

@Composable
fun MainContent() {
    val selectedIndex = rememberSaveable { mutableStateOf(3) }
    var indexCounter = -1

    feePerHour = rememberSaveable { mutableStateOf(null) }
    hoursPerWeek = rememberSaveable { mutableStateOf(null) }
    daysPerWeek = rememberSaveable { mutableStateOf(null) }
    moneyPerWeek = rememberSaveable(feePerHour.value, hoursPerWeek.value) {
        val feePerHour = feePerHour.value
        val hoursPerWeek = hoursPerWeek.value
        val moneyPerWeek = (feePerHour * hoursPerWeek)
        mutableStateOf(moneyPerWeek)
    }

    fun selectionUpdater(indexCounter: Int): () -> Unit = {
        selectedIndex.value = indexCounter
    }

    val containerModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = MARGIN_LARGE)

    @Suppress("KotlinConstantConditions")
    SingleInputContainer(
        containerModifier = containerModifier,
        label = "Fee per hour",
        value = feePerHour.value.format(decimalCount = 2),
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            feePerHour.value = value
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
            hoursPerWeek.value = value
        },
    )
    SingleInputContainer(
        containerModifier = containerModifier,
        label = "Days per week",
        value = daysPerWeek.value.format(decimalCount = 0),
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            daysPerWeek.value = value
        },
    )
    MoneyOverTimeSection(
        containerModifier = containerModifier,
        selected = selectedIndex.value == ++indexCounter,
        moneyPerWeek = moneyPerWeek.value,
        daysPerWeek = daysPerWeek.value,
        onSelected = selectionUpdater(indexCounter),
        onMoneyPerWeekChange = {
            moneyPerWeek.value = it
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
                MainContent()
            }
        }
    }
}