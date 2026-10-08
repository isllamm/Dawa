package com.family.dawa.ui.debug

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.alarm.AlarmReceiver
import com.family.dawa.alarm.AlarmSync
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.core.time.DebugTimeProvider
import com.family.dawa.data.repo.DoseRepository
import com.family.dawa.data.settings.SettingsRepository
import com.family.dawa.domain.engine.DoseEngine
import com.family.dawa.ui.MainActivity
import com.family.dawa.ui.theme.*
import kotlinx.coroutines.launch
import java.time.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    timeProvider: DebugTimeProvider,
    settingsRepository: SettingsRepository,
    doseRepository: DoseRepository,
    alarmSync: AlarmSync,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var currentOffsetMs by remember { mutableStateOf(timeProvider.offsetMillis) }
    var statusMessage by remember { mutableStateOf("") }

    val simulatedNow = remember(currentOffsetMs) { timeProvider.nowZoned() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "أدوات التجربة والاختبار 🛠️",
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
            // Simulated Time Display
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = AmberSurface),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "الوقت الافتراضي داخل التطبيق:",
                            fontFamily = CairoFontFamily,
                            fontSize = 16.sp,
                            color = TextSecondary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = ArabicFormatters.formatTime(simulatedNow.toLocalTime()),
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = TextPrimary
                        )
                        if (currentOffsetMs != 0L) {
                            Text(
                                text = "⚠️ إزاحة الوقت مفعلة (+${currentOffsetMs / 60000} دقيقة)",
                                fontFamily = CairoFontFamily,
                                color = RedPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (statusMessage.isNotEmpty()) {
                item {
                    Text(
                        text = statusMessage,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        color = GreenPrimary,
                        fontSize = 16.sp
                    )
                }
            }

            // Real 10-Second Alarm Test
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "اختبار المنبه وشاشة القفل",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )
                        Text(
                            text = "يقوم بجدولة منبه حقيقي بعد ١٠ ثوانٍ عبر AlarmManager. يمكنك إغلاق وقفل الشاشة للتأكد من استيقاظ الهاتف وظهور شاشة التنبيه.",
                            fontFamily = CairoFontFamily,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )

                        Button(
                            onClick = {
                                val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
                                val triggerAt = System.currentTimeMillis() + 10_000L

                                val intent = Intent(context, AlarmReceiver::class.java).apply {
                                    action = AlarmSync.ACTION_DUE_ALARM
                                }
                                val pi = PendingIntent.getBroadcast(
                                    context,
                                    9999,
                                    intent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                val showIntent = Intent(context, MainActivity::class.java)
                                val showPi = PendingIntent.getActivity(
                                    context,
                                    0,
                                    showIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                                )

                                alarmManager.setAlarmClock(
                                    AlarmManager.AlarmClockInfo(triggerAt, showPi),
                                    pi
                                )
                                statusMessage = "تم ضبط المنبه بعد ١٠ ثوانٍ! اقفل الشاشة الآن للتجربة 🔔"
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth().height(50.dp)
                        ) {
                            Text("رن منبه حقيقي بعد ١٠ ثوانٍ 🔔", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Fast Forward Time Buttons
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "تسريع الوقت والمحاكاة",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val newOffset = currentOffsetMs + (15 * 60 * 1000L)
                                        currentOffsetMs = newOffset
                                        timeProvider.offsetMillis = newOffset
                                        settingsRepository.setDebugTimeOffset(newOffset)
                                        alarmSync.resync()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+١٥ دقيقة", fontFamily = CairoFontFamily)
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val newOffset = currentOffsetMs + (60 * 60 * 1000L)
                                        currentOffsetMs = newOffset
                                        timeProvider.offsetMillis = newOffset
                                        settingsRepository.setDebugTimeOffset(newOffset)
                                        alarmSync.resync()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+١ ساعة", fontFamily = CairoFontFamily)
                            }

                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        val newOffset = currentOffsetMs + (24 * 60 * 60 * 1000L)
                                        currentOffsetMs = newOffset
                                        timeProvider.offsetMillis = newOffset
                                        settingsRepository.setDebugTimeOffset(newOffset)
                                        alarmSync.resync()
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+١ يوم", fontFamily = CairoFontFamily)
                            }
                        }

                        Button(
                            onClick = {
                                scope.launch {
                                    currentOffsetMs = 0L
                                    timeProvider.offsetMillis = 0L
                                    settingsRepository.setDebugTimeOffset(0L)
                                    alarmSync.resync()
                                    statusMessage = "تمت استعادة الوقت الفعلي للهاتف ✔"
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color.DarkGray),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("إعادة الوقت للوقت الفعلي 🔄", fontFamily = CairoFontFamily)
                        }
                    }
                }
            }

            // Reset Today Events
            item {
                Button(
                    onClick = {
                        scope.launch {
                            doseRepository.resetTodayEvents()
                            alarmSync.resync()
                            statusMessage = "تم مسح سجلات اليوم والبدء من جديد ✔"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("مسح سجل اليوم للبدء من جديد 🧹", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
