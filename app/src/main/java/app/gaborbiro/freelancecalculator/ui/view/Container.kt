package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.RadioButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_LARGE

@Composable
fun Container(
    modifier: Modifier,
    selected: Boolean,
    onSelected: () -> Unit,
    content: @Composable RowScope.(modifier: Modifier) -> Unit
) {
    Card(modifier = modifier) {
        Row {
            content(
                Modifier
                    .weight(1f)
            )
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(top = 12.dp),
                selected = selected,
                onClick = onSelected,
            )
        }
    }
}

@ExperimentalMaterial3Api
@Preview
@Composable
private fun ContainerPreview() {
    Container(
        modifier = Modifier.padding(MARGIN_LARGE),
        selected = true,
        onSelected = { }) { modifier ->
        InputField(
            modifier = modifier,
            label = "Input Field Label",
            value = "123.45",
            decimalCount = 2,
            onValueChange = { },
        )
    }
}
