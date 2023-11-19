package app.gaborbiro.freelancecalculator.persistence.domain

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
