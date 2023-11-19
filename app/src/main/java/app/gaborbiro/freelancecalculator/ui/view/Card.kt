package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE

@Composable
fun Card(modifier: Modifier, content: @Composable () -> Unit) {
    Surface(
        modifier = modifier
            .wrapContentHeight()
            .border(
                width = .5.dp,
                color = MaterialTheme.colorScheme.surfaceVariant,
                shape = RoundedCornerShape(2.dp)
            ),
        content = content
    )
}

@ExperimentalMaterial3Api
@Preview
@Composable
private fun CardPreview() {
    Card(modifier = Modifier.padding(PADDING_LARGE)) {
        Row {
            InputField(
                modifier = Modifier.weight(1f),
                label = "Input Field Label",
                value = "123.45",
                onValueChange = { },
            )
            Spacer(modifier = Modifier.width(16.dp))
            RadioButton(
                modifier = Modifier
                    .align(Alignment.Top)
                    .padding(top = 12.dp),
                selected = true,
                onClick = { },
            )
        }
    }
}