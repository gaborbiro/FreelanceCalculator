package app.gaborbiro.freelancecalculator.ui.sections.daysperweek

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.view.FocusPinnedInputField
import app.gaborbiro.freelancecalculator.util.hide.format
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DaysPerWeekSection(
    modifier: Modifier,
    store: Store,
) {
    val daysPerWeek by store.daysPerWeek.collectAsState(initial = null)

    FocusPinnedInputField(
        modifier = modifier,
        label = "Days per week",
        value = daysPerWeek.format(decimalCount = 0),
        outlined = false,
        clearButtonVisible = true,
    ) { newDaysPerWeek ->
        store.daysPerWeek = flowOf(newDaysPerWeek)
    }
}