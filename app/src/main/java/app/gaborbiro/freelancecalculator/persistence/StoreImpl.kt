package app.gaborbiro.freelancecalculator.persistence

import android.content.Context
import app.gaborbiro.freelancecalculator.persistence.domain.MapPrefsDelegate
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.ui.model.ExchangeRateUIModel
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow

internal open class StoreImpl(context: Context, scope: CoroutineScope) : StoreBase(context, scope),
    Store {

    override var selectedIndex: Flow<Int?> by intDelegate("SELECTED_INDEX")

    override var daysPerWeek: Flow<Double?> by doubleDelegate("DAYS_PER_WEEK")

    override var feePerHour: Flow<ArithmeticChain?> by gsonSerializablePrefsDelegate("FEE_PER_HOUR")

    override var fromCurrency: Flow<String?> by stringDelegate("FROM_CURRENCY")

    override var toCurrency: Flow<String?> by stringDelegate("TO_CURRENCY")

    override val sectionExpander = booleanMapPrefsDelegate("SECTION_EXPANDED")

    override val exchangeRates: MapPrefsDelegate<ExchangeRateUIModel> =
        gsonSerializableMapPrefsDelegate("EXCHANGE_RATES")

    override val registry: MapPrefsDelegate<ArithmeticChain> =
        gsonSerializableMapPrefsDelegate("REGISTRY")
}