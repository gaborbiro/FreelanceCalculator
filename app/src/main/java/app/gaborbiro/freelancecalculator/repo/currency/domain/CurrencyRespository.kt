package app.gaborbiro.freelancecalculator.repo.currency.domain

import app.gaborbiro.freelancecalculator.util.Lce
import io.reactivex.Observable


interface CurrencyRepository {
    val currencies: Observable<Lce<List<String>>>

    fun getRate(from: String, to: String): Observable<Lce<ExchangeRate>>

    companion object {
        fun dummyImplementation() = object : CurrencyRepository {
            override val currencies: Observable<Lce<List<String>>> =
                Observable.just(Lce.Data(listOf("USD", "GBP")))

            override fun getRate(from: String, to: String): Observable<Lce<ExchangeRate>> =
                Observable.just(Lce.Data(ExchangeRate(1.0, "now")))
        }
    }

    data class ExchangeRate(
        val rate: Double,
        val since: String,
    )
}
