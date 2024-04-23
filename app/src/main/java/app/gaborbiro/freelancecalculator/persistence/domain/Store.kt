package app.gaborbiro.freelancecalculator.persistence.domain

import app.gaborbiro.freelancecalculator.ui.model.ExchangeRateUIModel
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


interface Store {

    var selectedIndex: Flow<Int?>

    var daysPerWeek: Flow<Double?>

    var fromCurrency: Flow<String?>

    var toCurrency: Flow<String?>

    var feePerHour: Flow<ArithmeticChain?>

    val sectionExpander: MapPrefsDelegate<Boolean>

    val exchangeRates: MapPrefsDelegate<ExchangeRateUIModel>

    val registry: MapPrefsDelegate<ArithmeticChain>

    companion object {
        fun dummyImplementation(
            selectedIndex: Int = 2,
            daysPerWeek: Double = 5.0,
            fromCurrency: String = "USD",
            toCurrency: String = "GBP",
        ) = object : Store {
            override var selectedIndex: Flow<Int?> = flowOf(selectedIndex)
            override var daysPerWeek: Flow<Double?> = flowOf(daysPerWeek)
            override var fromCurrency: Flow<String?> = flowOf(fromCurrency)
            override var toCurrency: Flow<String?> = flowOf(toCurrency)
            override var feePerHour: Flow<ArithmeticChain?> = flowOf(0.0.chainify())
            override val sectionExpander: MapPrefsDelegate<Boolean> =
                MapPrefsDelegate.dummyImplementation()
            override val exchangeRates: MapPrefsDelegate<ExchangeRateUIModel> =
                MapPrefsDelegate.dummyImplementation()
            override val registry: MapPrefsDelegate<ArithmeticChain> =
                MapPrefsDelegate.dummyImplementation()
        }

        const val SECTION_ID_BASE = "base"
        const val DATA_ID_FEE_PER_HOUR = "feePerHour"
        const val DATA_ID_HOURS_PER_WEEK = "hoursPerWeek"
        const val DATA_ID_MONEY_PER_WEEK = "moneyPerWeek"
        const val DATA_ID_FEE = "fee"
    }
}
