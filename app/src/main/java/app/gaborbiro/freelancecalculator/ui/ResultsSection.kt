package app.gaborbiro.freelancecalculator.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.sections.currency.CurrencySection
import app.gaborbiro.freelancecalculator.ui.sections.daysperweek.DaysPerWeekSection
import app.gaborbiro.freelancecalculator.ui.sections.fee.FeeSection
import app.gaborbiro.freelancecalculator.ui.sections.tax.TaxAndNetIncomeSection
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.view.MoneyOverTime
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.times


@Composable
fun ColumnScope.ResultsSection(
    store: Store,
    currencyRepository: CurrencyRepository,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    DaysPerWeekSection(
        modifier = Modifier
            .wrapContentSize()
            .align(Alignment.End),
        store = store,
    )

    BaseSection(
        store = store,
        onMoneyPerWeekChange = onMoneyPerWeekChange,
    )

    FeeSection(
        store = store,
        onMoneyPerWeekChange = onMoneyPerWeekChange,
    )

    CurrencySection(
        store = store,
        currencyRepository = currencyRepository,
        onMoneyPerWeekChange = onMoneyPerWeekChange
    )

    TaxAndNetIncomeSection(
        store = store,
        currencyRepository = currencyRepository,
        onMoneyPerWeekChange = onMoneyPerWeekChange
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ColumnScope.BaseSection(
    store: Store,
    onMoneyPerWeekChange: (value: ArithmeticChain?) -> Unit,
) {
    val feePerHour by store.feePerHour.collectAsState(initial = null)
    val hoursPerWeek by store.hoursPerWeek.collectAsState(initial = null)

    MoneyOverTime(
        sectionId = "base",
        title = "Base",
        store = store,
        moneyPerWeek = feePerHour * hoursPerWeek,
        onMoneyPerWeekChange = onMoneyPerWeekChange
    )
}


@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Preview
@Composable
private fun ResultsContentPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PADDING_LARGE)
    ) {
        ResultsSection(
            store = Store.dummyImplementation(),
            currencyRepository = CurrencyRepository.dummyImplementation(),
            onMoneyPerWeekChange = { },
        )
    }
}