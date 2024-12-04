package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow


@ExperimentalMaterial3Api
@Composable
fun singleInputContainer(
    containerModifier: Modifier,
    label: String,
    value: String,
    clearButtonVisible: Boolean = false,
    selected: Boolean,
    onSelected: () -> Unit,
): Flow<Double?> {
    val output: MutableSharedFlow<Double?> = remember(label) {
        MutableSharedFlow(
            replay = 1,
            onBufferOverflow = BufferOverflow.SUSPEND
        )
    }

    SelectableContainer(
        modifier = containerModifier,
        selected = selected,
        onSelected = onSelected,
    ) { modifier ->
        val inputFieldOutput = focusPinnedInputField(
            modifier = modifier,
            label = label,
            value = value,
            outlined = true,
            clearButtonVisible = clearButtonVisible,
        )
        LaunchedEffect(Unit) {
            inputFieldOutput.collect {
                output.tryEmit(it)
            }
        }
    }
    return output
}

@ExperimentalMaterial3Api
@Preview
@Composable
private fun SingleInputContainerPreview() {
    singleInputContainer(
        containerModifier = Modifier
            .fillMaxWidth()
            .padding(PADDING_LARGE),
        label = "Fee per hour",
        value = "80.00",
        clearButtonVisible = true,
        selected = true,
        onSelected = { },
    )
}