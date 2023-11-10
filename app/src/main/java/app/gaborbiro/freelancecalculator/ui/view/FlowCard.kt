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
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE


@ExperimentalLayoutApi
@Composable
fun FlowCard(modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(MARGIN_LARGE)
    ) {
        FlowRow(
            horizontalArrangement = Arrangement.Start,
            content = content,
        )
    }
}

@ExperimentalMaterial3Api
@ExperimentalLayoutApi
@Preview
@Composable
private fun FlowCardPreview() {
    Container(
        modifier = Modifier
            .fillMaxWidth()
            .padding(MARGIN_LARGE),
        selected = true,
        onSelected = { }
    ) { modifier ->
        FlowCard(modifier = modifier) {
            InputField(
                modifier = Modifier,
                label = "Input Field",
                value = "123.45",
                decimalCount = 2,
                clearButtonVisible = false,
                onValueChange = { },
            )
            InputField(
                modifier = Modifier,
                label = "Input Field",
                value = "123.45",
                decimalCount = 2,
                clearButtonVisible = false,
                onValueChange = { },
            )
            InputField(
                modifier = Modifier,
                label = "Input Field Looooooonger",
                value = "123.45",
                decimalCount = 2,
                clearButtonVisible = false,
                onValueChange = { },
            )
        }
    }
}