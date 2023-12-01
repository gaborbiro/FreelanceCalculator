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
import app.gaborbiro.freelancecalculator.persistence.domain.TypedSubStore
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope


private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preferences")

abstract class StoreBase(context: Context, private val scope: CoroutineScope) {

    private val gson = Gson()

    private val prefs: DataStore<Preferences> = context.dataStore

    protected fun arithmeticChainDelegate(key: String): PrefsDelegate<ArithmeticChain, String> {
        return PrefsDelegate(
            key = stringPreferencesKey(key),
            scope = scope,
            prefs = prefs,
            mapper = object : Mapper<ArithmeticChain, String> {

                private val cache: LruCache<String, ArithmeticChain> =
                    LruCache(1 * 1024 * 1024) // 1MB

                override fun toType(value: ArithmeticChain?): String? {
                    return value?.let {
                        val mappedValue = gson.toJson(it)
                        cache.put(mappedValue, it)
                        mappedValue
                    }
                }

                override fun fromType(value: String?): ArithmeticChain? {
                    return value?.let { value ->
                        cache[value]
                            ?: gson.fromJson(value, ArithmeticChain::class.java)
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

    protected fun booleanSubDelegate(key: String): TypedSubStore<Boolean> {
        return TypedSubStoreImpl(
            key = { booleanPreferencesKey("${key}_$it") },
            scope = scope,
            prefs = prefs,
        )
    }
}