package app.gaborbiro.freelancecalculator.persistence

import android.content.Context
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

class StoreImpl(context: Context, scope: CoroutineScope) : StoreBase(context, scope), Store {

    override var feePerHour: Flow<Double?> by doubleDelegate("FEE_PER_HOUR")

    override var hoursPerWeek: Flow<Double?> by doubleDelegate("HOURS_PER_WEEK")

    override var daysPerWeek: Flow<Double?> by doubleDelegate("DAYS_PER_WEEK")

    override var fee: Flow<Double?> by doubleDelegate("FEE")

    override var fromCurrency: Flow<String?> by stringDelegate("FROM_CURRENCY")

    override var toCurrency: Flow<String?> by stringDelegate("TO_CURRENCY")

    override fun sectionExpander() = booleanSubDelegate("SECTION_EXPANDED")
}