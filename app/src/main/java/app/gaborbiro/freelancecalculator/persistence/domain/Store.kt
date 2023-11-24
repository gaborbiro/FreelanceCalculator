package app.gaborbiro.freelancecalculator.persistence.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import java.math.BigDecimal


interface Store {

    var feePerHour: Flow<Double?>

    var hoursPerWeek: Flow<Double?>

    var daysPerWeek: Flow<Double?>

    var fee: Flow<Double?>

    var fromCurrency: Flow<String?>

    var toCurrency: Flow<String?>

    fun sectionExpander(): TypedSubStore<Boolean>

    companion object {
        fun dummyImplementation(
            feePerHour: Double = 80.0,
            hoursPerWeek: Double = 30.0,
            daysPerWeek: Double = 5.0,
            fee: Double = 8.0,
            fromCurrency: String = "USD",
            toCurrency: String = "GBP",
            sectionExpanded: Map<String, Boolean> = emptyMap(),
        ) = object : Store {
            override var feePerHour: Flow<Double?> = flowOf(feePerHour)
            override var hoursPerWeek: Flow<Double?> = flowOf(hoursPerWeek)
            override var daysPerWeek: Flow<Double?> = flowOf(daysPerWeek)
            override var fee: Flow<Double?> = flowOf(fee)
            override var fromCurrency: Flow<String?> = flowOf(fromCurrency)
            override var toCurrency: Flow<String?> = flowOf(toCurrency)
            override fun sectionExpander(): TypedSubStore<Boolean> =
                TypedSubStore.dummyImplementation(sectionExpanded)
        }
    }
}
