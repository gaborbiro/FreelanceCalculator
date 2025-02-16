package app.gaborbiro.freelancecalculator.persistence

import androidx.collection.LruCache
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import app.gaborbiro.freelancecalculator.persistence.domain.MapPrefsDelegate
import app.gaborbiro.freelancecalculator.persistence.domain.PrefsDelegate
import app.gaborbiro.freelancecalculator.persistence.domain.PrefsDelegateNullable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A partial map implementation that knows how to get/set a DataStore of type Preferences.
 * This simplifies persistence by allowing the user to simply read/write a kotlin map object.
 *
 * The implementation allows for the underlying storage format to be different from the actual type
 * of the data, in order to allow storage (serialization) of complex data types.
 * Note, that the type of the key must always match the storage format in this case.
 *
 * If you don't need this, make [T] and [S] the same and set the [mapper] to null.
 */
class MapPrefsDelegateImpl<T, S>(
    private val key: String,
    private val createKey: (key: String) -> Preferences.Key<S>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
    private val mapper: Mapper<T, S>? = null,
) : MapPrefsDelegate<T> {

    private val prefsDelegateNullableMap: MutableMap<Preferences.Key<S>, PrefsDelegateNullable<T>> =
        mutableMapOf()
    private val prefsDelegateMap: MutableMap<Preferences.Key<S>, PrefsDelegate<T>> = mutableMapOf()

    private val keyCache: LruCache<String, Preferences.Key<S>> =
        object : LruCache<String, Preferences.Key<S>>(100) {

            override fun create(key: String): Preferences.Key<S> {
                return createKey(key)
            }
        }

    override operator fun get(subKey: String): MutableStateFlow<T?> {
        val key: Preferences.Key<S> = keyCache["${key}_${subKey}"]!!
        synchronized(key) {
            return prefsDelegateNullableMap[key]
                ?.stateFlow
                ?: run {
                    PrefsDelegateImplNullable(key, scope, prefs, mapper)
                        .also { prefsDelegateNullableMap[key] = it }
                        .stateFlow
                }
        }
    }

    override operator fun get(subKey: String, default: T): MutableStateFlow<T> {
        val key: Preferences.Key<S> = keyCache["${key}_${subKey}"]!!
        synchronized(key) {
            return prefsDelegateMap[key]
                ?.stateFlow
                ?: run {
                    PrefsDelegateImpl(key, scope, prefs, mapper, default)
                        .also { prefsDelegateMap[key] = it }
                        .stateFlow
                }
        }
    }
}