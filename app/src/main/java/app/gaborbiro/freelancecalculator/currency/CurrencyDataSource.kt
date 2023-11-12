package app.gaborbiro.freelancecalculator.currency

import androidx.annotation.Keep
import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.OkHttpClient
import okhttp3.Request
import org.reactivestreams.Publisher
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class CurrencyDataSource {

    private val client: OkHttpClient by lazy { OkHttpClient() }

    private val refreshCurrencies = call<ApiCurrencyListResult>("list").cache()

    fun getCurrencies(): Single<List<String>> {
        return refreshCurrencies.map { it.currencies.keys.toList() }
    }

    private val refreshRate: MutableMap<Pair<String, String>, Single<ApiConversionResult>> = mutableMapOf()

    fun getRate(from: String, to: String): Single<Double> {
        return (refreshRate[from to to] ?: run {
            refreshRate[from to to] = call<ApiConversionResult>("convert?format=json&from=$from&to=$to&amount=1").cache()
            refreshRate[from to to]!!
        })
            .map { it.rates[to]!!.rate }
    }

    private inline fun <reified T> call(path: String): Single<T> {
        return Single.create { emitter ->
            val request = Request.Builder()
                .url("https://currency-converter5.p.rapidapi.com/currency/$path")
                .get()
                .addHeader("x-rapidapi-key", "removed by git-filter-repo, using local.properties since commit on 22/11/2023 20:33:18")
                .addHeader("x-rapidapi-host", "currency-converter5.p.rapidapi.com")
                .build()
            val response = client.newCall(request).execute()
            if (response.code in 200..299) {
                Gson().fromJson(InputStreamReader(response.body.byteStream()), T::class.java)?.let {
                    emitter.onSuccess(it)
                } ?: run {
                    val error = CurrencyConverterError(200, "No rate received")
                    emitter.onError(error)
                }
            } else {
                emitter.onError(CurrencyConverterError(response.code, "Http ${response.code} ${response.message}"))
            }
        }
            .retryWhen(RetryWith { error, retryCount ->
                val isTooManyRequestsError = (error as? CurrencyConverterError)?.code == 429
                if (isTooManyRequestsError && retryCount < 3) {
                    Flowable.timer(2000L + retryCount, TimeUnit.MILLISECONDS)
                } else {
                    Flowable.error(error)
                }
            })
    }

    @Keep
    private class ApiCurrencyListResult(val currencies: Map<String, String>)

    @Keep
    private class ApiConversionResult(val rates: Map<String, ApiConversionRate>)

    @Keep
    private class ApiConversionRate(val rate: Double)

    class RetryWith<T>(
        val condition: (Throwable, retryCount: Int) -> Flowable<T>,
    ) : io.reactivex.functions.Function<Flowable<Throwable?>?, Publisher<T>?> {
        private var retryCount: Int = 0

        override fun apply(throwableFlowable: Flowable<Throwable?>): Publisher<T> {
            return throwableFlowable.flatMap { throwable ->
                condition(throwable, ++retryCount)
            }
        }
    }
}