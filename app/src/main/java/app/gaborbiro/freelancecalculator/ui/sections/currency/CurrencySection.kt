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
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.ui.model.ExchangeRateUIModel
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.Lce
import app.gaborbiro.freelancecalculator.util.chainify
import app.gaborbiro.freelancecalculator.util.div
import app.gaborbiro.freelancecalculator.util.times
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ColumnScope.CurrencySection(
    store: Store,
    currencyRepository: CurrencyRepository,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    val fromCurrency by store.fromCurrency.collectAsState(initial = null)
    val toCurrency by store.toCurrency.collectAsState(initial = null)

    if (fromCurrency == null || toCurrency == null) {
        return
    }

    val fee by store.fee.collectAsState(initial = null)
    val feeMultiplier = fee?.let { 1.0 - (it / 100.0) }.chainify()
    val feePerHour by store.feePerHour.collectAsState(initial = null)
    val hoursPerWeek by store.hoursPerWeek.collectAsState(initial = null)
    var exchangeRate: ExchangeRateUIModel by remember {
        mutableStateOf(
            ExchangeRateUIModel(
                rate = null,
                since = ""
            )
        )
    }

    val newRate = remember(fromCurrency, toCurrency) {
        currencyRepository.getRate(
            fromCurrency!!,
            toCurrency!!
        )
    }
        .subscribeAsState(Lce.Loading).value

    when (newRate) {
        is Lce.Loading -> {
            exchangeRate = exchangeRate.copy(since = "Loading...")
        }

        is Lce.Data -> {
            exchangeRate = exchangeRate.copy(
                rate = newRate.data.rate,
                since = "Refreshed at:\n${newRate.data.since}"
            )
        }

        is Lce.Error -> {
            Toast.makeText(
                LocalContext.current,
                newRate.throwable.message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    MoneyOverTime(
        sectionId = "currency",
        title = "Currency conversion",
        store = store,
        moneyPerWeek = feePerHour * hoursPerWeek * feeMultiplier * exchangeRate.rate,
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
                    store.fromCurrency = flowOf(it)
                }
                CurrencySelector(
                    modifier = Modifier
                        .width(140.dp)
                        .padding(PADDING_LARGE),
                    selectedCurrency = toCurrency,
                    label = "To",
                    currencyRepository = currencyRepository
                ) {
                    store.toCurrency = flowOf(it)
                }
                Text(
                    modifier = Modifier
                        .padding(top = PADDING_LARGE),
                    text = exchangeRate.since,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                )
            }
        }
    ) { newValue: ArithmeticChain? ->
        val moneyPerWeek = newValue / feeMultiplier / exchangeRate.rate
        onMoneyPerWeekChange(moneyPerWeek)
    }
}