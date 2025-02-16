package app.gaborbiro.freelancecalculator.persistence.domain

import kotlinx.coroutines.flow.MutableStateFlow


interface MapPrefsDelegate<T> {
    operator fun get(subKey: String): MutableStateFlow<T?>
    operator fun get(subKey: String, default: T): MutableStateFlow<T>

    companion object {
        fun <T> dummyImplementation(value: Map<String, T> = emptyMap()): MapPrefsDelegate<T> {
            return object : MapPrefsDelegate<T> {

                override fun get(subKey: String): MutableStateFlow<T?> {
                    return MutableStateFlow(value[subKey])
                }

                override fun get(subKey: String, default: T): MutableStateFlow<T> {
                    return MutableStateFlow(value[subKey] ?: default)
                }
            }
        }
    }
}
