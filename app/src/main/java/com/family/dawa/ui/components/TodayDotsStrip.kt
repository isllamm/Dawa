package com.family.dawa.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.domain.model.SlotStatus
import com.family.dawa.domain.model.SlotWithStatus
import com.family.dawa.ui.theme.AmberPrimary
import com.family.dawa.ui.theme.GreenPrimary
import com.family.dawa.ui.theme.RedPrimary

@Composable
fun TodayDotsStrip(
    todaySlots: List<SlotWithStatus>,
    modifier: Modifier = Modifier
) {
    if (todaySlots.isEmpty()) return

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        todaySlots.forEachIndexed { index, slotWithStatus ->
            DotItem(status = slotWithStatus.status)
            if (index < todaySlots.size - 1) {
                Spacer(modifier = Modifier.width(16.dp))
            }
        }
    }
}

@Composable
private fun DotItem(status: SlotStatus) {
    val (bgColor, borderColor, symbol) = when (status) {
        SlotStatus.COMPLETED -> Triple(GreenPrimary, GreenPrimary, "✓")
        SlotStatus.DUE -> Triple(AmberPrimary, AmberPrimary, "🔔")
        SlotStatus.MISSED -> Triple(RedPrimary, RedPrimary, "✕")
        SlotStatus.UPCOMING, SlotStatus.SKIPPED -> Triple(Color.White, Color.Gray, "")
    }

    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(bgColor)
            .border(2.dp, borderColor, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        if (symbol.isNotEmpty()) {
            Text(
                text = symbol,
                color = Color.White,
                fontSize = if (symbol == "🔔") 18.sp else 22.sp
            )
        }
    }
}
