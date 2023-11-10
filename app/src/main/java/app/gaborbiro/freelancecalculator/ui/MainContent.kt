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
import app.gaborbiro.freelancecalculator.formatWithCommas
import app.gaborbiro.freelancecalculator.parse
import app.gaborbiro.freelancecalculator.times
import app.gaborbiro.freelancecalculator.ui.theme.FreelanceCalculatorTheme
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE
import java.math.BigDecimal


private lateinit var feePerHourStr: MutableState<String?>
private lateinit var hoursPerWeekStr: MutableState<String?>
private lateinit var daysPerWeekStr: MutableState<String?>
private lateinit var moneyPerWeek: MutableState<BigDecimal?>

@Composable
fun MainContent() {
    val selectedIndex = rememberSaveable { mutableStateOf(3) }
    var indexCounter = -1

    feePerHourStr = rememberSaveable { mutableStateOf(null) }
    hoursPerWeekStr = rememberSaveable { mutableStateOf(null) }
    daysPerWeekStr = rememberSaveable { mutableStateOf(null) }
    moneyPerWeek = rememberSaveable(feePerHourStr.value, hoursPerWeekStr.value) {
        val feePerHour = feePerHourStr.value.parse()
        val hoursPerWeek = hoursPerWeekStr.value.parse()
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
        value = feePerHourStr.value ?: "",
        decimalCount = 2,
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            feePerHourStr.value = value.formatWithCommas()
        },
    )
    SingleInputContainer(
        containerModifier = containerModifier,
        label = "Hours per week",
        value = hoursPerWeekStr.value ?: "",
        decimalCount = 2,
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            hoursPerWeekStr.value = value.formatWithCommas()
        },
    )
    SingleInputContainer(
        containerModifier = containerModifier,
        label = "Days per week",
        value = daysPerWeekStr.value ?: "",
        decimalCount = 2,
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            daysPerWeekStr.value = value.formatWithCommas()
        },
    )
    MoneyOverTimeSection(
        containerModifier = containerModifier,
        selected = selectedIndex.value == ++indexCounter,
        moneyPerWeek = moneyPerWeek.value,
        daysPerWeek = daysPerWeekStr.value.parse(),
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