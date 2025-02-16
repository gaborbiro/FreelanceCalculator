package app.gaborbiro.freelancecalculator.persistence

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import app.gaborbiro.freelancecalculator.persistence.domain.PrefsDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * A delegate that knows how to read/write a DataStore of type Preferences. This simplifies
 * persistence by allowing the user to simply read/write a kotlin variable.
 *
 * The implementation allows for the underlying storage format to be different from the actual type
 * of the data, in order to allow storage (serialization) of complex data types.
 * Note, that the type of the key must always match the storage format in this case.
 *
 * If you don't need this, make [T] and [S] the same and set the [mapper] to null.
 */
class PrefsDelegateImpl<T, S>(
    private val key: Preferences.Key<S>,
    private val scope: CoroutineScope,
    private val prefs: DataStore<Preferences>,
    private val mapper: Mapper<T, S>? = null,
) : PrefsDelegate<T> {

    private val _stateFlow = MutableStateFlow<T?>(null)
    override val stateFlow: MutableStateFlow<T?> get() = _stateFlow
    private val mutex = Mutex()

    init {
        readInitialValue()
        observeStateFlow()
    }

    private fun readInitialValue() {
        scope.launch {
            _stateFlow.value = prefs.data.firstOrNull()?.let { prefs ->
                mapper
                    ?.fromStoreType(prefs[key])
                    ?: prefs[key] as T?
            }
        }
    }

    private fun observeStateFlow() {
        scope.launch {
            _stateFlow.collect { value ->
                mutex.withLock {
                    prefs.edit { pref ->
                        if (value != null) {
                            val mappedValue = mapper
                                ?.let { it.toStoreType(value)!! }
                                ?: value as S
                            if (pref[key] != mappedValue) {
                                pref[key] = mappedValue
                            }
                        } else {
                            pref.remove(key)
                        }
                    }
                }
            }
        }
    }
}

