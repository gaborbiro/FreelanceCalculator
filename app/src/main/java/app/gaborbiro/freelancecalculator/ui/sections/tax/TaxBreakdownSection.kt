package app.gaborbiro.freelancecalculator.ui.sections.tax

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.persistence.domain.MapDelegate
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_HALF
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_MEDIUM
import app.gaborbiro.freelancecalculator.ui.view.CollapseExpandButton
import app.gaborbiro.freelancecalculator.util.ArithmeticChain
import app.gaborbiro.freelancecalculator.util.chainify

@Composable
fun TaxBreakdownSection(
    taxModel: TaxBreakdownUIModel,
    sectionExpander: MapDelegate<Boolean, Boolean>,
) {
    Row {
        Column(
            modifier = Modifier
                .wrapContentSize()
                .padding(bottom = PADDING_LARGE, top = PADDING_LARGE)
                .weight(.85f)
        ) {
            val expanded: Boolean? by sectionExpander["tax"].collectAsState(initial = true)

            AnimatedVisibility(
                enter = fadeIn(),
                exit = fadeOut(),
                visible = expanded != false
            ) {
                TaxBreakdownEntry(title = "Income tax:", value = taxModel.incomeTax)
            }

            AnimatedVisibility(
                enter = fadeIn(),
                exit = fadeOut(),
                visible = expanded != false
            ) {
                TaxBreakdownEntry(title = "NIC2:", value = taxModel.nic2Tax)
            }

            AnimatedVisibility(
                enter = fadeIn(),
                exit = fadeOut(),
                visible = expanded != false
            ) {
                TaxBreakdownEntry(title = "NIC4:", value = taxModel.nic4Tax)
            }
            if (expanded != false) {
                Divider(
                    modifier = Modifier
                        .padding(
                            start = PADDING_MEDIUM,
                            end = PADDING_MEDIUM,
                            top = PADDING_HALF
                        )
                        .fillMaxWidth()
                        .height(1.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                )
            }

            TaxBreakdownEntry(title = "Total tax:", value = taxModel.totalTax)
        }
        CollapseExpandButton(
            modifier = Modifier
                .align(Alignment.Bottom)
                .weight(.15f),
            id = "tax",
            sectionExpander = sectionExpander
        )
    }
}


@Composable
private fun TaxBreakdownEntry(title: String, value: String) {
    Row {
        Text(
            modifier = Modifier
                .weight(.4f)
                .padding(start = PADDING_LARGE, top = PADDING_LARGE)
                .alignByBaseline(),
            text = title,
            style = MaterialTheme.typography.bodyLarge,
        )
        Text(
            modifier = Modifier
                .weight(.6f)
                .alignByBaseline(),
            text = value,
            style = MaterialTheme.typography.bodyMedium,
        )
    }
}

class TaxBreakdownUIModel(
    val incomeTax: String,
    val nic2Tax: String,
    val nic4Tax: String,
    val totalTax: String,
    val afterTaxPerWeek: ArithmeticChain,
) {
    companion object {
        fun dummyData() = TaxBreakdownUIModel(
            incomeTax = "26,330.56 (allowance: 12,570)",
            nic2Tax = "179.40",
            nic4Tax = "4,332.53 (allowance: 12,570)",
            totalTax = "30,570.48",
            afterTaxPerWeek = 1273.0.chainify()!!,
        )
    }
}

@Preview
@Composable
private fun TaxBreakdownSectionPreview() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = PADDING_LARGE)
    ) {
        TaxBreakdownSection(
            taxModel = TaxBreakdownUIModel.dummyData(),
            sectionExpander = MapDelegate.dummyImplementation()
        )
    }
}