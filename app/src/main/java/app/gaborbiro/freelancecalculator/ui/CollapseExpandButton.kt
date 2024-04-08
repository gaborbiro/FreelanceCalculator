package app.gaborbiro.freelancecalculator.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowLeft
import androidx.compose.material.icons.outlined.KeyboardArrowRight
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import app.gaborbiro.freelancecalculator.persistence.domain.TypedSubStore
import app.gaborbiro.freelancecalculator.ui.theme.PADDING_LARGE

@Composable
fun CollapseExpandButton(modifier: Modifier, id: String, sectionExpander: TypedSubStore<Boolean>) {
    val expanded: Boolean? by sectionExpander[id].collectAsState(initial = true)
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
                        sectionExpander[id] = false
                    },
                imageVector = Icons.Outlined.KeyboardArrowUp,
                contentDescription = "collapse",
            )
        } else {
            Icon(
                modifier = iconModifier
                    .clickable {
                        sectionExpander[id] = true
                    },
                imageVector = Icons.Outlined.KeyboardArrowDown,
                contentDescription = "expand",
            )
        }
    }
}