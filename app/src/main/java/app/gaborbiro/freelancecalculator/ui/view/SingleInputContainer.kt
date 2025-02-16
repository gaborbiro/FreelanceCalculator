package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE


@ExperimentalMaterial3Api
@Composable
fun SingleInputContainer(
    containerModifier: Modifier,
    label: String,
    value: String,
    clearButtonVisible: Boolean = false,
    selected: Boolean,
    onPinButtonTapped: suspend () -> Unit,
    onValueChanged: suspend (Double?) -> Unit,
) {
    PinnedContainer(
        modifier = containerModifier,
        pinned = selected,
        onPinButtonTapped = onPinButtonTapped,
    ) { modifier ->
        FocusPinnedInputField(
            modifier = modifier,
            label = label,
            value = value,
            outlined = true,
            clearButtonVisible = clearButtonVisible,
            onValueChanged = onValueChanged,
        )
    }
}

@ExperimentalMaterial3Api
@Preview
@Composable
private fun SingleInputContainerPreview() {
    SingleInputContainer(
        containerModifier = Modifier
            .fillMaxWidth()
            .padding(PADDING_LARGE),
        label = "Fee per hour",
        value = "80.00",
        clearButtonVisible = true,
        selected = true,
        onPinButtonTapped = { },
        onValueChanged = { },
    )
}