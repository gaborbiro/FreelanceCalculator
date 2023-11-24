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


class MapperPrefsDelegate<T>(
    private val key: Preferences.Key<T>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Flow<T?> {
        return prefs.data.map {
            it[key]
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Flow<T?>) {
        scope.launch {
            prefs.edit { pref ->
                value.collectLatest {
                    it?.let {
                        pref[key] = it
                    } ?: run {
                        pref.remove(key)
                    }
                }
            }
        }
    }
}
