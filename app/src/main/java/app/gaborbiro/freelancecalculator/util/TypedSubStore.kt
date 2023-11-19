package app.gaborbiro.freelancecalculator.util

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

interface TypedSubStore<T> {
    operator fun get(subKey: String): Flow<T?>

    operator fun set(subKey: String, value: T?)

    companion object {
        fun <T> dummyImplementation(value: Map<String, T>): TypedSubStore<T> {
            return object : TypedSubStore<T> {
                override fun get(subKey: String): Flow<T?> {
                    return flowOf(value[subKey])
                }

                override fun set(subKey: String, value: T?) {
                }
            }
        }
    }
}


class TypedSubStoreImpl<T>(
    private val key: (String) -> Preferences.Key<T>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
) : TypedSubStore<T> {

    override operator fun get(subKey: String): Flow<T?> {
        return prefs.data.map {
            it[key(subKey)]
        }
    }

    override operator fun set(subKey: String, value: T?) {
        scope.launch {
            prefs.edit { pref ->
                value?.let {
                    pref[key(subKey)] = it
                } ?: run {
                    pref.remove(key(subKey))
                }
            }
        }
    }
}