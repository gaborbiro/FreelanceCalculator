package app.gaborbiro.freelancecalculator.persistence.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


interface MapDelegate<T, S> {
    operator fun get(subKey: String): Flow<T?>

    operator fun set(subKey: String, value: T?)

    companion object {
        fun <T, S> dummyImplementation(value: Map<String, T> = emptyMap()): MapDelegate<T, S> {
            return object : MapDelegate<T, S> {
                override fun get(subKey: String): Flow<T?> {
                    return flowOf(value[subKey])
                }

                override fun set(subKey: String, value: T?) {
                }
            }
        }
    }
}
