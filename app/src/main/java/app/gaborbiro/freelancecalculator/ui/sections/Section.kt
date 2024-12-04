package app.gaborbiro.freelancecalculator.ui.sections

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.view.moneyBreakdown
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine


class SectionBuilder(
    private val inputId: String,
    private val sectionId: String,
    private val title: String,
    private val store: Store,
) {

    @OptIn(ExperimentalMaterial3Api::class)
    @ExperimentalMaterial3Api
    @Composable
        /**
         * Simple section, that takes an input flow, applies the specified multiplier and displays
         * it as a 4-way breakdown.
         *
         * @return reverse flow generated by the user changing any of the values in the 4-way breakdown.
         */
    fun section(
        scope: ColumnScope,
        extraContent: (@Composable ColumnScope.() -> Unit)? = null,
        getMultiplier: Store.() -> Flow<Double?>,
    ): Flow<ArithmeticChain?> {
        val multiplier: Flow<Double?> = remember { getMultiplier(store) }

        val displayedMoneyPerWeek = remember {
            combine(
                store.registry["$inputId:${Store.MONEY_PER_WEEK}"],
                multiplier
            ) { f1, f2 -> (f1 * f2) }
        }
        store.registry["$sectionId:${Store.MONEY_PER_WEEK}"] = displayedMoneyPerWeek

        val moneyPerWeek by displayedMoneyPerWeek.collectAsState(initial = null)

        return scope.moneyBreakdown(
            collapseId = "$sectionId:collapse",
            title = "$title ($inputId->$sectionId)",
            store = store,
            moneyPerWeek = moneyPerWeek,
            extraContent = extraContent,
        )
            .combine(multiplier) { f1: ArithmeticChain?, f2: Double? ->
                f1 / f2
            }
    }
}
