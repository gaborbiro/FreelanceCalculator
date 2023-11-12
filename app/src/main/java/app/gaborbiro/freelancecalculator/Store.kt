package app.gaborbiro.freelancecalculator

import android.content.Context
import app.gaborbiro.freelancecalculator.util.StoreBase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import java.math.BigDecimal


interface Store {
    var feePerHour: Flow<BigDecimal?>

    var hoursPerWeek: Flow<BigDecimal?>

    var daysPerWeek: Flow<BigDecimal?>

    companion object {
        val DUMMY_IMPL = object : Store {
            override var feePerHour: Flow<BigDecimal?> = emptyFlow()
            override var hoursPerWeek: Flow<BigDecimal?> = emptyFlow()
            override var daysPerWeek: Flow<BigDecimal?> = emptyFlow()
        }
    }
}

class StoreImpl(context: Context, scope: CoroutineScope) : StoreBase(context, scope), Store {

    override var feePerHour: Flow<BigDecimal?> by bigDecimalDelegate("FEE_PER_HOUR")

    override var hoursPerWeek: Flow<BigDecimal?> by bigDecimalDelegate("HOURS_PER_WEEK")

    override var daysPerWeek: Flow<BigDecimal?> by bigDecimalDelegate("DAYS_PER_WEEK")
}
