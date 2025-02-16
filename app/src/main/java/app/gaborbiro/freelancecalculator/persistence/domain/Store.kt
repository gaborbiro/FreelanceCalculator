package app.gaborbiro.freelancecalculator.persistence.domain

import app.gaborbiro.freelancecalculator.ui.model.ExchangeRateUIModel
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow


interface Store {

    var selectedIndex: MutableStateFlow<Int>

    var daysPerWeek: MutableStateFlow<Double?>

    val sectionExpander: MapPrefsDelegate<Boolean>

    val currencySelections: MapPrefsDelegate<Pair<String?, String?>>

    val exchangeRates: MapPrefsDelegate<ExchangeRateUIModel>

    val registry: MapPrefsDelegate<ArithmeticChain>

    companion object {
        fun dummyImplementation(
            selectedIndex: Int = 2,
            daysPerWeek: Double = 5.0,
        ) = object : Store {
            override var selectedIndex: MutableStateFlow<Int> = MutableStateFlow(selectedIndex)
            override var daysPerWeek: MutableStateFlow<Double?> = MutableStateFlow(daysPerWeek)
            override val sectionExpander: MapPrefsDelegate<Boolean> =
                MapPrefsDelegate.dummyImplementation()
            override val currencySelections: MapPrefsDelegate<Pair<String?, String?>> =
                MapPrefsDelegate.dummyImplementation()
            override val exchangeRates: MapPrefsDelegate<ExchangeRateUIModel> =
                MapPrefsDelegate.dummyImplementation()
            override val registry: MapPrefsDelegate<ArithmeticChain> =
                MapPrefsDelegate.dummyImplementation()
        }

        const val SECTION_GROSS = "gross"
        const val SECTION_UK = "uk"
        const val SECTION_TIMEOFF = "time_off"
        const val SECTION_CURRENCY1 = "currency1"
        const val SECTION_TAX = "tax"
        const val SECTION_CURRENCY2 = "currency2"
        const val FEE_PER_HOUR = "feePerHour"
        const val HOURS_PER_WEEK = "hoursPerWeek"
        const val MONEY_PER_WEEK = "moneyPerWeek"
        const val TYPE_FEE = "fee"
    }
}
