package app.gaborbiro.freelancecalculator.persistence.domain

import app.gaborbiro.freelancecalculator.ui.model.ExchangeRateUIModel
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


interface Store {

    var selectedIndex: Flow<Int?>

    var daysPerWeek: Flow<Double?>

    val sectionExpander: MapPrefsDelegate<Boolean>

    val currencies: MapPrefsDelegate<Pair<String?, String?>>

    val exchangeRates: MapPrefsDelegate<ExchangeRateUIModel>

    val registry: MapPrefsDelegate<ArithmeticChain>

    companion object {
        fun dummyImplementation(
            selectedIndex: Int = 2,
            daysPerWeek: Double = 5.0,
        ) = object : Store {
            override var selectedIndex: Flow<Int?> = flowOf(selectedIndex)
            override var daysPerWeek: Flow<Double?> = flowOf(daysPerWeek)
            override val sectionExpander: MapPrefsDelegate<Boolean> =
                MapPrefsDelegate.dummyImplementation()
            override val currencies: MapPrefsDelegate<Pair<String?, String?>> =
                MapPrefsDelegate.dummyImplementation()
            override val exchangeRates: MapPrefsDelegate<ExchangeRateUIModel> =
                MapPrefsDelegate.dummyImplementation()
            override val registry: MapPrefsDelegate<ArithmeticChain> =
                MapPrefsDelegate.dummyImplementation()
        }

        const val SECTION_BASE = "base"
        const val SECTION_PT = "pt"
        const val SECTION_UK = "uk"
        const val SECTION_TIMEOFF = "time_off"
        const val SECTION_CURRENCY1 = "currency1"
        const val SUB_SECTION_TAX = "tax"
        const val SUB_SECTION_CURRENCY = "currency"
        const val FEE_PER_HOUR = "feePerHour"
        const val HOURS_PER_WEEK = "hoursPerWeek"
        const val MONEY_PER_WEEK = "moneyPerWeek"
        const val TYPE_FEE = "fee"
    }
}
