package com.family.dawa.ui.admin

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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.R
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.data.demo.DemoSeeder
import com.family.dawa.data.settings.AppSettings
import com.family.dawa.data.settings.SettingsRepository
import com.family.dawa.ui.theme.*
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    settingsRepository: SettingsRepository,
    demoSeeder: DemoSeeder,
    onBack: () -> Unit
) {
    val scope = rememberCoroutineScope()
    val settings by settingsRepository.settingsFlow.collectAsState(initial = AppSettings())

    var patientName by remember(settings) { mutableStateOf(settings.patientName) }
    var caregiverName by remember(settings) { mutableStateOf(settings.caregiverName) }
    var graceMinutes by remember(settings) { mutableStateOf(settings.graceMinutes.toString()) }
    var realertMinutes by remember(settings) { mutableStateOf(settings.realertMinutes.toString()) }
    var showDisclaimerDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "إعدادات التطبيق",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Names Card
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
                            text = "الأسماء المألوفة",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = patientName,
                            onValueChange = { patientName = it },
                            label = { Text("اسم المريض (مثال: جدو)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = caregiverName,
                            onValueChange = { caregiverName = it },
                            label = { Text("اسم مقدم الرعاية (مثال: تيتا)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
            }

            // Timers Card
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
                            text = "فترات التنبيه والانتظار",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = graceMinutes,
                            onValueChange = { graceMinutes = it },
                            label = { Text("مهلة التأخير بالدقائق قبل اعتبار الدواء فائتاً (افتراضي ٦٠)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = realertMinutes,
                            onValueChange = { realertMinutes = it },
                            label = { Text("إعادة رنين التنبيه كل (بالدقائق) إذا لم يُؤخذ (افتراضي ٥)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
            }

            // Save Settings Button
            item {
                Button(
                    onClick = {
                        scope.launch {
                            settingsRepository.updateSettings(
                                patientName = patientName.trim(),
                                caregiverName = caregiverName.trim(),
                                graceMinutes = graceMinutes.toIntOrNull() ?: 60,
                                realertMinutes = realertMinutes.toIntOrNull() ?: 5,
                                voiceEnabled = settings.voiceEnabled,
                                vibrationEnabled = settings.vibrationEnabled,
                                breakfastMinutes = settings.breakfastMinutes,
                                lunchMinutes = settings.lunchMinutes,
                                dinnerMinutes = settings.dinnerMinutes,
                                sleepMinutes = settings.sleepMinutes
                            )
                            onBack()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                ) {
                    Text(
                        text = "حفظ الإعدادات ✔",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }
            }

            // Medical Disclaimer Button
            item {
                OutlinedButton(
                    onClick = { showDisclaimerDialog = true },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "قراءة إخلاء المسؤولية الطبي ⚠️",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }

            // Reseed Demo Data Button
            item {
                OutlinedButton(
                    onClick = {
                        scope.launch {
                            demoSeeder.forceSeed()
                            onBack()
                        }
                    },
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "إعادة تحميل البيانات التجريبية الافتراضية 🔄",
                        fontFamily = CairoFontFamily,
                        color = Color.DarkGray,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }

    if (showDisclaimerDialog) {
        AlertDialog(
            onDismissRequest = { showDisclaimerDialog = false },
            title = {
                Text(
                    text = stringResource(R.string.medical_disclaimer_title),
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = stringResource(R.string.medical_disclaimer_body),
                    fontFamily = CairoFontFamily,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = { showDisclaimerDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                ) {
                    Text("حسناً، أوافق", fontFamily = CairoFontFamily)
                }
            }
        )
    }
}
