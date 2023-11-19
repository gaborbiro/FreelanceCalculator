package app.gaborbiro.freelancecalculator

import android.content.Context
import app.gaborbiro.freelancecalculator.util.StoreBase
import app.gaborbiro.freelancecalculator.util.TypedSubStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal


interface Store {

    var feePerHour: Flow<BigDecimal?>

    var hoursPerWeek: Flow<BigDecimal?>

    var daysPerWeek: Flow<BigDecimal?>

    var fee: Flow<BigDecimal?>

    var fromCurrency: Flow<String?>

    var toCurrency: Flow<String?>

    var rate: Flow<BigDecimal?>

    fun sectionExpander(): TypedSubStore<Boolean>

    companion object {
        fun dummyImplementation(
            feePerHour: Int = 80,
            hoursPerWeek: Int = 30,
            daysPerWeek: Int = 5,
            fee: Int = 8,
            fromCurrency: String = "USD",
            toCurrency: String = "GBP",
            rate: Int = 1,
            sectionExpanded: Map<String, Boolean> = emptyMap(),
        ) = object : Store {
            override var feePerHour: Flow<BigDecimal?> = flowOf(BigDecimal(feePerHour))
            override var hoursPerWeek: Flow<BigDecimal?> = flowOf(BigDecimal(hoursPerWeek))
            override var daysPerWeek: Flow<BigDecimal?> = flowOf(BigDecimal(daysPerWeek))
            override var fee: Flow<BigDecimal?> = flowOf(BigDecimal(fee))
            override var fromCurrency: Flow<String?> = flowOf(fromCurrency)
            override var toCurrency: Flow<String?> = flowOf(toCurrency)
            override var rate: Flow<BigDecimal?> = flowOf(BigDecimal(rate))
            override fun sectionExpander(): TypedSubStore<Boolean> =
                TypedSubStore.dummyImplementation(sectionExpanded)
        }
    }
}

class StoreImpl(context: Context, scope: CoroutineScope) : StoreBase(context, scope), Store {

    override var feePerHour: Flow<BigDecimal?> by bigDecimalDelegate("FEE_PER_HOUR")

    override var hoursPerWeek: Flow<BigDecimal?> by bigDecimalDelegate("HOURS_PER_WEEK")

    override var daysPerWeek: Flow<BigDecimal?> by bigDecimalDelegate("DAYS_PER_WEEK")

    override var fee: Flow<BigDecimal?> by bigDecimalDelegate("FEE")

    override var fromCurrency: Flow<String?> by stringDelegate("FROM_CURRENCY")

    override var toCurrency: Flow<String?> by stringDelegate("TO_CURRENCY")

    override var rate: Flow<BigDecimal?> by bigDecimalDelegate("RATE")

    override fun sectionExpander() = booleanSubDelegate("SECTION_EXPANDED")
}
