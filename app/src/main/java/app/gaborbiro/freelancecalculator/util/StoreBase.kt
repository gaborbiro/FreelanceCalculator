package app.gaborbiro.freelancecalculator.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import java.math.BigDecimal

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preferences")


abstract class StoreBase(context: Context, private val scope: CoroutineScope) {

    private val prefs: DataStore<Preferences> = context.dataStore

    protected fun bigDecimalDelegate(key: String): MapperPrefsDelegate<BigDecimal, String> {
        return MapperPrefsDelegate(
            key = stringPreferencesKey(key),
            scope = scope,
            prefs = prefs,
            mapper = bigDecimalMapper
        )
    }

    protected fun stringDelegate(key: String): MapperPrefsDelegate<String, String> {
        return MapperPrefsDelegate(
            key = stringPreferencesKey(key),
            scope = scope,
            prefs = prefs,
        )
    }

    protected fun booleanSubDelegate(key: String): TypedSubStore<Boolean> {
        return TypedSubStoreImpl<Boolean>(
            key = { booleanPreferencesKey("${key}_$it") },
            scope = scope,
            prefs = prefs,
        )
    }
}