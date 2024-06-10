package app.gaborbiro.freelancecalculator.ui.sections

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.sections.fee.toFeeMultiplier
import app.gaborbiro.freelancecalculator.ui.view.MoneyBreakdown
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
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
    fun section(
        scope: ColumnScope,
        extraContent: (@Composable ColumnScope.() -> Unit)? = null,
        multiplier: Multiplier,
    ): Flow<ArithmeticChain?> {
        val multiplier = remember {
            multiplier.getFromStore(store)
        }

        val displayedMoneyPerWeek = remember {
            combine(
                store.registry["$inputId:${Store.MONEY_PER_WEEK}"],
                multiplier
            ) { f1, f2 ->
                (f1 * f2)
                    .also {
                        store.registry["${sectionId}:${Store.MONEY_PER_WEEK}"] = it
                    }
            }
        }

        return scope.MoneyBreakdown(
            collapseId = "$sectionId:collapse",
            title = "$title ($inputId->$sectionId)",
            store = store,
            moneyPerWeek = displayedMoneyPerWeek.collectAsState(initial = null).value,
            extraContent = extraContent,
        )
            .combine(multiplier) { f1, f2 ->
                f1 / f2
            }
    }
}

sealed class Multiplier {

    class Fee(private val storeID: String) : Multiplier() {
        override fun getFromStore(store: Store): Flow<Double?> {
            return store.registry[storeID]
                .map { it.toFeeMultiplier() }
        }
    }

    class ExchangeRate(private val storeID: String) : Multiplier() {
        override fun getFromStore(store: Store): Flow<Double?> {
            return store.exchangeRates[storeID]
                .map { it?.rate }
        }
    }

    abstract fun getFromStore(store: Store): Flow<Double?>
}