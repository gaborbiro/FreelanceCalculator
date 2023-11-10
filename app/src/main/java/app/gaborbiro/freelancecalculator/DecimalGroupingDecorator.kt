package app.gaborbiro.freelancecalculator

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

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