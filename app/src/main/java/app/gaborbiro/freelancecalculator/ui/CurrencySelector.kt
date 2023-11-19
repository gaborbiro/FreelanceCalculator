package app.gaborbiro.freelancecalculator.ui

import android.widget.Toast
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.util.Lce
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CurrencySelector(
    modifier: Modifier,
    selectedCurrency: String?,
    currencyRepository: CurrencyRepository,
    onCurrencySelected: (currency: String?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    var currentText by remember(selectedCurrency) {
        mutableStateOf(
            TextFieldValue(text = selectedCurrency ?: "")
        )
    }
    var filter: String? by rememberSaveable { mutableStateOf(null) }
    var enabled by rememberSaveable { mutableStateOf(true) }
    var currencies: List<String> by rememberSaveable { mutableStateOf(emptyList()) }
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    val data = remember { currencyRepository.currencies }
        .subscribeAsState(Lce.Loading).value

    when (data) {
        is Lce.Data -> {
            currencies = data.data
            enabled = true
            LaunchedEffect(Unit) {
                selectedCurrency?.let {
                    if (data.data.contains(it).not()) {
                        currentText = TextFieldValue("")
                        onCurrencySelected(null)
                    }
                }
            }
        }

        is Lce.Error -> {
            Toast.makeText(
                LocalContext.current,
                data.throwable.message,
                Toast.LENGTH_SHORT
            ).show()
        }

        Lce.Loading -> {
            enabled = false
        }
    }

    ExposedDropdownMenuBox(
        modifier = modifier
            .wrapContentSize(),
        expanded = expanded,
        onExpandedChange = {
            expanded = expanded.not()
        },
    ) {
        TextField(
            modifier = Modifier
                .menuAnchor()
                .onFocusChanged {
                    if (it.hasFocus) {
                        scope.launch {
                            delay(10)
                            currentText = currentText.copy(
                                selection = TextRange(0, currentText.text.length)
                            )
                        }
                    } else {
                        expanded = false
                    }
                },
            value = currentText,
            enabled = enabled,
            onValueChange = {
                if (currentText.text != it.text) {
                    filter = it.text.uppercase()
                }
                currentText = it.copy(text = it.text.uppercase())
            },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
        )

        val filteredCurrencies = remember(filter, currencies) {
            if (filter.isNullOrBlank()) {
                currencies
            } else {
                currencies.filter { it.contains(filter!!, ignoreCase = true) }
            }
        }
        if (filteredCurrencies.isNotEmpty()) {
            ExposedDropdownMenu(
                modifier = Modifier
                    .wrapContentSize()
                    .heightIn(max = 250.dp),
                expanded = expanded,
                onDismissRequest = {
                    // do nothing to prevent closing on typing
                    // it can still be closed via the other interactions
                },
            ) {
                filteredCurrencies.forEach { currency ->
                    DropdownMenuItem(
                        modifier = Modifier
                            .wrapContentSize(),
                        text = { Text(currency) },
                        onClick = {
                            expanded = expanded.not()
                            focusManager.clearFocus()
                            currentText = TextFieldValue(currency)
                            scope.launch {
                                delay(200)
                                onCurrencySelected(currency)
                                filter = null
                            }
                        },
                    )
                }
            }
        }
    }
}

@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Preview
@Composable
private fun CurrencySelectorPreview() {
    Row {
        CurrencySelector(
            modifier = Modifier.weight(1f),
            selectedCurrency = "USD",
            currencyRepository = CurrencyRepository.dummyImplementation(),
        ) {}
        CurrencySelector(
            modifier = Modifier.weight(1f),
            selectedCurrency = "GBP",
            currencyRepository = CurrencyRepository.dummyImplementation(),
        ) {}
    }
}