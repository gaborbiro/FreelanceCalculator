package app.gaborbiro.freelancecalculator.persistence.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


interface MapPrefsDelegate<T> {

    operator fun get(subKey: String): Flow<T?>

    /**
     * Stops previous flows being read (if any)
     */
    operator fun set(subKey: String, value: Flow<T?>)

    companion object {
        fun <T> dummyImplementation(value: Map<String, T> = emptyMap()): MapPrefsDelegate<T> {
            return object : MapPrefsDelegate<T> {
                override fun get(subKey: String): Flow<T?> {
                    return flowOf(value[subKey])
                }

                override fun set(subKey: String, value: Flow<T?>) {
                }
            }
        }
    }
}
