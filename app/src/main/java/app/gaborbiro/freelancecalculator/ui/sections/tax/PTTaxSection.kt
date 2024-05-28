package app.gaborbiro.freelancecalculator.ui.sections.tax

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SUB_SECTION_CURRENCY
import app.gaborbiro.freelancecalculator.persistence.domain.Store.Companion.SUB_SECTION_TAX
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.ui.sections.currency.CurrencySection
import app.gaborbiro.freelancecalculator.ui.sections.fee.FeeSection
import app.gaborbiro.freelancecalculator.util.ArithmeticChain

@Composable
fun ColumnScope.PTTaxSection(
    inputId: String,
    sectionId: String,
    store: Store,
    currencyRepository: CurrencyRepository,
    onMoneyPerWeekChangedTax: (ArithmeticChain?) -> Unit,
    onMoneyPerWeekChangedCurrency: (ArithmeticChain?) -> Unit,
) {
    val selectedCurrency by store.currencies[inputId]
        .collectAsState(initial = null)
    val (fromCurrency, toCurrency) = selectedCurrency ?: (null to null)

    if (fromCurrency == null || toCurrency != "EUR") {
        return
    }

    FeeSection(
        inputId = inputId,
        sectionId = "$sectionId/$SUB_SECTION_TAX",
        title = "PT Tax",
        store = store,
        onMoneyPerWeekChanged = onMoneyPerWeekChangedTax,
    )

    CurrencySection(
        inputId = "$sectionId/$SUB_SECTION_TAX",
        sectionId = "$sectionId/$SUB_SECTION_CURRENCY",
        title = "Currency",
        store = store,
        currencyRepository = currencyRepository,
        onMoneyPerWeekChanged = onMoneyPerWeekChangedCurrency,
    )
}