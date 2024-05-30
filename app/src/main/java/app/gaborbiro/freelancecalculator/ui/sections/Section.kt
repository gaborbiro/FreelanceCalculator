package app.gaborbiro.freelancecalculator.ui.sections

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.view.MoneyBreakdown
import app.gaborbiro.freelancecalculator.util.ArithmeticChain


class SectionBuilder(
    private val inputId: String,
    private val sectionId: String,
    private val title: String,
    private val store: Store,
    private val onMoneyPerWeekChanged: (newValue: ArithmeticChain?) -> Unit,
) {

    @OptIn(ExperimentalMaterial3Api::class)
    @ExperimentalMaterial3Api
    @Composable
    fun MoneyBreakdown(
        scope: ColumnScope,
        output: (moneyPerWeek: ArithmeticChain?) -> ArithmeticChain?,
        reverse: (moneyPerWeek: ArithmeticChain?) -> ArithmeticChain?,
        extraContent: (@Composable ColumnScope.() -> Unit)? = null,
        operand: Operand,
        externalOperands: List<Operand>,
    ) {
        val moneyPerWeek by store
            .registry["$inputId:${Store.MONEY_PER_WEEK}"]
            .collectAsState(initial = null)

        val outputMoneyPerWeek = output(moneyPerWeek)

        store.registry["${sectionId}:${Store.MONEY_PER_WEEK}"] = outputMoneyPerWeek

        scope.MoneyBreakdown(
            collapseId = "$sectionId:collapse",
            title = "$title ($inputId->$sectionId)",
            store = store,
            moneyPerWeek = outputMoneyPerWeek,
            extraContent = extraContent,
            onMoneyPerWeekChanged = { newValue: ArithmeticChain? ->
                onMoneyPerWeekChanged(reverse(newValue))
            },
        )
    }
}

open class Operand(storeID: String)

class Fee(storeID: String) : Operand(storeID)
class ExchangeRate(storeID: String) : Operand(storeID)