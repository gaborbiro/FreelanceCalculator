package app.gaborbiro.freelancecalculator.util

import java.io.Serializable

data class Quad<out A, out B, out C, out D>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
) : Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth)"
}

data class Quint<out A, out B, out C, out D, out E>(
    val first: A,
    val second: B,
    val third: C,
    val fourth: D,
    val fifth: E,
) : Serializable {
    override fun toString(): String = "($first, $second, $third, $fourth, $fifth)"
}