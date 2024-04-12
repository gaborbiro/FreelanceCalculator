package app.gaborbiro.freelancecalculator.data.currency

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.annotation.IntDef
import androidx.annotation.Keep
import androidx.core.content.getSystemService
import app.gaborbiro.freelancecalculator.BuildConfig
import app.gaborbiro.freelancecalculator.data.currency.domain.CurrencyDataSource
import com.google.gson.Gson
import io.reactivex.Flowable
import io.reactivex.Single
import okhttp3.Cache
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.logging.HttpLoggingInterceptor
import org.reactivestreams.Publisher
import java.io.InputStreamReader
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit


class CurrencyDataSourceImpl(private val appContext: Context) : CurrencyDataSource {

    private val client: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
        val logger = HttpLoggingInterceptor()
        logger.level = HttpLoggingInterceptor.Level.BODY
        builder.addInterceptor(logger)
        val cacheSize = (5 * 1024 * 1024).toLong() // 5MB
        builder.cache(Cache(appContext.cacheDir, cacheSize))
        builder.addInterceptor { chain ->

            val cacheControlHeader = if (getConnectionType(appContext) == NONE) {
                "public, only-if-cached, max-stale=" + TimeUnit.DAYS.toSeconds(7)
            } else {
                "public, max-age=" + TimeUnit.DAYS.toSeconds(1)
            }

            val request = chain.request().newBuilder()
                .header(
                    name = "Cache-Control",
                    value = cacheControlHeader
                ).build()

            chain.proceed(request)
        }
        builder.build()
    }

    private val refreshCurrencies = call<ApiCurrencyListResult>("list")

    override val currencies: Single<List<String>> = refreshCurrencies
        .map { it.currencies.keys.toList() }

    private val refreshRate: MutableMap<Pair<String, String>, Single<CurrencyDataSource.ConversionRate>> =
        mutableMapOf()

    override fun getRate(from: String, to: String): Single<CurrencyDataSource.ConversionRate> {
        return (refreshRate[from to to] ?: run {
            refreshRate[from to to] =
                call<ApiConversionResult>("convert?format=json&from=$from&to=$to&amount=1")
                    .map {
                        CurrencyDataSource.ConversionRate(
                            rate = it.rates[to]!!.rate,
                            since = LocalDateTime.now(),
                        )
                    }
                    .cache()
            refreshRate[from to to]!!
        })
    }

    private inline fun <reified T> call(path: String): Single<T> {
        return Single.create { emitter ->
            val request = Request.Builder()
                .url("https://currency-converter5.p.rapidapi.com/currency/$path")
                .get()
                .addHeader("x-rapidapi-key", BuildConfig.RAPIDAPI_API_KEY)
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
                emitter.onError(
                    CurrencyConverterError(
                        response.code,
                        "Http ${response.code} ${response.message}"
                    )
                )
            }
        }
            .doOnError {
                println(it)
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
    private data class ApiCurrencyListResult(val currencies: Map<String, String>)

    @Keep
    private data class ApiConversionResult(val rates: Map<String, ApiConversionRate>)

    @Keep
    private data class ApiConversionRate(val rate: Double)

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

    companion object {
        @IntDef(NONE, MOBILE, WIFI, VPN)
        @Retention(AnnotationRetention.SOURCE)
        annotation class ConnectionType

        const val NONE = 0
        const val MOBILE = 1
        const val WIFI = 2
        const val VPN = 3
    }

    @ConnectionType
    fun getConnectionType(context: Context): Int {
        val capabilities = context.getSystemService<ConnectivityManager>()
            ?.let { it.getNetworkCapabilities(it.activeNetwork) }
        return when {
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) == true -> WIFI
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) == true -> MOBILE
            capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true -> VPN
            else -> NONE
        }
    }
}
