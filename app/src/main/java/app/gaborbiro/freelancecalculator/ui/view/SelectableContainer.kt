package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.R
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import kotlinx.coroutines.runBlocking

@Composable
fun SelectableContainer(
    modifier: Modifier,
    selected: Boolean,
    onPinButtonTapped: suspend () -> Unit,
    content: @Composable RowScope.(modifier: Modifier) -> Unit
) {
    Card(modifier = modifier) {
        Row(
            verticalAlignment = Alignment.Top
        ) {
            content(
                Modifier
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Icon(
                modifier = Modifier
                    .padding(16.dp)
                    .clickable(onClick = {
                        runBlocking { onPinButtonTapped() }
                    }),
                painter = if (selected) painterResource(R.drawable.keep_on) else painterResource(
                    R.drawable.keep_off
                ),
                contentDescription = if (selected) "unpin this field" else "pin this field"
            )
        }
    }
}

@ExperimentalMaterial3Api
@Preview
@Composable
private fun ContainerPreview() {
    SelectableContainer(
        modifier = Modifier.padding(PADDING_LARGE),
        selected = true,
        onPinButtonTapped = { }) { modifier ->
        InputField(
            modifier = modifier,
            label = "Input Field Label",
            value = "123.45",
            outlined = true,
        ) {}
    }
}

@ExperimentalMaterial3Api
@Preview
@Composable
private fun ContainerPreviewUnselected() {
    SelectableContainer(
        modifier = Modifier.padding(PADDING_LARGE),
        selected = false,
        onPinButtonTapped = { }) { modifier ->
        InputField(
            modifier = modifier,
            label = "Input Field Label",
            value = "123.45",
            outlined = true,
        ) {}
    }
}