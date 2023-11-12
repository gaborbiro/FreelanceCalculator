package app.gaborbiro.freelancecalculator.util

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.CoroutineScope
import java.math.BigDecimal

abstract class StoreBase(context: Context, private val scope: CoroutineScope) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("preferences")
    private val prefs: DataStore<Preferences> = context.dataStore

    protected fun bigDecimalDelegate(key: String): MapperPrefsDelegate<BigDecimal> {
        return MapperPrefsDelegate(
            key = stringPreferencesKey(key),
            scope = scope,
            prefs = prefs,
            mapper = bigDecimalMapper
        )
    }
}