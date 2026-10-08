package com.family.dawa.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.core.time.TimeProvider
import com.family.dawa.data.repo.DoseRepository
import com.family.dawa.domain.model.DoseEvent
import com.family.dawa.domain.model.EventStatus
import com.family.dawa.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHistoryScreen(
    doseRepository: DoseRepository,
    timeProvider: TimeProvider,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val today = remember { timeProvider.today() }
    val startDate = remember { today.minusDays(14) }

    val historyEvents by doseRepository.getEventsBetween(startDate, today)
        .collectAsState(initial = emptyList())

    val groupedEvents = remember(historyEvents) {
        historyEvents.groupBy { it.slotDate }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "سجل الأدوية السابقة",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "رجوع")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BgCalm)
            )
        },
        containerColor = BgCalm
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (groupedEvents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد سجلات سابقة مسجلة بعد",
                            fontFamily = CairoFontFamily,
                            color = TextSecondary,
                            fontSize = 18.sp
                        )
                    }
                }
            } else {
                groupedEvents.forEach { (date, events) ->
                    item {
                        val dateLabel = if (date == today) "اليوم (${date})" else "تاريخ ${date}"
                        Text(
                            text = dateLabel,
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = TextPrimary,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }

                    items(events) { event ->
                        HistoryEventCard(
                            event = event,
                            onMarkGivenLate = {
                                scope.launch {
                                    doseRepository.doseLedger.updateEventStatus(
                                        eventId = event.id,
                                        newStatus = EventStatus.COMPLETED,
                                        now = timeProvider.nowZoned()
                                    )
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryEventCard(
    event: DoseEvent,
    onMarkGivenLate: () -> Unit
) {
    val (statusColor, statusLabel) = when (event.status) {
        EventStatus.COMPLETED -> GreenPrimary to "✅ تم الإعطاء"
        EventStatus.MISSED -> RedPrimary to "⛔ فات موعده"
        EventStatus.SKIPPED -> Color.Gray to "تم التخطي"
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = ArabicFormatters.formatMinutesOfDay(event.slotTimeMinutes),
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = statusLabel,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = statusColor
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "${event.medNameSnapshot} (${ArabicFormatters.formatPillQuantity(event.quantityHalvesSnapshot)})",
                fontFamily = CairoFontFamily,
                fontSize = 16.sp,
                color = TextSecondary
            )

            if (event.status == EventStatus.MISSED) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onMarkGivenLate,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenSurface, contentColor = GreenPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "تصحيح: أخذته تيتا متأخراً ✔",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
