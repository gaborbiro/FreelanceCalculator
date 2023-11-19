package app.gaborbiro.freelancecalculator.data.currency.domain

import io.reactivex.Single


interface CurrencyDataSource {
    val currencies: Single<List<String>>
    fun getRate(from: String, to: String): Single<Double>
}
