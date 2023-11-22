package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_DOUBLE
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE

@Composable
fun Card(
    modifier: Modifier,
    title: String? = null,
    content: @Composable () -> Unit
) {
    val topPadding = if (title.isNullOrBlank().not()) PADDING_DOUBLE else 0.dp

    Box(
        modifier = modifier
            .wrapContentHeight()
    ) {
        Surface(
            modifier = Modifier
                .wrapContentHeight()
                .padding(top = topPadding)
                .border(
                    width = .5.dp,
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ),
            content = content,
        )
        if (title.isNullOrBlank().not()) {
            Box(
                modifier = Modifier
                    .padding(start = PADDING_LARGE)
            ) {
                Text(
                    modifier = Modifier
                        .wrapContentSize()
                        .background(MaterialTheme.colorScheme.background),
                    text = " $title ",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Start,
                )
            }

        }
    }
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