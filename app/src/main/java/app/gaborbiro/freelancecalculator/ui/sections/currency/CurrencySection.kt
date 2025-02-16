package app.gaborbiro.freelancecalculator.ui.sections.currency

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.model.ExchangeRateUIModel
import app.gaborbiro.freelancecalculator.ui.sections.SectionBuilder
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.Lce
import io.reactivex.Observable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FlowOperatorInvokedInComposition")
@Composable
fun ColumnScope.CurrencySection(
    inputId: String,
    sectionId: String,
    title: String,
    store: Store,
    currencyRepository: CurrencyRepository,
    onPerWeekValueChanged: (ArithmeticChain?) -> Unit,
) {
    val selectedCurrencies by remember(sectionId) { store.currencySelections[sectionId] }.collectAsState(initial = null)

    val (fromCurrency, toCurrency) = selectedCurrencies ?: (null to null)

    var rateUIModel: ExchangeRateUIModel by remember(inputId, sectionId) {
        mutableStateOf(
            ExchangeRateUIModel(
                rate = null,
                since = "",
                error = false,
            )
        )
    }

    if (fromCurrency != null && toCurrency != null) {
        rateUIModel = remember(sectionId) { store.exchangeRates[sectionId] }
            .collectAsState(initial = null)
            .value ?: rateUIModel

        val rateResult = remember(fromCurrency, toCurrency) {
            if (fromCurrency != toCurrency) {
                currencyRepository.getRate(
                    fromCurrency,
                    toCurrency
                )
            } else {
                Observable.just(Lce.Data(CurrencyRepository.ExchangeRate(rate = 1.0, since = "")))
            }
        }
            .subscribeAsState(Lce.Loading).value

        LaunchedEffect(rateResult) {
            when (rateResult) {
                is Lce.Loading -> {
                    rateUIModel = rateUIModel.copy(
                        since = "Loading...",
                        error = false,
                    )
                }

                is Lce.Data -> {
                    rateUIModel = rateUIModel.copy(
                        rate = rateResult.data.rate,
                        since = "Refreshed at:\n${rateResult.data.since}",
                        error = false,
                    )
                    store.exchangeRates[sectionId] = rateUIModel
                }

                is Lce.Error -> {
                    if (rateUIModel.rate == null) {
                        rateUIModel = rateUIModel.copy(
                            error = true
                        )
                    }
                }
            }
        }
    }

    val builder = remember(inputId, sectionId, title) {
        SectionBuilder(inputId, sectionId, title, store)
    }

    builder.Section(
        this,
        extraContent = {
            Row {
                val newFromCurrency = currencySelector(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(PADDING_LARGE),
                    selectedCurrency = fromCurrency,
                    label = "From",
                    currencyRepository = currencyRepository,
                )
                    .onStart { emit(fromCurrency) }

                val newToCurrency = currencySelector(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(PADDING_LARGE),
                    selectedCurrency = toCurrency,
                    label = "To",
                    currencyRepository = currencyRepository
                )
                    .onStart { emit(toCurrency) }

                LaunchedEffect(fromCurrency, toCurrency) {
                    combine(newFromCurrency, newToCurrency) { f1, f2 -> f1 to f2 }
                        .filter { (newFromCurrency, newToCurrency) ->
                            newFromCurrency != fromCurrency || newToCurrency != toCurrency
                        }
                        .collect {
                            store.currencySelections[sectionId] = it
                        }
                }

                val sinceStr = remember(rateUIModel) {
                    rateUIModel.since + if (rateUIModel.error) "(error)" else ""
                }

                Text(
                    modifier = Modifier
                        .padding(top = PADDING_LARGE),
                    text = sinceStr,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                )
            }
        },
        getMultiplier = {
            exchangeRates[sectionId]
                .map {
                    it?.rate
                }
        },
        onPerWeekValueChanged = onPerWeekValueChanged,
    )
}
