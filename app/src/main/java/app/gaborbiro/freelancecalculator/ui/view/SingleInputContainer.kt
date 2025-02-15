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
    onSelected: () -> Unit,
    onValueChanged: (Double?) -> Unit,
) {

    SelectableContainer(
        modifier = containerModifier,
        selected = selected,
        onSelected = onSelected,
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
        onSelected = { },
        onValueChanged = { },
    )
}