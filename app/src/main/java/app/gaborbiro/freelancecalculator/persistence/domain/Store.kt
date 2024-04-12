package app.gaborbiro.freelancecalculator.persistence.domain

import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


interface Store {

    var selectedIndex: Flow<Int?>

    var feePerHour: Flow<ArithmeticChain?>

    var hoursPerWeek: Flow<ArithmeticChain?>

    var daysPerWeek: Flow<Double?>

    var fee: Flow<Double?>

    var fromCurrency: Flow<String?>

    var toCurrency: Flow<String?>

    fun sectionExpander(): MapDelegate<Boolean, Boolean>

    fun registry(): MapDelegate<ArithmeticChain, String>

    companion object {
        fun dummyImplementation(
            selectedIndex: Int = 2,
            feePerHour: Double = 80.0,
            hoursPerWeek: Double = 30.0,
            daysPerWeek: Double = 5.0,
            fee: Double = 8.0,
            fromCurrency: String = "USD",
            toCurrency: String = "GBP",
        ) = object : Store {
            override var selectedIndex: Flow<Int?> = flowOf(selectedIndex)
            override var feePerHour: Flow<ArithmeticChain?> = flowOf(feePerHour.chainify())
            override var hoursPerWeek: Flow<ArithmeticChain?> = flowOf(hoursPerWeek.chainify())
            override var daysPerWeek: Flow<Double?> = flowOf(daysPerWeek)
            override var fee: Flow<Double?> = flowOf(fee)
            override var fromCurrency: Flow<String?> = flowOf(fromCurrency)
            override var toCurrency: Flow<String?> = flowOf(toCurrency)
            override fun sectionExpander() = MapDelegate.dummyImplementation<Boolean, Boolean>()
            override fun registry() = MapDelegate.dummyImplementation<ArithmeticChain, String>()
        }
    }
}
