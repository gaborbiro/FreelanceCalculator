package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE


@ExperimentalLayoutApi
@Composable
fun FlowCard(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = PADDING_LARGE, start = PADDING_LARGE, end = PADDING_LARGE)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.SpaceBetween,
            content = content,
        )
    }
}

@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Preview
@Composable
private fun FlowCardPreview() {
    SelectableContainer(
        modifier = Modifier
            .fillMaxWidth()
            .padding(PADDING_LARGE),
        selected = true,
        onSelected = { }
    ) { modifier ->
        FlowCard(modifier = modifier) {
            InputField(
                modifier = Modifier,
                label = "Input Field",
                value = "123.45",
                clearButtonVisible = false,
                onValueChange = { },
            )
            InputField(
                modifier = Modifier,
                label = "Input Field",
                value = "123.45",
                clearButtonVisible = false,
                onValueChange = { },
            )
            InputField(
                modifier = Modifier,
                label = "Input Field Looooooonger",
                value = "123.45",
                clearButtonVisible = false,
                onValueChange = { },
            )
        }
    }
}