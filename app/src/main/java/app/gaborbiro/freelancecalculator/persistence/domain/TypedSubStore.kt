package app.gaborbiro.freelancecalculator.persistence.domain

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf


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
