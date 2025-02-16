package app.gaborbiro.freelancecalculator.persistence

import android.content.Context
import android.util.LruCache
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import app.gaborbiro.freelancecalculator.persistence.domain.MapPrefsDelegate
import app.gaborbiro.freelancecalculator.persistence.domain.PrefsDelegate
import app.gaborbiro.freelancecalculator.persistence.domain.PrefsDelegateNullable
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preferences")

internal abstract class StoreBase(context: Context, protected val scope: CoroutineScope) {

    protected val gson = Gson()
    protected val prefs: DataStore<Preferences> = context.dataStore
    private val prefsDelegateMap: MutableMap<String, PrefsDelegate<*>> = mutableMapOf()
    private val prefsDelegateNullableMap: MutableMap<String, PrefsDelegateNullable<*>> =
        mutableMapOf()
    private val mapPrefsDelegateMap: MutableMap<String, MapPrefsDelegate<*>> = mutableMapOf()

    companion object {
        private const val CACHE_SIZE = 1 * 1024 * 1024 // 1MB
    }

    protected inline fun <reified T> gsonSerializablePrefsDelegate(
        key: String,
    ): PrefsDelegateNullable<T> {
        synchronized(key) {
            return (prefsDelegateNullableMap[key] as PrefsDelegateNullable<T>?) ?: run {
                PrefsDelegateImplNullable(
                    key = stringPreferencesKey(key),
                    scope = scope,
                    prefs = prefs,
                    mapper = getPrefsDelegateMapper(CACHE_SIZE, T::class.java),
                ).also {
                    prefsDelegateNullableMap[key] = it
                }
            }
        }
    }

    protected inline fun <reified T> gsonSerializablePrefsDelegate(
        key: String,
        default: T,
    ): PrefsDelegate<T> {
        synchronized(key) {
            return (prefsDelegateMap[key] as PrefsDelegate<T>?) ?: run {
                PrefsDelegateImpl(
                    key = stringPreferencesKey(key),
                    scope = scope,
                    prefs = prefs,
                    mapper = getPrefsDelegateMapper(CACHE_SIZE, T::class.java),
                    default = default,
                ).also {
                    prefsDelegateMap[key] = it
                }
            }
        }
    }

    protected inline fun <reified T> gsonSerializableMapPrefsDelegate(
        keyBase: String,
    ): MapPrefsDelegate<T> {
        synchronized(keyBase) {
            return (mapPrefsDelegateMap[keyBase] as MapPrefsDelegate<T>?) ?: run {
                MapPrefsDelegateImpl(
                    key = keyBase,
                    createKey = { stringPreferencesKey(it) },
                    scope = scope,
                    prefs = prefs,
                    mapper = getPrefsDelegateMapper(CACHE_SIZE, T::class.java),
                ).also {
                    mapPrefsDelegateMap[keyBase] = it
                }
            }
        }
    }

    protected fun booleanMapPrefsDelegate(keyBase: String): MapPrefsDelegate<Boolean> {
        synchronized(keyBase) {
            return (mapPrefsDelegateMap[keyBase] as MapPrefsDelegate<Boolean>?) ?: run {
                MapPrefsDelegateImpl<Boolean, Boolean>(
                    key = keyBase,
                    createKey = { booleanPreferencesKey(it) },
                    scope = scope,
                    prefs = prefs,
                ).also {
                    mapPrefsDelegateMap[keyBase] = it
                }
            }
        }
    }

    private fun <T> getPrefsDelegateMapper(size: Int, type: Class<T>): Mapper<T, String> =
        object : Mapper<T, String> {

            private val cache: LruCache<String, T> = LruCache(size)

            override fun toStoreType(value: T?): String? {
                return value?.let {
                    val serialised = gson.toJson(it)
                    cache.put(serialised, it)
                    serialised
                }
            }

            override fun fromStoreType(serialised: String?): T? {
                return serialised?.let { serialised ->
                    cache[serialised]
                        ?: gson.fromJson(serialised, type)
                            .also {
                                cache.put(serialised, it)
                            }
                }
            }
        }

    protected fun intDelegate(key: String): MutableStateFlow<Int?> {
        synchronized(key) {
            return (prefsDelegateNullableMap[key] as PrefsDelegateNullable<Int>?)
                ?.stateFlow
                ?: run {
                    PrefsDelegateImplNullable<Int, Int>(
                        key = intPreferencesKey(name = key),
                        scope = scope,
                        prefs = prefs,
                    ).also {
                        prefsDelegateNullableMap[key] = it
                    }.stateFlow
                }
        }
    }

    protected fun intDelegate(key: String, default: Int): MutableStateFlow<Int> {
        synchronized(key) {
            return (prefsDelegateMap[key] as PrefsDelegate<Int>?)
                ?.stateFlow
                ?: run {
                    PrefsDelegateImpl(
                        key = intPreferencesKey(name = key),
                        scope = scope,
                        prefs = prefs,
                        default = default,
                    ).also {
                        prefsDelegateMap[key] = it
                    }.stateFlow
                }
        }
    }

    protected fun doubleDelegate(key: String): MutableStateFlow<Double?> {
        synchronized(key) {
            return (prefsDelegateNullableMap[key] as PrefsDelegateNullable<Double>?)
                ?.stateFlow
                ?: run {
                    PrefsDelegateImplNullable<Double, Double>(
                        key = doublePreferencesKey(name = key),
                        scope = scope,
                        prefs = prefs,
                    ).also {
                        prefsDelegateNullableMap[key] = it
                    }.stateFlow
                }
        }
    }

    protected fun doubleDelegate(key: String, default: Double): MutableStateFlow<Double> {
        synchronized(key) {
            return (prefsDelegateMap[key] as PrefsDelegate<Double>?)
                ?.stateFlow
                ?: run {
                    PrefsDelegateImpl(
                        key = doublePreferencesKey(name = key),
                        scope = scope,
                        prefs = prefs,
                        default = default,
                    ).also {
                        prefsDelegateMap[key] = it
                    }.stateFlow
                }
        }
    }

    protected fun stringDelegate(key: String): MutableStateFlow<String?> {
        synchronized(key) {
            return (prefsDelegateNullableMap[key] as PrefsDelegateNullable<String>?)
                ?.stateFlow
                ?: run {
                    PrefsDelegateImplNullable<String, String>(
                        key = stringPreferencesKey(name = key),
                        scope = scope,
                        prefs = prefs,
                    ).also {
                        prefsDelegateNullableMap[key] = it
                    }.stateFlow
                }
        }
    }

    protected fun stringDelegate(key: String, default: String): MutableStateFlow<String> {
        synchronized(key) {
            return (prefsDelegateMap[key] as PrefsDelegate<String>?)
                ?.stateFlow
                ?: run {
                    PrefsDelegateImpl<String, String>(
                        key = stringPreferencesKey(name = key),
                        scope = scope,
                        prefs = prefs,
                        default = default,
                    ).also {
                        prefsDelegateMap[key] = it
                    }.stateFlow
                }
        }
    }
}