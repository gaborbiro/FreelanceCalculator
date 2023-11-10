package app.gaborbiro.freelancecalculator.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE
import app.gaborbiro.freelancecalculator.ui.view.Container
import app.gaborbiro.freelancecalculator.ui.view.InputField

@ExperimentalMaterial3Api
@Composable
fun SingleInputContainerContent(
    modifier: Modifier,
    label: String,
    value: String,
    decimalCount: Int,
    clearButtonVisible: Boolean,
    onValueChange: (value: String) -> Unit,
) {
    InputField(
        modifier = modifier,
        label = label,
        value = value,
        decimalCount = decimalCount,
        clearButtonVisible = clearButtonVisible,
        onValueChange = onValueChange,
    )
}

@ExperimentalMaterial3Api
@Composable
fun SingleInputContainer(
    containerModifier: Modifier,
    label: String,
    value: String,
    decimalCount: Int,
    clearButtonVisible: Boolean = false,
    selected: Boolean,
    onSelected: () -> Unit,
    onValueChange: (value: String) -> Unit,
) {
    Container(
        modifier = containerModifier,
        selected = selected,
        onSelected = onSelected,
    ) { modifier ->
        SingleInputContainerContent(
            modifier = modifier,
            label = label,
            value = value,
            decimalCount = decimalCount,
            clearButtonVisible = clearButtonVisible,
            onValueChange = onValueChange
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
            .padding(MARGIN_LARGE),
        label = "Fee per hour",
        value = "80.00",
        decimalCount = 2,
        clearButtonVisible = true,
        selected = true,
        onSelected = { },
        onValueChange = { },
    )
}