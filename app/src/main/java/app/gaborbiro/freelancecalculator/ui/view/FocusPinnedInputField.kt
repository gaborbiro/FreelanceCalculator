package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import app.gaborbiro.freelancecalculator.util.hide.strictParse
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.debounce
import kotlin.time.Duration.Companion.milliseconds

@ExperimentalMaterial3Api
@Composable
fun FocusPinnedInputField(
    modifier: Modifier,
    label: String,
    value: String,
    outlined: Boolean,
    clearButtonVisible: Boolean = false,
    onValueChanged: (Double?) -> Unit,
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
        val number = it.replace(",", "").strictParse()
        if (number != 0.0) {
            if (value.replace(",", "").strictParse() != number) {
                onValueChanged(number)
            }
        } else {
            onValueChanged(null)
        }
        value = it
    }
}
