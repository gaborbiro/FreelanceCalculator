package app.gaborbiro.freelancecalculator.persistence.domain

import kotlinx.coroutines.flow.MutableStateFlow


interface PrefsDelegateNullable<T> {
    val stateFlow: MutableStateFlow<T?>
}
