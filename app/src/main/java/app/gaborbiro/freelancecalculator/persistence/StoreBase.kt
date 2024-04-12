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
import app.gaborbiro.freelancecalculator.persistence.domain.MapDelegate
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preferences")

abstract class StoreBase(context: Context, protected val scope: CoroutineScope) {

    protected val gson = Gson()

    protected val prefs: DataStore<Preferences> = context.dataStore

    protected inline fun <reified T> gsonSerializedPrefsDelegate(key: String): PrefsDelegate<T, String> {
        return PrefsDelegate(
            key = stringPreferencesKey(key),
            scope = scope,
            prefs = prefs,
            mapper = object : Mapper<T, String> {

                private val cache: LruCache<String, T> =
                    LruCache(1 * 1024 * 1024) // 1MB

                override fun toStoreType(value: T?): String? {
                    return value?.let {
                        val mappedValue = gson.toJson(it)
                        cache.put(mappedValue, it)
                        mappedValue
                    }
                }

                override fun fromStoreType(value: String?): T? {
                    return value?.let { value ->
                        cache[value]
                            ?: gson.fromJson(value, T::class.java)
                                .also {
                                    cache.put(value, it)
                                }
                    }
                }
            }
        )
    }

    protected inline fun <reified T> gsonSerializedMapDelegate(
        key: String,
        size: Int = 1 * 1024 * 1024, // 1MB
    ): MapDelegate<T, String> {
        return MapDelegateImpl(
            key = { stringPreferencesKey("${key}_$it") },
            scope = scope,
            prefs = prefs,
            mapper = object : Mapper<T, String> {

                private val cache: LruCache<String, T> = LruCache(size)

                override fun toStoreType(value: T?): String? {
                    return value?.let {
                        val serialised = gson.toJson(it)
                        cache.put(serialised, it)
                        serialised
                    }
                }

                override fun fromStoreType(value: String?): T? {
                    return value?.let { value ->
                        cache[value]
                            ?: gson.fromJson(value, T::class.java)
                                .also {
                                    cache.put(value, it)
                                }
                    }
                }
            }
        )
    }

    protected fun intDelegate(key: String): PrefsDelegate<Int, Int> {
        return PrefsDelegate(
            key = intPreferencesKey(key),
            scope = scope,
            prefs = prefs,
        )
    }

    protected fun doubleDelegate(key: String): PrefsDelegate<Double, Double> {
        return PrefsDelegate(
            key = doublePreferencesKey(key),
            scope = scope,
            prefs = prefs,
        )
    }

    protected fun stringDelegate(key: String): PrefsDelegate<String, String> {
        return PrefsDelegate(
            key = stringPreferencesKey(key),
            scope = scope,
            prefs = prefs,
        )
    }

    protected fun booleanMapDelegate(key: String): MapDelegate<Boolean, Boolean> {
        return MapDelegateImpl(
            key = { booleanPreferencesKey("${key}_$it") },
            scope = scope,
            prefs = prefs,
        )
    }
}