package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.animation.Animatable
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_MEDIUM

@ExperimentalMaterial3Api
@Composable
fun InputField(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
    outlined: Boolean,
    clearButtonVisible: Boolean = false,
    onFocusChanged: ((isFocused: Boolean) -> Unit)? = null,
    onValueChange: (value: String) -> Unit,
) {
    val customTextSelectionColors = TextSelectionColors(
        handleColor = Color.Transparent,
        backgroundColor = Color.Transparent
    )
    CompositionLocalProvider(LocalTextSelectionColors provides customTextSelectionColors) {
        BasicTextField(
            modifier = modifier
                .padding(PADDING_LARGE)
                .onFocusChanged {
                    onFocusChanged?.invoke(it.isFocused)
                },
            value = TextFieldValue(value, selection = TextRange(value.length)),
            textStyle = MaterialTheme.typography.bodyMedium
                .merge(TextStyle(color = LocalContentColor.current)),
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Decimal),
            onValueChange = {
                onValueChange(it.text)
            },
            visualTransformation = DecimalGroupingDecorator(),
            cursorBrush = SolidColor(LocalContentColor.current),
        ) {
            if (outlined) {
                val color = remember { Animatable(Color.Gray) }
                LaunchedEffect(value) {
                    color.animateTo(Color.Red, animationSpec = tween(300))
                    color.animateTo(Color.Transparent, animationSpec = tween(300))
                }
                OutlineDecorationBox(
                    value = value,
                    label = label,
                    innerTextField = it,
                    clearButtonVisible = clearButtonVisible,
                    onValueChange = onValueChange,
                )
            } else {
                UnderlineDecorationBox(
                    value = value,
                    label = label,
                    innerTextField = it,
                    clearButtonVisible = clearButtonVisible,
                    onValueChange = onValueChange,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OutlineDecorationBox(
    value: String,
    label: String,
    innerTextField: @Composable () -> Unit,
    clearButtonVisible: Boolean,
    onValueChange: (value: String) -> Unit,
) {
    return TextFieldDefaults.OutlinedTextFieldDecorationBox(
        value = value,
        label = { Text(text = label) },
        enabled = true,
        innerTextField = innerTextField,
        interactionSource = remember { MutableInteractionSource() },
        singleLine = true,
        visualTransformation = VisualTransformation.None,
        contentPadding = TextFieldDefaults.textFieldWithLabelPadding(
            start = 8.dp,
            end = 4.dp,
        ),
        colors = TextFieldDefaults.outlinedTextFieldColors(),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun UnderlineDecorationBox(
    value: String,
    label: String,
    innerTextField: @Composable () -> Unit,
    clearButtonVisible: Boolean,
    onValueChange: (value: String) -> Unit,
) {
    return TextFieldDefaults.TextFieldDecorationBox(
        value = value,
        label = { Text(text = label) },
        enabled = true,
        innerTextField = innerTextField,
        interactionSource = remember { MutableInteractionSource() },
        singleLine = true,
        visualTransformation = VisualTransformation.None,
        contentPadding = TextFieldDefaults.textFieldWithLabelPadding(
            start = 8.dp,
            end = 4.dp,
        ),
        colors = TextFieldDefaults.textFieldColors(
            containerColor = Color.Transparent,
        ),
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

@ExperimentalMaterial3Api
@Preview
@Composable
private fun InputFieldPreview() {
    InputField(
        modifier = Modifier,
        label = "Input Field Label",
        value = "123.1453",
        outlined = true,
        onFocusChanged = { },
        onValueChange = { },
    )
    InputField(
        modifier = Modifier,
        label = "Input Field Label",
        value = "123.1453",
        outlined = false,
        onFocusChanged = { },
        onValueChange = { },
    )
}