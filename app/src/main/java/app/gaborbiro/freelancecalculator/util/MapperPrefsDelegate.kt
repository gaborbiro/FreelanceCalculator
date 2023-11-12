package app.gaborbiro.freelancecalculator.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.math.BigDecimal
import kotlin.reflect.KProperty


val bigDecimalMapper = object : Mapper<BigDecimal> {
    override fun map(value: BigDecimal) = value.toString()

    override fun map(value: String) = BigDecimal(value)
}

class MapperPrefsDelegate<T>(
    private val key: Preferences.Key<String>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
    private val mapper: Mapper<T>,
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Flow<T?> {
        return prefs.data.map {
            it[key]?.let(mapper::map)
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Flow<T?>) {
        scope.launch {
            prefs.edit { pref ->
                value.collectLatest {
                    it?.let {
                        pref[key] = mapper.map(it)
                    } ?: run {
                        pref.remove(key)
                    }
                }
            }
        }
    }
}

interface Mapper<T> {
    fun map(value: T): String
    fun map(value: String): T
}
