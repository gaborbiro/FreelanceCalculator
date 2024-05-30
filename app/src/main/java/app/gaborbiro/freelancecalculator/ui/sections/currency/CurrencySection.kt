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
import app.gaborbiro.freelancecalculator.ui.sections.ExchangeRate
import app.gaborbiro.freelancecalculator.ui.sections.Fee
import app.gaborbiro.freelancecalculator.ui.sections.Operand
import app.gaborbiro.freelancecalculator.ui.sections.SectionBuilder
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
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
    externalOperands: List<Operand>,
    onMoneyPerWeekChanged: (ArithmeticChain?) -> Unit,
) {
    val selectedCurrency by store.currencies[sectionId]
        .collectAsState(initial = null)
    val (fromCurrency, toCurrency) = selectedCurrency ?: (null to null)

    var rateUIModel: ExchangeRateUIModel by remember {
        mutableStateOf(
            ExchangeRateUIModel(
                rate = null,
                since = "",
                error = false,
            )
        )
    }

    if (fromCurrency != null && toCurrency != null) {
        rateUIModel = store.exchangeRates[sectionId].collectAsState(initial = null).value
            ?: rateUIModel

        val rateResult = remember(fromCurrency, toCurrency) {
            currencyRepository.getRate(
                fromCurrency,
                toCurrency
            )
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
//                    Toast.makeText(
//                        LocalContext.current,
//                        rateResult.throwable.message,
//                        Toast.LENGTH_SHORT
//                    ).show()
                }
            }
        }
    }

    val sectionBuilder = SectionBuilder(inputId, sectionId, title, store, onMoneyPerWeekChanged)

    sectionBuilder.MoneyBreakdown(
        this,
        output = { moneyPerWeek ->
            moneyPerWeek * rateUIModel.rate
        },
        reverse = { newValue ->
            newValue / rateUIModel.rate
        },
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
                val sinceStr = rateUIModel.since + if (rateUIModel.error) "(error)" else ""
                Text(
                    modifier = Modifier
                        .padding(top = PADDING_LARGE),
                    text = sinceStr,
                    fontSize = 10.sp,
                    lineHeight = 12.sp,
                )
            }
        },
        operand = ExchangeRate(sectionId),
        externalOperands = externalOperands,
    )
}