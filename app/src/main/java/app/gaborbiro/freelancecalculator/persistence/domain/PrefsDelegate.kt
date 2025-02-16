package app.gaborbiro.freelancecalculator.persistence.domain

import kotlinx.coroutines.flow.MutableStateFlow


interface PrefsDelegate<T> {
    val stateFlow: MutableStateFlow<T?>
}
