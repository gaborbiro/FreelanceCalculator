package app.gaborbiro.freelancecalculator

import android.content.Context
import app.gaborbiro.freelancecalculator.util.StoreBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal


interface Store {
    var feePerHour: Flow<BigDecimal?>

    var hoursPerWeek: Flow<BigDecimal?>

    var daysPerWeek: Flow<BigDecimal?>

    var fee: Flow<BigDecimal?>

    companion object {
        fun dummyImplementation(
            feePerHour: BigDecimal = BigDecimal(80),
            hoursPerWeek: BigDecimal = BigDecimal(30),
            daysPerWeek: BigDecimal = BigDecimal(5),
            fee: BigDecimal = BigDecimal(8),
        ) = object : Store {
            override var feePerHour: Flow<BigDecimal?> = flowOf(feePerHour)
            override var hoursPerWeek: Flow<BigDecimal?> = flowOf(hoursPerWeek)
            override var daysPerWeek: Flow<BigDecimal?> = flowOf(daysPerWeek)
            override var fee: Flow<BigDecimal?> = flowOf(fee)
        }
    }
}

class StoreImpl(context: Context, scope: CoroutineScope) : StoreBase(context, scope), Store {

    override var feePerHour: Flow<BigDecimal?> by bigDecimalDelegate("FEE_PER_HOUR")

    override var hoursPerWeek: Flow<BigDecimal?> by bigDecimalDelegate("HOURS_PER_WEEK")

    override var daysPerWeek: Flow<BigDecimal?> by bigDecimalDelegate("DAYS_PER_WEEK")

    override var fee: Flow<BigDecimal?> by bigDecimalDelegate("FEE")
}
