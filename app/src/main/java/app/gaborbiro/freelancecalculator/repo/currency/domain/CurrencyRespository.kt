package app.gaborbiro.freelancecalculator.repo.currency.domain

import app.gaborbiro.freelancecalculator.util.Lce
import io.reactivex.Observable


interface CurrencyRepository {
    val currencies: Observable<Lce<List<String>>>

    fun getRate(from: String, to: String): Observable<Lce<Double>>

    companion object {
        fun dummyImplementation() = object : CurrencyRepository {
            override val currencies: Observable<Lce<List<String>>> =
                Observable.just(Lce.Data(listOf("USD", "GBP")))

            override fun getRate(from: String, to: String): Observable<Lce<Double>> =
                Observable.just(Lce.Data(1.0))
        }
    }
}
