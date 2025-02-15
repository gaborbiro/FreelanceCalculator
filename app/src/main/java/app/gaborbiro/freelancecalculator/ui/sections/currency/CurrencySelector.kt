package app.gaborbiro.freelancecalculator.ui.sections.currency

import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rxjava2.subscribeAsState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.PopupProperties
import app.gaborbiro.freelancecalculator.repo.currency.domain.CurrencyRepository
import app.gaborbiro.freelancecalculator.util.Lce
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch

@OptIn(
    ExperimentalMaterial3Api::class,
    ExperimentalComposeUiApi::class
)
@Composable
fun currencySelector(
    modifier: Modifier,
    label: String,
    selectedCurrency: String?,
    currencyRepository: CurrencyRepository,
): Flow<String?> {
    val output = remember(label) { MutableSharedFlow<String?>(
        replay = 1,
        onBufferOverflow = BufferOverflow.SUSPEND
    ) }

    var expanded by remember {
        mutableStateOf(false)
    }
    var currentText by remember(selectedCurrency) {
        mutableStateOf(
            TextFieldValue(text = selectedCurrency ?: "")
        )
    }
    var filter: String? by rememberSaveable {
        mutableStateOf(null)
    }
    var enabled by rememberSaveable {
        mutableStateOf(true)
    }
    var currencies: List<String> by rememberSaveable {
        mutableStateOf(emptyList())
    }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val scope = rememberCoroutineScope()

    val data = remember {
        currencyRepository.currencies
    }
        .subscribeAsState(initial = Lce.Loading).value

    when (data) {
        is Lce.Data -> {
            currencies = data.data
            enabled = true
            selectedCurrency?.let {
                if (data.data.contains(it).not()) {
                    currentText = TextFieldValue("")
                    SideEffect {
                        output.tryEmit(null)
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

    val filteredCurrencies = remember(filter, currencies) {
        if (filter.isNullOrBlank()) {
            currencies
        } else {
            currencies.filter { it.contains(filter!!, ignoreCase = true) }
        }
    }

    BackHandler(enabled = expanded) {
        expanded = false
        focusManager.clearFocus(force = true)
    }

    Box(Modifier.imePadding()) {
        TextField(
            modifier = modifier
                .wrapContentSize()
                .onFocusChanged {
                    if (it.hasFocus) {
                        scope.launch {
                            delay(10)
                            currentText = currentText.copy(
                                selection = TextRange(0, currentText.text.length)
                            )
                        }
                    }
                    expanded = it.hasFocus
                },
            value = currentText,
            label = { Text(label) },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent,
            ),
            onValueChange = {
                if (currentText.text != it.text) {
                    filter = it.text.uppercase()
                }
                currentText = it.copy(text = it.text.uppercase())
            },
            trailingIcon = {
                Icon(
                    Icons.Filled.ArrowDropDown,
                    if (expanded) "Open currency picker" else "Close currency picker",
                    Modifier
                        .rotate(if (expanded) 180f else 0f)
                )
            },
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = when (filteredCurrencies.size) {
                    0, 1 -> ImeAction.Done
                    else -> ImeAction.Search
                }
            ),
            keyboardActions = KeyboardActions(
                onAny = {
                    when (filteredCurrencies.size) {
                        0 -> {
                            focusManager.clearFocus(force = true)
                        }

                        1 -> {
                            expanded = false
                            focusManager.clearFocus(force = true)
                            currentText = currentText.copy(text = filteredCurrencies[0])
                            scope.launch {
                                delay(200)
                                output.tryEmit(filteredCurrencies[0])
                                filter = null
                            }
                        }

                        else -> keyboardController?.hide()
                    }
                }
            ),
        )

        if (expanded) {
            DropdownMenu(
                modifier = Modifier
                    .imePadding()
                    .heightIn(max = 250.dp),
                expanded = expanded,
                onDismissRequest = {
                },
                properties = PopupProperties(
                    focusable = false,
                )
            ) {
                if (expanded) {
                    if (filteredCurrencies.isEmpty()) {
                        DropdownMenuItem(
                            onClick = {},
                            enabled = false,
                            text = {
                                Text(
                                    "No match",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontStyle = FontStyle.Italic,
                                )
                            }
                        )
                    } else {
                        filteredCurrencies.forEach { currency ->
                            DropdownMenuItem(
                                modifier = Modifier
                                    .wrapContentSize(),
                                text = { Text(currency) },
                                onClick = {
                                    expanded = expanded.not()
                                    focusManager.clearFocus(force = true)
                                    currentText = TextFieldValue(currency)
                                    scope.launch {
                                        delay(200)
                                        output.tryEmit(currency)
                                        filter = null
                                    }
                                },
                            )
                        }
                    }
                }
            }
        }
    }
    return output
}

@ExperimentalLayoutApi
@ExperimentalMaterial3Api
@Preview
@Composable
private fun CurrencySelectorPreview() {
    Row {
        currencySelector(
            modifier = Modifier.weight(1f),
            selectedCurrency = "USD",
            label = "from",
            currencyRepository = CurrencyRepository.dummyImplementation(),
        )
        currencySelector(
            modifier = Modifier.weight(1f),
            selectedCurrency = "GBP",
            label = "to",
            currencyRepository = CurrencyRepository.dummyImplementation(),
        )
    }
}