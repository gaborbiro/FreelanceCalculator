package app.gaborbiro.freelancecalculator.ui.sections

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.sections.fee.toFeeMultiplier
import app.gaborbiro.freelancecalculator.ui.view.MoneyBreakdown
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.times
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
    fun Section(
        scope: ColumnScope,
        extraContent: (@Composable ColumnScope.() -> Unit)? = null,
        multiplier: Multiplier,
    ): Flow<ArithmeticChain?> {
        val moneyPerWeek by store
            .registry["$inputId:${Store.MONEY_PER_WEEK}"]
            .collectAsState(initial = null)

        val multiplier by multiplier
            .getFromStore(store)
            .collectAsState(initial = null)

        val outputMoneyPerWeek = remember(moneyPerWeek) {
            val output = moneyPerWeek * multiplier
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
                it / multiplier
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