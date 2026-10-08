package com.family.dawa.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.family.dawa.domain.model.DoseItem

/**
 * Shows every medicine of a slot inside the given space, with no scrolling, so the caregiver
 * can never miss a medicine (or its quantity) hidden below the fold.
 * One column for 1–2 medicines, two columns for more; rows share the height equally, the card
 * style gets tighter as the count grows, and each photo takes the space left. A lone card in
 * the last row is centred.
 */
@Composable
fun DoseCardGrid(
    items: List<DoseItem>,
    modifier: Modifier = Modifier,
    spacing: Dp = 10.dp
) {
    val columns = if (items.size <= 2) 1 else 2
    val rows = items.chunked(columns)
    val style = when (items.size) {
        1 -> DoseCardStyle.Regular
        2 -> DoseCardStyle.Wide
        3, 4 -> DoseCardStyle.Compact
        else -> DoseCardStyle.Minimal
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        rows.forEach { rowItems ->
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                val emptyCells = columns - rowItems.size
                if (emptyCells > 0) Spacer(modifier = Modifier.weight(emptyCells / 2f))
                rowItems.forEach { item ->
                    DoseCard(
                        item = item,
                        style = style,
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                    )
                }
                if (emptyCells > 0) Spacer(modifier = Modifier.weight(emptyCells / 2f))
            }
        }
    }
}
