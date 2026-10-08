package com.family.dawa.presentation.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.core.time.ArabicFormatters

@Composable
fun PillPictogram(
    quantityHalves: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val pills = quantityHalves / 2
        val hasHalf = (quantityHalves % 2) != 0

        if (hasHalf && pills == 0) {
            Text("½ 💊", fontSize = 38.sp)
        } else {
            val displayPills = Math.min(pills, 4)
            for (i in 0 until displayPills) {
                Text("💊", fontSize = 38.sp)
                Spacer(modifier = Modifier.width(4.dp))
            }
            if (hasHalf) {
                Text("½", fontSize = 32.sp)
            }
            if (pills > 4) {
                Text("+${ArabicFormatters.toArabicDigits(pills - 4)}", fontSize = 30.sp)
            }
        }
    }
}
