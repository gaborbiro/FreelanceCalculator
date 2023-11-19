package app.gaborbiro.freelancecalculator.currency

import app.gaborbiro.freelancecalculator.util.Lce
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException

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

class CurrencyRepositoryImpl(private val dataSource: CurrencyDataSource) :
    CurrencyRepository {

    override val currencies: Observable<Lce<List<String>>> =
        Observable.just(
            Lce.Data(
                listOf(
                    "STN", "XAG", "XAU", "USDC", "USDT", "PLN", "UGX", "GGP", "MWK", "NAD", "ALL", "BHD", "JEP", "BWP", "MRU", "BMD", "MNT", "FKP", "PYG", "AUD", "KYD", "RWF", "WST", "SHP", "SOS", "SSP", "BIF", "SEK", "CUC", "BTN", "MOP", "XDR", "IMP", "INR", "BYN", "BOB", "SRD", "GEL", "ZWL", "EUR", "BBD", "RSD", "SDG", "VND", "VES", "ZMW", "KGS", "HUF", "BND", "BAM", "CVE", "BGN", "NOK", "BRL", "JPY", "HRK", "HKD", "ISK", "IDR", "KRW", "KHR", "XAF", "CHF", "MXN", "PHP", "RON", "RUB", "SGD", "AED", "KWD", "CAD", "PKR", "CLP", "CNY", "COP", "AOA", "KMF", "CRC", "CUP", "GNF", "NZD", "EGP", "DJF", "ANG", "DOP", "JOD", "AZN", "SVC", "NGN", "ERN", "SZL", "DKK", "ETB", "FJD", "XPF", "GMD", "AFN", "GHS", "GIP", "GTQ", "HNL", "GYD", "HTG", "XCD", "GBP", "AMD", "IRR", "JMD", "IQD", "KZT", "KES", "ILS", "LYD", "LSL", "LBP", "LRD", "AWG", "MKD", "LAK", "MGA", "ZAR", "MDL", "MVR", "MUR", "MMK", "MAD", "XOF", "MZN", "MYR", "OMR", "NIO", "NPR", "PAB", "PGK", "PEN", "ARS", "SAR", "QAR", "SCR", "SLL", "LKR", "SBD", "VUV", "USD", "DZD", "BDT", "BSD", "BZD", "CDF", "UAH", "YER", "TMT", "UZS", "UYU", "CZK", "SYP", "TJS", "TWD", "TZS", "TOP", "TTD", "THB", "TRY", "TND"
                )
            )
        )
//        prepare(dataSource.currencies.map {
//        it.sorted()
//    }).cache()

    override fun getRate(from: String, to: String): Observable<Lce<Double>> {
        return Observable.just(Lce.Data(1.0))
//        return prepare(dataSource.getRate(from, to))
    }

    private fun <T> prepare(single: Single<T>): Observable<Lce<T>> {
        return single.map { Lce.Data(it) as Lce<T> }
            .toObservable()
            .startWith(Lce.Loading)
            .onErrorReturn {
                if (it is IOException) {
                    val error =
                        DomainException("Network error. Please check your internet connection.")
                    Lce.Error(error)
                } else {
                    Lce.Error(throwable = it)
                }
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
    }
}