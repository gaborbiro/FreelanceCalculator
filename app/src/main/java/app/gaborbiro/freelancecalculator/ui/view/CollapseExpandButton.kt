package app.gaborbiro.freelancecalculator.ui.view

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.persistence.domain.MapPrefsDelegate
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE
import kotlinx.coroutines.runBlocking

@Composable
fun CollapseExpandButton(
    modifier: Modifier,
    collapseId: String,
    sectionExpander: MapPrefsDelegate<Boolean>
) {
    val expanded: Boolean? by sectionExpander[collapseId].collectAsState()

    Box(
        modifier = modifier
    ) {
        val iconModifier = Modifier
            .size(48.dp)
            .padding(PADDING_LARGE)
            .align(Alignment.CenterEnd)

        if (expanded != false) {
            Icon(
                modifier = iconModifier
                    .clickable {
                        runBlocking {
                            sectionExpander[collapseId].emit(false)
                        }
                    },
                imageVector = Icons.Outlined.KeyboardArrowUp,
                contentDescription = "collapse",
            )
        } else {
            Icon(
                modifier = iconModifier
                    .clickable {
                        runBlocking {
                            sectionExpander[collapseId].emit(true)
                        }
                    },
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = "expand",
            )
        }
    }
}