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
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.DATA_ID_FEE
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.DATA_ID_MONEY_PER_WEEK
import app.gaborbiro.freelancecalculator.ui.view.FocusPinnedInputField
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.resolve
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.flow.combine

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FlowOperatorInvokedInComposition")
@Composable
fun ColumnScope.FeeSection(
    inputId: String,
    sectionId: String,
    title: String,
    store: Store,
    onMoneyPerWeekChanged: (ArithmeticChain?) -> Unit,
) {
    val moneyPerWeek by store
        .registry["${inputId}:${DATA_ID_MONEY_PER_WEEK}"]
        .collectAsState(initial = null)
    val fee by store
        .registry["${sectionId}:${DATA_ID_FEE}"]
        .collectAsState(initial = null)
    val feeMultiplier = remember(fee) {
        fee?.resolve()?.toDouble()?.let { 1.0 - (it / 100.0) }
    }
    val outputMoneyPerWeek = moneyPerWeek * feeMultiplier
    store.registry["${sectionId}:${DATA_ID_MONEY_PER_WEEK}"] = outputMoneyPerWeek

    MoneyOverTime(
        collapseId = "${sectionId}:fee",
        title = title,
        store = store,
        moneyPerWeek = outputMoneyPerWeek,
        extraContent = {
            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "%",
                value = fee.resolve().format(decimalCount = 2),
                outlined = false,
                clearButtonVisible = true,
                onValueChange = { fee: Double? ->
                    store.registry["${sectionId}:${DATA_ID_FEE}"] = fee.chainify()
                },
            )
        }
    ) { newValue: ArithmeticChain? ->
        val newMoneyPerWeek = newValue / feeMultiplier
        onMoneyPerWeekChanged(newMoneyPerWeek)
    }
}