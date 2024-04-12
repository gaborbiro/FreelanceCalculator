package app.gaborbiro.freelancecalculator.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty


/**
 * A delegate that executes get/set operations applied to a var by fetching/saving the piece of data
 * it was associated with by the [key] argument in the constructor.
 *
 * It can have a type, but also an underlying storage type. If a mapper is available, the data is
 * stored in the underlying format instead as the main type. If you don't need an underlying format,
 * make [T] and [S] have the same value and set the [mapper] to null.
 */
class PrefsDelegate<T, S>(
    private val key: Preferences.Key<S>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
    private val mapper: Mapper<T, S>? = null,
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Flow<T?> {
        return prefs.data.map {
            mapper
                ?.fromStoreType(it[key])
                ?: it[key] as T?
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Flow<T?>) {
        scope.launch {
            prefs.edit { pref ->
                value.collectLatest { latest ->
                    if (latest != null) {
                        pref[key] = mapper
                            ?.let { it.toStoreType(latest)!! }
                            ?: latest as S
                    } else {
                        pref.remove(key)
                    }
                }
            }
        }
    }
}

interface Mapper<T, S> {
    fun toStoreType(value: T?): S?
    fun fromStoreType(value: S?): T?
}
