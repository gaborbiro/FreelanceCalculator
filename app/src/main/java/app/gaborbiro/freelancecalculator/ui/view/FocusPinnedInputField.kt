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
fun focusPinnedInputField(
    modifier: Modifier,
    label: String,
    value: String,
    outlined: Boolean,
    clearButtonVisible: Boolean = false,
): Flow<Double?> {
    var isFocused by rememberSaveable { mutableStateOf(false) }
    var previousValue: String by rememberSaveable { mutableStateOf("") }
    var value by rememberSaveable(value) {
        mutableStateOf(if (isFocused) previousValue else value)
    }
    val output = remember { MutableSharedFlow<Double?>(extraBufferCapacity = 1) }

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
                output.tryEmit(number)
            }
        } else {
            output.tryEmit(null)
        }
        value = it
    }
    return remember {
        output
            .debounce(300.milliseconds)
    }
}
