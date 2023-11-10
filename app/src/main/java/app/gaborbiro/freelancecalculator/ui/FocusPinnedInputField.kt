package app.gaborbiro.freelancecalculator.ui

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.gaborbiro.freelancecalculator.strictParse
import app.gaborbiro.freelancecalculator.ui.view.InputField
import java.math.BigDecimal

@ExperimentalMaterial3Api
@Composable
fun FocusPinnedInputField(
    modifier: Modifier,
    label: String,
    value: String,
    clearButtonVisible: Boolean = false,
    onValueChange: (value: BigDecimal?) -> Unit
) {
    var isFocused by rememberSaveable { mutableStateOf(false) }
    var previousValue: String by rememberSaveable { mutableStateOf("") }
    var value by rememberSaveable(value) {
        mutableStateOf(if (isFocused) previousValue else value)
    }
    InputField(
        modifier = modifier,
        label = label,
        value = value,
        clearButtonVisible = clearButtonVisible,
        onFocusChanged = {
            isFocused = it
        },
    ) {
        previousValue = it
        val number = it.strictParse()
        if (value.strictParse() != number) {
            onValueChange(number)
        }
        value = it
    }
}
