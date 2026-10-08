package com.family.dawa.presentation.admin.debug

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DebugScreen(
    viewModel: DebugViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

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
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "رجوع")
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
                            text = ArabicFormatters.formatTime(state.simulatedNow.toLocalTime()),
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 32.sp,
                            color = TextPrimary
                        )
                        if (state.currentOffsetMs != 0L) {
                            Text(
                                text = "⚠️ إزاحة الوقت مفعلة (+${state.currentOffsetMs / 60000} دقيقة)",
                                fontFamily = CairoFontFamily,
                                color = RedPrimary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (state.statusMessage.isNotEmpty()) {
                item {
                    Text(
                        text = state.statusMessage,
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
                            onClick = { viewModel.sendIntent(DebugIntent.TriggerTenSecondAlarm) },
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
                                onClick = { viewModel.sendIntent(DebugIntent.FastForward(15)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+١٥ دقيقة", fontFamily = CairoFontFamily)
                            }

                            OutlinedButton(
                                onClick = { viewModel.sendIntent(DebugIntent.FastForward(60)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+١ ساعة", fontFamily = CairoFontFamily)
                            }

                            OutlinedButton(
                                onClick = { viewModel.sendIntent(DebugIntent.FastForward(24 * 60)) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+١ يوم", fontFamily = CairoFontFamily)
                            }
                        }

                        Button(
                            onClick = { viewModel.sendIntent(DebugIntent.ResetOffset) },
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
                    onClick = { viewModel.sendIntent(DebugIntent.ResetToday) },
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
