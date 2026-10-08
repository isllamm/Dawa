package com.family.dawa.presentation.admin.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.SlotStatus
import com.family.dawa.domain.model.SlotWithStatus
import com.family.dawa.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@Composable
fun AdminDashboardScreen(
    viewModel: AdminDashboardViewModel = koinViewModel(),
    onNavigateMeds: () -> Unit,
    onNavigateHistory: () -> Unit,
    onNavigateContact: () -> Unit,
    onNavigateSettings: () -> Unit,
    onNavigateHealthCheck: () -> Unit,
    onNavigateDebug: () -> Unit,
    onExitAdmin: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.sendIntent(AdminDashboardIntent.CheckPermissions(context))
    }

    val totalDoses = state.timeline.size
    val completedCount = state.timeline.count { it.status == SlotStatus.COMPLETED }
    val dueCount = state.timeline.count { it.status == SlotStatus.DUE }
    val missedCount = state.timeline.count { it.status == SlotStatus.MISSED }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCalm)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "لوحة تحكم العائلة",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp,
                    color = TextPrimary
                )
                Button(
                    onClick = onExitAdmin,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "الرجوع لتيتا 🏠",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Health Check Warning Banner
        if (state.hasPermissionIssues) {
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = RedSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onNavigateHealthCheck)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("⚠️", fontSize = 32.sp)
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "تنبيه هام للصلاحيات والتشغيل",
                                fontFamily = CairoFontFamily,
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = RedPrimary
                            )
                            Text(
                                text = "اضغط هنا لفحص إذن شاومي والإشعارات لضمان رن المنبه",
                                fontFamily = CairoFontFamily,
                                fontSize = 14.sp,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }
        }

        // Stats Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "متابعة أدوية اليوم",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        StatBadge("الإجمالي", ArabicFormatters.toArabicDigits(totalDoses), TextPrimary)
                        StatBadge("تم إعطاؤه", ArabicFormatters.toArabicDigits(completedCount), GreenPrimary)
                        StatBadge("حان وقته", ArabicFormatters.toArabicDigits(dueCount), AmberPrimary)
                        StatBadge("متأخر", ArabicFormatters.toArabicDigits(missedCount), RedPrimary)
                    }
                }
            }
        }

        // Action Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NavCard("الأدوية", "إضافة وتعديل", "💊", onNavigateMeds, Modifier.weight(1f))
                    NavCard("السجل السابق", "الأيام الماضية", "📜", onNavigateHistory, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NavCard("رقم الطوارئ", "هاتف العائلة", "📞", onNavigateContact, Modifier.weight(1f))
                    NavCard("الإعدادات", "المواعيد والوجبات", "⚙️", onNavigateSettings, Modifier.weight(1f))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    NavCard("فحص الصلاحيات", "شاومي والمنبه", "🛡️", onNavigateHealthCheck, Modifier.weight(1f))
                    NavCard("أدوات التجربة", "تسريع الوقت والاختبار", "🛠️", onNavigateDebug, Modifier.weight(1f))
                }
            }
        }

        // Timeline
        item {
            Text(
                text = "جدول اليوم بالتفصيل",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        if (state.timeline.isEmpty()) {
            item {
                Text(
                    text = "لا توجد أدوية مضافة لليوم",
                    fontFamily = CairoFontFamily,
                    color = TextSecondary,
                    fontSize = 16.sp
                )
            }
        } else {
            items(state.timeline) { slotWithStatus ->
                TimelineSlotRow(slotWithStatus = slotWithStatus)
            }
        }
    }
}

@Composable
private fun StatBadge(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            color = color
        )
        Text(
            text = label,
            fontFamily = CairoFontFamily,
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}

@Composable
private fun NavCard(
    title: String,
    subtitle: String,
    icon: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = modifier.clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(icon, fontSize = 32.sp)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    fontFamily = CairoFontFamily,
                    fontSize = 12.sp,
                    color = TextSecondary
                )
            }
        }
    }
}

@Composable
private fun TimelineSlotRow(slotWithStatus: SlotWithStatus) {
    val slot = slotWithStatus.slot
    val status = slotWithStatus.status

    val (statusColor, statusText) = when (status) {
        SlotStatus.COMPLETED -> GreenPrimary to "✅ تم الإعطاء"
        SlotStatus.DUE -> AmberPrimary to "🔔 حان وقته الآن"
        SlotStatus.MISSED -> RedPrimary to "⛔ متأخر"
        SlotStatus.UPCOMING -> Color.Gray to "⚪ قادم"
        SlotStatus.SKIPPED -> Color.Gray to "تم التخطي"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ArabicFormatters.formatMinutesOfDay(slot.timeMinutes),
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    color = TextPrimary
                )
                Text(
                    text = statusText,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            slot.items.forEach { item ->
                Text(
                    text = "• ${item.medicationName} (${ArabicFormatters.formatPillQuantity(item.quantityHalves)})",
                    fontFamily = CairoFontFamily,
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }
        }
    }
}
