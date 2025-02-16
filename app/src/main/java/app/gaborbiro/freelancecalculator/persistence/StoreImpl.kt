package app.gaborbiro.freelancecalculator.persistence

import android.content.Context
import app.gaborbiro.freelancecalculator.persistence.domain.MapPrefsDelegate
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.model.ExchangeRateUIModel
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

internal open class StoreImpl(context: Context, scope: CoroutineScope) : StoreBase(context, scope),
    Store {

    override var selectedIndex: MutableStateFlow<Int?> = intDelegate("SELECTED_INDEX")

    override var daysPerWeek: MutableStateFlow<Double?> = doubleDelegate("DAYS_PER_WEEK")

    override val sectionExpander: MapPrefsDelegate<Boolean> = booleanMapPrefsDelegate("SECTION_EXPANDED")

    override val currencySelections: MapPrefsDelegate<Pair<String?, String?>> =
        gsonSerializableMapPrefsDelegate("CURRENCIES")

    override val exchangeRates: MapPrefsDelegate<ExchangeRateUIModel> =
        gsonSerializableMapPrefsDelegate("EXCHANGE_RATES")

    override val registry: MapPrefsDelegate<ArithmeticChain> =
        gsonSerializableMapPrefsDelegate("REGISTRY")
}