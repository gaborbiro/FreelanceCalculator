package app.gaborbiro.freelancecalculator.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.gaborbiro.freelancecalculator.persistence.domain.PrefsDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlin.reflect.KProperty

/**
 * A delegate that knows how to read/write a DataStore of type Preferences. This simplifies
 * persistence by allowing the user to simply read/write a kotlin variable.
 *
 * The implementation allows for the underlying storage format to be different from the actual type
 * of the data, in order to allow storage (serialization) of complex data types.
 * Note, that the type of the key must always match the storage format in this case.
 *
 * If you don't need this, make [T] and [S] the same and set the [mapper] to null.
 */
class PrefsDelegateImpl<T, S>(
    private val key: Preferences.Key<S>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
    private val mapper: Mapper<T, S>? = null,
) : PrefsDelegate<T> {

    override operator fun getValue(thisRef: Any?, property: KProperty<*>): Flow<T?> {
        return prefs.data.map { prefs ->
            mapper
                ?.fromStoreType(prefs[key])
                ?: prefs[key] as T?
        }.distinctUntilChanged()
    }

    override operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Flow<T?>) {
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

