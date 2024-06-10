package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
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
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE


@ExperimentalLayoutApi
@Composable
fun FlowCard(
    modifier: Modifier,
    title: String? = null,
    extraContent: (@Composable ColumnScope.() -> Unit)? = null,
    items: @Composable RowScope.() -> Unit,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(top = PADDING_LARGE, start = PADDING_LARGE, end = PADDING_LARGE),
        title = title,
    ) {
        Column {
            extraContent?.invoke(this)
            val topPadding =
                if (title.isNullOrBlank() || extraContent != null) 0.dp else PADDING_LARGE
            FlowRow(
                modifier = Modifier
                    .padding(top = topPadding),
                horizontalArrangement = Arrangement.Start,
                content = items,
            )
        }
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
        FlowCard(modifier = modifier, title = "Title") {
            InputField(
                modifier = Modifier,
                label = "Input Field",
                value = "123.45",
                outlined = true,
                clearButtonVisible = false,
                onValueChange = { },
            )
            InputField(
                modifier = Modifier,
                label = "Input Field",
                value = "123.45",
                outlined = true,
                clearButtonVisible = false,
                onValueChange = { },
            )
            InputField(
                modifier = Modifier,
                label = "Input Field Looooooonger",
                value = "123.45",
                outlined = true,
                clearButtonVisible = false,
                onValueChange = { },
            )
        }
    }
}