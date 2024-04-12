package app.gaborbiro.freelancecalculator.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.gaborbiro.freelancecalculator.persistence.domain.MapDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class MapDelegateImpl<T, S>(
    private val key: (subKey: String) -> Preferences.Key<S>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
    private val mapper: Mapper<T, S>? = null,
) : MapDelegate<T, S> {

    override operator fun get(subKey: String): Flow<T?> {
        return prefs.data.map { prefs ->
            mapper
                ?.fromStoreType(prefs[key(subKey)])
                ?: prefs[key(subKey)] as T?
        }
    }

    override operator fun set(subKey: String, value: T?) {
        scope.launch {
            prefs.edit { prefs ->
                value?.let {
                    prefs[key(subKey)] = mapper
                        ?.toStoreType(it)
                        ?: it as S
                } ?: run {
                    prefs.remove(key(subKey))
                }
            }
        }
    }
}