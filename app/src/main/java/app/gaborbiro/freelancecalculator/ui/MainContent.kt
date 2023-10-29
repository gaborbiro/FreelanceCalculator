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
import app.gaborbiro.freelancecalculator.formatWithCommas
import app.gaborbiro.freelancecalculator.parse
import app.gaborbiro.freelancecalculator.times
import app.gaborbiro.freelancecalculator.ui.theme.FreelanceCalculatorTheme
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE


private lateinit var feePerHour: MutableState<String?>
private lateinit var hoursPerWeek: MutableState<String?>
private lateinit var daysPerWeek: MutableState<String?>
private lateinit var moneyPerWeek: MutableState<String?>

@Composable
fun MainContent() {
    val selectedIndex = rememberSaveable { mutableStateOf(3) }
    var indexCounter = -1

    feePerHour = rememberSaveable { mutableStateOf(null) }
    hoursPerWeek = rememberSaveable { mutableStateOf(null) }
    daysPerWeek = rememberSaveable { mutableStateOf(null) }
    moneyPerWeek = rememberSaveable(feePerHour.value, hoursPerWeek.value) {
        val feePerHour = feePerHour.value.parse()
        val hoursPerWeek = hoursPerWeek.value.parse()
        val moneyPerWeek = (feePerHour * hoursPerWeek).format(decimalCount = 2)
        mutableStateOf(moneyPerWeek)
    }

    fun selectionUpdater(indexCounter: Int): () -> Unit = {
        selectedIndex.value = indexCounter
    }

    val containerModifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = MARGIN_LARGE)

    SingleInputContainer(
        containerModifier = containerModifier,
        label = "Fee per hour",
        value = feePerHour.value ?: "",
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            feePerHour.value = value.formatWithCommas()
        },
    )
    SingleInputContainer(
        containerModifier = containerModifier,
        label = "Hours per week",
        value = hoursPerWeek.value ?: "",
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            hoursPerWeek.value = value.formatWithCommas()
        },
    )
    SingleInputContainer(
        containerModifier = containerModifier,
        label = "Days per week",
        value = daysPerWeek.value ?: "",
        clearButtonVisible = true,
        selected = selectedIndex.value == ++indexCounter,
        onSelected = selectionUpdater(indexCounter),
        onValueChange = { value ->
            daysPerWeek.value = value.formatWithCommas()
        },
    )
    MoneyOverTime(
        containerModifier = containerModifier,
        selected = selectedIndex.value == ++indexCounter,
        moneyPerWeek = moneyPerWeek.value.parse(),
        daysPerWeek = daysPerWeek.value.parse(),
        onSelected = selectionUpdater(indexCounter),
        onMoneyPerWeekChange = {
            moneyPerWeek.value = it.format(decimalCount = 2)
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