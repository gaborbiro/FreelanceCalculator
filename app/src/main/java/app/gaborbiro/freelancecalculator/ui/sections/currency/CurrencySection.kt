package app.gaborbiro.freelancecalculator.ui.sections.currency

import android.widget.Toast
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.model.ExchangeRateUIModel
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.Lce
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.times

@OptIn(ExperimentalMaterial3Api::class)
@Suppress("FlowOperatorInvokedInComposition")
@Composable
fun ColumnScope.CurrencySection(
    inputId: String,
    sectionId: String,
    title: String,
    store: Store,
    currencyRepository: CurrencyRepository,
    onMoneyPerWeekChanged: (ArithmeticChain?) -> Unit,
) {
    val selectedCurrency by store.currencies[sectionId]
        .collectAsState(initial = null)
    val (fromCurrency, toCurrency) = selectedCurrency ?: (null to null)

    val moneyPerWeek by store
        .registry["${inputId}:${Store.DATA_ID_MONEY_PER_WEEK}"]
        .collectAsState(initial = null)

    val savedRate by store.exchangeRates[sectionId].collectAsState(initial = null)
    var rateUIModel: ExchangeRateUIModel by remember(savedRate) {
        mutableStateOf(
            savedRate ?: ExchangeRateUIModel(rate = null, since = "")
        )
    }

    if (fromCurrency != null && toCurrency != null) {
        val rateResult = remember(fromCurrency, toCurrency) {
            currencyRepository.getRate(
                fromCurrency,
                toCurrency
            )
        }
            .subscribeAsState(Lce.Loading).value

        when (rateResult) {
            is Lce.Loading -> {
                rateUIModel = rateUIModel.copy(since = "Loading...")
            }

            is Lce.Data -> {
                store.exchangeRates[sectionId] = rateUIModel.copy(
                    rate = rateResult.data.rate,
                    since = "Refreshed at:\n${rateResult.data.since}"
                )
            }

            is Lce.Error -> {
                Toast.makeText(
                    LocalContext.current,
                    rateResult.throwable.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    val outputMoneyPerWeek = if (fromCurrency != null && toCurrency != null) {
        moneyPerWeek * rateUIModel.rate
    } else {
        null
    }
    store.registry["${sectionId}:${Store.DATA_ID_MONEY_PER_WEEK}"] = outputMoneyPerWeek

    MoneyOverTime(
        collapseId = "${sectionId}:currency",
        title = title,
        store = store,
        moneyPerWeek = outputMoneyPerWeek,
        extraContent = {
            Row {
                CurrencySelector(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(PADDING_LARGE),
                    selectedCurrency = fromCurrency,
                    label = "From",
                    currencyRepository = currencyRepository,
                ) {
                    store.currencies[sectionId] = it to toCurrency
                }
                CurrencySelector(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(PADDING_LARGE),
                    selectedCurrency = toCurrency,
                    label = "To",
                    currencyRepository = currencyRepository
                ) {
                    store.currencies[sectionId] = fromCurrency to it
                }
                Text(
                    modifier = Modifier
                        .padding(top = PADDING_LARGE),
                    text = rateUIModel.since,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                )
            }
        }
    ) { newValue: ArithmeticChain? ->
        val newMoneyPerWeek = newValue / rateUIModel.rate
        onMoneyPerWeekChanged(newMoneyPerWeek)
    }
}