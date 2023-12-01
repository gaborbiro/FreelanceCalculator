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


class PrefsDelegate<T, S>(
    private val key: Preferences.Key<S>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
    private val mapper: Mapper<T, S>? = null,
) {

    operator fun getValue(thisRef: Any?, property: KProperty<*>): Flow<T?> {
        return prefs.data.map {
            if (mapper != null) {
                mapper.fromType(it[key])
            } else {
                it[key] as T?
            }
        }
    }

    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Flow<T?>) {
        scope.launch {
            prefs.edit { pref ->
                value.collectLatest {
                    if (it != null) {
                        if (mapper != null) {
                            pref[key] = mapper.toType(it)!!
                        } else {
                            pref[key] = it as S
                        }
                    } else {
                        pref.remove(key)
                    }
                }
            }
        }
    }
}

interface Mapper<T, S> {
    fun toType(value: T?): S?
    fun fromType(value: S?): T?
}
