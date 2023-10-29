package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.ui.theme.MARGIN_MEDIUM

@ExperimentalMaterial3Api
@Composable
fun InputField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    clearButtonVisible: Boolean = false,
    onValueChange: (value: String) -> Unit
) {
    val customTextSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,
        backgroundColor = Color.Transparent
    )
    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        BasicTextField(
            modifier = modifier
                .padding(MARGIN_MEDIUM),
            value = TextFieldValue(value, selection = TextRange(value.length)),
            textStyle = MaterialTheme.typography.bodyMedium
                .merge(TextStyle(color = LocalContentColor.current)),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
            onValueChange = {
                onValueChange(it.text)
            },
            cursorBrush = SolidColor(LocalContentColor.current),
        ) {
            TextFieldDefaults.TextFieldDecorationBox(
                value = value,
                label = { Text(text = label) },
                enabled = true,
                innerTextField = it,
                interactionSource = remember { MutableInteractionSource() },
                singleLine = true,
                visualTransformation = VisualTransformation.None,
                contentPadding = TextFieldDefaults.textFieldWithLabelPadding(
                    start = 8.dp,
                    end = 4.dp,
                ),
                colors = TextFieldDefaults.textFieldColors(containerColor = Color.Transparent),
                trailingIcon = if (clearButtonVisible) {
                    {
                        IconButton(
                            onClick = {
                                onValueChange("")
                            },
                        ) {
                            Icon(
                                Icons.Default.Clear,
                                contentDescription = "clear",
                            )
                        }
                    }
                } else null,
            )
        }
    }
}

@ExperimentalMaterial3Api
@Preview
@Composable
private fun InputFieldPreview() {
    InputField(
        modifier = Modifier,
        label = "Input Field Label",
        value = "123.45",
        onValueChange = { },
    )
}