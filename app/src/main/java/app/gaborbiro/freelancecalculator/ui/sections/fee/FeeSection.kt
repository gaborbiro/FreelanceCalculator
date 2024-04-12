package app.gaborbiro.freelancecalculator.ui.sections.fee

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.view.FocusPinnedInputField
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.FeeSection(
    store: Store,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    val feePerHour by store.feePerHour.collectAsState(initial = null)
    val hoursPerWeek by store.hoursPerWeek.collectAsState(initial = null)
    val fee by store.fee.collectAsState(initial = null)

    val feeMultiplier: Double? = remember(fee) {
        fee?.let { 1.0 - (it / 100.0) }
    }

    MoneyOverTime(
        sectionId = "fee",
        title = "Fee / Reduction",
        store = store,
        moneyPerWeek = feePerHour * hoursPerWeek * feeMultiplier,
        extraContent = {
            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "%",
                value = fee.format(decimalCount = 2),
                outlined = false,
                clearButtonVisible = true,
                onValueChange = { fee ->
                    store.fee = flowOf(fee)
                },
            )
        }
    ) { newValue: ArithmeticChain? ->
        onMoneyPerWeekChange(newValue / feeMultiplier)
    }
}