package app.gaborbiro.freelancecalculator.persistence

import android.content.Context
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class StoreImpl(context: Context, scope: CoroutineScope) : StoreBase(context, scope), Store {

    override var selectedIndex: Flow<Int?> by intDelegate("SELECTED_INDEX")

    override var feePerHour: Flow<ArithmeticChain?> by arithmeticChainDelegate("FEE_PER_HOUR")

    override var hoursPerWeek: Flow<ArithmeticChain?> by arithmeticChainDelegate("HOURS_PER_WEEK")

    override var daysPerWeek: Flow<Double?> by doubleDelegate("DAYS_PER_WEEK")

    override var fee: Flow<Double?> by doubleDelegate("FEE")

    override var fromCurrency: Flow<String?> by stringDelegate("FROM_CURRENCY")

    override var toCurrency: Flow<String?> by stringDelegate("TO_CURRENCY")

    override fun sectionExpander() = booleanSubDelegate("SECTION_EXPANDED")
}