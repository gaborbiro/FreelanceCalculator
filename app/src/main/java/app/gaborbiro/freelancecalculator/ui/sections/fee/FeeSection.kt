package app.gaborbiro.freelancecalculator.ui.sections.fee

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.TYPE_FEE
import app.gaborbiro.freelancecalculator.ui.sections.Multiplier
import app.gaborbiro.freelancecalculator.ui.sections.SectionBuilder
import app.gaborbiro.freelancecalculator.ui.view.focusPinnedInputField
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.resolve
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FlowOperatorInvokedInComposition")
@Composable
fun ColumnScope.feeSection(
    inputId: String,
    sectionId: String,
    title: String,
    store: Store,
): Flow<ArithmeticChain?> {
    val fee by store
        .registry["$sectionId:$TYPE_FEE"]
        .collectAsState(initial = null)

    val feeStr = remember(fee) { fee.resolve().format(decimalCount = 2) }

    val builder = remember {
        SectionBuilder(inputId, sectionId, title, store)
    }

    return builder.section(
        this,
        extraContent = {
            val newFee = focusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize(),
                label = "%",
                value = feeStr,
                outlined = false,
                clearButtonVisible = true,
            )
                .map { it?.chainify() }
            LaunchedEffect(Unit) {
                newFee
                    .collect {
                        store.registry["${sectionId}:${TYPE_FEE}"] = it
                    }
            }
        },
        multiplier = Multiplier.Fee("$sectionId:$TYPE_FEE"),
    )
}

fun ArithmeticChain?.toFeeMultiplier() = resolve()
    ?.toDouble()
    ?.let { 1.0 - (it / 100.0) }
    ?: 1.0