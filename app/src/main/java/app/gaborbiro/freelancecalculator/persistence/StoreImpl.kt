package app.gaborbiro.freelancecalculator.persistence

import android.content.Context
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import java.math.BigDecimal

class StoreImpl(context: Context, scope: CoroutineScope) : StoreBase(context, scope), Store {

    override var feePerHour: Flow<BigDecimal?> by bigDecimalDelegate("FEE_PER_HOUR")

    override var hoursPerWeek: Flow<BigDecimal?> by bigDecimalDelegate("HOURS_PER_WEEK")

    override var daysPerWeek: Flow<BigDecimal?> by bigDecimalDelegate("DAYS_PER_WEEK")

    override var fee: Flow<BigDecimal?> by bigDecimalDelegate("FEE")

    override var fromCurrency: Flow<String?> by stringDelegate("FROM_CURRENCY")

    override var toCurrency: Flow<String?> by stringDelegate("TO_CURRENCY")

    override fun sectionExpander() = booleanSubDelegate("SECTION_EXPANDED")
}