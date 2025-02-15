package app.gaborbiro.freelancecalculator.persistence.domain

import kotlinx.coroutines.flow.Flow
import kotlin.reflect.KProperty


interface PrefsDelegate<T> {
    operator fun getValue(thisRef: Any?, property: KProperty<*>): Flow<T?>

    /**
     * Stops previous flows being read (if any)
     */
    operator fun setValue(thisRef: Any?, property: KProperty<*>, value: Flow<T?>)
}
