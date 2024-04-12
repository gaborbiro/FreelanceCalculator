package app.gaborbiro.freelancecalculator.data.currency.domain

import io.reactivex.Single
import java.time.LocalDateTime


interface CurrencyDataSource {

    val currencies: Single<List<String>>

    fun getRate(from: String, to: String): Single<ConversionRate>

    data class ConversionRate(
        val rate: Double,
        val since: LocalDateTime,
    )
}
