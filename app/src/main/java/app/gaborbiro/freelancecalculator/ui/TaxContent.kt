package app.gaborbiro.freelancecalculator.ui

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
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.persistence.domain.Store
import app.gaborbiro.freelancecalculator.persistence.domain.TypedSubStore
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_MEDIUM
import app.gaborbiro.freelancecalculator.ui.view.Card
import app.gaborbiro.freelancecalculator.util.ArithmeticChain

@Composable
fun TaxContent(taxInfo: TaxCalculationUIModel, store: Store) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = PADDING_LARGE,
                end = PADDING_LARGE,
                top = PADDING_LARGE
            ),
        title = "Tax",
    ) {
        Row {
            val sectionExpander: TypedSubStore<Boolean> = store.sectionExpander()

            Column(
                modifier = Modifier
                    .wrapContentSize()
                    .padding(bottom = PADDING_LARGE, top = PADDING_LARGE)
                    .weight(.85f)
            ) {
                val expanded: Boolean? by sectionExpander["tax"].collectAsState(initial = false)

                AnimatedVisibility(
                    enter = fadeIn(),
                    exit = fadeOut(),
                    visible = expanded != false
                ) {
                    TaxInfoRow(title = "Income tax:", value = taxInfo.incomeTax)
                }

                AnimatedVisibility(
                    enter = fadeIn(),
                    exit = fadeOut(),
                    visible = expanded != false
                ) {
                    TaxInfoRow(title = "NIC2:", value = taxInfo.nic2Tax)
                }

                AnimatedVisibility(
                    enter = fadeIn(),
                    exit = fadeOut(),
                    visible = expanded != false
                ) {
                    TaxInfoRow(title = "NIC4:", value = taxInfo.nic4Tax)
                }
                if (expanded != false) {
                    Divider(
                        modifier = Modifier
                            .padding(start = PADDING_MEDIUM, end = PADDING_MEDIUM)
                            .fillMaxWidth()
                            .height(1.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                    )
                }
                TaxInfoRow(title = "Total tax:", value = taxInfo.totalTax)
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
}


@Composable
private fun TaxInfoRow(title: String, value: String) {
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

class TaxCalculationUIModel(
    val incomeTax: String,
    val nic2Tax: String,
    val nic4Tax: String,
    val totalTax: String,
    val afterTaxPerWeek: ArithmeticChain,
)
