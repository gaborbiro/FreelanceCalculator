package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import app.gaborbiro.freelancecalculator.util.hide.tryGrouping

class DecimalGroupingDecorator : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val finalText = text.text.tryGrouping()
        return TransformedText(
            text = AnnotatedString(finalText),
            offsetMapping = object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    return finalText.length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return text.length
                }
            }
        )
    }
}