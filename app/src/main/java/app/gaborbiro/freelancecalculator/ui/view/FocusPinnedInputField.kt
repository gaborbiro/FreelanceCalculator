package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.gaborbiro.freelancecalculator.util.hide.strictParse
import app.gaborbiro.freelancecalculator.ui.view.InputField

@ExperimentalMaterial3Api
@Composable
fun FocusPinnedInputField(
    modifier: Modifier,
    label: String,
    value: String,
    outlined: Boolean,
    clearButtonVisible: Boolean = false,
    onValueChange: (value: Double?) -> Unit
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
        outlined = outlined,
        clearButtonVisible = clearButtonVisible,
        onFocusChanged = {
            isFocused = it
        },
    ) {
        previousValue = it
        val number = it.strictParse()
        if (number != 0.0) {
            if (value.strictParse() != number) {
                onValueChange(number)
            }
        } else {
            onValueChange(null)
        }
        value = it
    }
}
