package app.gaborbiro.freelancecalculator.util

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.zip

//fun <T> zip(
//    vararg flows: Flow<T>,
//): Flow<List<T>> = when (flows.size) {
//    0 -> error("Number of flows must be higher than 0")
//    else -> flows.fold(flowOf(emptyList<T>())) { buffer, next ->
//        buffer.zip(next) { list, item ->
//            list + item
//        }
//    }
//}

fun <T1, T2> zip(
    first: Flow<T1>,
    second: Flow<T2>,
): Flow<Pair<T1, T2>> =
    first
        .zip(second) { a, b -> Pair(a, b) }

fun <T1, T2, T3> zip(
    first: Flow<T1>,
    second: Flow<T2>,
    third: Flow<T3>,
): Flow<Triple<T1, T2, T3>> =
    first
        .zip(second) { a, b -> a to b }
        .zip(third) { (a, b), c -> Triple(a, b, c) }

fun <T1, T2, T3, T4> zip(
    first: Flow<T1>,
    second: Flow<T2>,
    third: Flow<T3>,
    fourth: Flow<T4>,
): Flow<Quad<T1, T2, T3, T4>> =
    first
        .zip(second) { a, b -> a to b }
        .zip(third) { (a, b), c -> Triple(a, b, c) }
        .zip(fourth) { (a, b, c), d -> Quad(a, b, c, d) }

fun <T1, T2, T3, T4, T5> zip(
    first: Flow<T1>,
    second: Flow<T2>,
    third: Flow<T3>,
    fourth: Flow<T4>,
    fifth: Flow<T5>,
): Flow<Quint<T1, T2, T3, T4, T5>> =
    first
        .zip(second) { a, b -> a to b }
        .zip(third) { (a, b), c -> Triple(a, b, c) }
        .zip(fourth) { (a, b, c), d -> Quad(a, b, c, d) }
        .zip(fifth) { (a, b, c, d), e -> Quint(a, b, c, d, e) }