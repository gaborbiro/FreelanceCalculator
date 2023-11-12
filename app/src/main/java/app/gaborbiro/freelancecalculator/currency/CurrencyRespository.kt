package app.gaborbiro.freelancecalculator.currency

import app.gaborbiro.freelancecalculator.util.Lce
import io.reactivex.Observable
import io.reactivex.Single
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import java.io.IOException

interface CurrencyRepository {
    fun getCurrencies(): Observable<Lce<Array<String>>>

    fun getRate(from: String, to: String): Observable<Lce<Double>>

    companion object {
        fun getDummyImplementation() = object : CurrencyRepository {
            override fun getCurrencies(): Observable<Lce<Array<String>>> =
                Observable.just(Lce.Data(arrayOf("USD", "GBP")))

            override fun getRate(from: String, to: String): Observable<Lce<Double>> =
                Observable.just(Lce.Data(1.0))
        }
    }
}

class CurrencyRepositoryImpl(private val dataSource: CurrencyDataSource) :
    CurrencyRepository {

    override fun getCurrencies(): Observable<Lce<Array<String>>> {
        return prepare(dataSource.getCurrencies().map {
            it.sorted().toTypedArray()
        })
    }

    override fun getRate(from: String, to: String): Observable<Lce<Double>> {
        return prepare(dataSource.getRate(from, to))
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