package app.gaborbiro.freelancecalculator.ui

import England_23_24
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.SelectableContainer
import app.gaborbiro.freelancecalculator.util.Lce
import app.gaborbiro.freelancecalculator.util.hide.WEEKS_PER_YEAR
import app.gaborbiro.freelancecalculator.util.hide.div
import app.gaborbiro.freelancecalculator.util.hide.format
import app.gaborbiro.freelancecalculator.util.hide.minus
import app.gaborbiro.freelancecalculator.util.hide.times
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode


@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Composable
fun MoneyOverTimeSection(
    containerModifier: Modifier,
    selected: Boolean,
    moneyPerWeek: BigDecimal?,
    store: Store,
    currencyRepository: CurrencyRepository,
    onSelected: () -> Unit,
    onMoneyPerWeekChange: (value: BigDecimal?) -> Unit,
) {
    SelectableContainer(
        modifier = containerModifier,
        selected = selected,
        onSelected = onSelected,
    ) { modifier ->
        Column(
            modifier = modifier
                .padding(bottom = PADDING_LARGE)
        ) {
            val daysPerWeek: State<BigDecimal?> = store.daysPerWeek
                .collectAsState(initial = null)
            val fee: State<BigDecimal?> = store.fee
                .collectAsState(initial = null)
            val fromCurrency by store.fromCurrency
                .collectAsState(initial = null)
            val toCurrency by store.toCurrency
                .collectAsState(initial = null)

            FocusPinnedInputField(
                modifier = Modifier
                    .wrapContentSize()
                    .align(Alignment.End),
                label = "Days per week",
                value = daysPerWeek.value.format(decimalCount = 0),
                clearButtonVisible = true,
                onValueChange = { value ->
                    CoroutineScope(Dispatchers.IO).launch {
                        store.daysPerWeek = flowOf(value)
                    }
                },
            )

            MoneyOverTimeContent(
                sectionId = "BASE",
                title = "Basic",
                store = store,
                moneyPerWeek = moneyPerWeek,
                daysPerWeek = daysPerWeek.value,
                onMoneyPerWeekChange = onMoneyPerWeekChange
            )

            val feeMultiplier: BigDecimal? = remember(moneyPerWeek, fee.value) {
                BigDecimal.ONE - fee.value.div(BigDecimal(100))
            }

            MoneyOverTimeContent(
                sectionId = "FEE",
                title = "Fee / Reduction",
                store = store,
                moneyPerWeek = moneyPerWeek * feeMultiplier,
                daysPerWeek = daysPerWeek.value,
                extraContent = {
                    FocusPinnedInputField(
                        modifier = Modifier
                            .wrapContentSize(),
                        label = "%",
                        value = fee.value.format(decimalCount = 2),
                        clearButtonVisible = true,
                        onValueChange = { value ->
                            CoroutineScope(Dispatchers.IO).launch {
                                store.fee = flowOf(value)
                            }
                        },
                    )
                }
            ) {
                onMoneyPerWeekChange(it / feeMultiplier)
            }

            val storeRate = remember { store.rate }
                .collectAsState(initial = null)

            var rate by rememberSaveable(storeRate.value) {
                mutableStateOf(storeRate.value)
            }

            if (fromCurrency != null && toCurrency != null) {
                val newRate = remember(fromCurrency, toCurrency) {
                    currencyRepository.getRate(
                        fromCurrency!!,
                        toCurrency!!
                    )
                }
                    .subscribeAsState(Lce.Loading).value

                when (newRate) {
                    is Lce.Loading -> {

                    }

                    is Lce.Data -> {
                        rate = BigDecimal(newRate.data)
                        store.rate = flowOf(BigDecimal(newRate.data))
                    }

                    is Lce.Error -> {
                        Toast.makeText(
                            LocalContext.current,
                            newRate.throwable.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }

            val currencyMultiplier: BigDecimal? = remember(moneyPerWeek, fee.value, rate) {
                (BigDecimal.ONE - fee.value.div(BigDecimal(100))) * rate
            }

            MoneyOverTimeContent(
                sectionId = "CURRENCY",
                title = "Currency conversion",
                store = store,
                moneyPerWeek = moneyPerWeek * currencyMultiplier,
                daysPerWeek = daysPerWeek.value,
                extraContent = {
                    Row {
                        CurrencySelector(
                            modifier = Modifier
                                .width(140.dp)
                                .padding(PADDING_LARGE),
                            selectedCurrency = fromCurrency,
                            currencyRepository = currencyRepository,
                        ) {
                            store.fromCurrency = flowOf(it)
                        }
                        CurrencySelector(
                            modifier = Modifier
                                .width(140.dp)
                                .padding(PADDING_LARGE),
                            selectedCurrency = toCurrency,
                            currencyRepository = currencyRepository
                        ) {
                            store.toCurrency = flowOf(it)
                        }
                    }
                }
            ) {
                onMoneyPerWeekChange(it / currencyMultiplier)
            }

            val taxCalculator = remember { England_23_24() }

            val taxInfo = remember(moneyPerWeek, currencyMultiplier) {
                val perYear: BigDecimal? = moneyPerWeek * currencyMultiplier * WEEKS_PER_YEAR
                perYear?.let {
                    val incomeTax = taxCalculator.calculateTax(it.toDouble())
                    val nic2Tax = taxCalculator.calculateNIC2(perYear.toDouble())
                    val nic4Tax = taxCalculator.calculateNIC4(perYear.toDouble())
                    val totalTax = incomeTax.totalTax + nic2Tax.totalTax + nic4Tax.totalTax

                    val afterTaxPerWeek: BigDecimal = (perYear - BigDecimal(totalTax))!!.divide(
                        WEEKS_PER_YEAR,
                        RoundingMode.HALF_UP
                    )

                    TaxCalculationModel(
                        incomeTax = "${incomeTax.totalTax.format(decimalCount = 2)} (free: ${incomeTax.breakdown[0].bracket.amount.format()})",
                        nic2Tax = nic2Tax.totalTax.format(decimalCount = 2),
                        nic4Tax = "${nic4Tax.totalTax.format(decimalCount = 2)} (free: ${nic4Tax.breakdown[0].bracket.amount.format()})",
                        totalTax = totalTax.format(decimalCount = 2),
                        afterTaxPerWeek = afterTaxPerWeek
                    )
                }
            }

            taxInfo?.let {
                TaxContent(taxInfo = taxInfo, store = store)

                MoneyOverTimeContent(
                    sectionId = "TAX",
                    title = "Net Income",
                    store = store,
                    moneyPerWeek = taxInfo.afterTaxPerWeek,
                    daysPerWeek = daysPerWeek.value,
                ) {

                }
            }
        }
    }
}

@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Preview
@Composable
private fun MoneyOverTimePreview() {
    MoneyOverTimeSection(
        containerModifier = Modifier
            .fillMaxWidth()
            .padding(PADDING_LARGE),
        moneyPerWeek = BigDecimal.valueOf(355.0),
        onMoneyPerWeekChange = { },
        store = Store.dummyImplementation(),
        currencyRepository = CurrencyRepository.dummyImplementation(),
        selected = true,
        onSelected = { },
    )
}