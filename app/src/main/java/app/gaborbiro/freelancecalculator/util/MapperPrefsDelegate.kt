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


class MapperPrefsDelegate<T, S>(
    private val key: Preferences.Key<S>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
    private val mapper: Mapper<T, S>? = null,
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Flow<T?> {
        return prefs.data.map {
            if (mapper != null) {
                mapper.mapFromStore(it[key])
            } else {
                it[key] as T?
            }
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Flow<T?>) {
        scope.launch {
            prefs.edit { pref ->
                value.collectLatest {
                    it?.let {
                        if (mapper != null) {
                            pref[key] = mapper.mapToStore(it)!!
                        } else {
                            pref[key] = it as S
                        }
                    } ?: run {
                        pref.remove(key)
                    }
                }
            }
        }
    }
}

val bigDecimalMapper = object : Mapper<BigDecimal, String> {
    override fun mapToStore(value: BigDecimal?): String? = value?.let { value.toString() }

    override fun mapFromStore(value: String?): BigDecimal? = value?.let { BigDecimal(it) }
}

interface Mapper<T, S> {
    fun mapToStore(value: T?): S?
    fun mapFromStore(value: S?): T?
}
