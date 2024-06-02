package app.gaborbiro.freelancecalculator.ui.sections

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.view.MoneyBreakdown
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class SectionBuilder(
    private val inputId: String,
    private val sectionId: String,
    private val title: String,
    private val store: Store,
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
    ): Flow<ArithmeticChain?> {
        val moneyPerWeek by store
            .registry["$inputId:${Store.MONEY_PER_WEEK}"]
            .collectAsState(initial = null)

        val outputMoneyPerWeek = remember(moneyPerWeek) {
            val output = output(moneyPerWeek)
            store.registry["${sectionId}:${Store.MONEY_PER_WEEK}"] = output
            output
        }

        return scope.MoneyBreakdown(
            collapseId = "$sectionId:collapse",
            title = "$title ($inputId->$sectionId)",
            store = store,
            moneyPerWeek = outputMoneyPerWeek,
            extraContent = extraContent,
        )
            .map {
                reverse(it)
            }
    }
}

open class Operand(storeID: String)

class Fee(storeID: String) : Operand(storeID)
class ExchangeRate(storeID: String) : Operand(storeID)