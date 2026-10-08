package com.family.dawa.presentation.admin.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.BuildConfig
import com.family.dawa.R
import com.family.dawa.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSettingsScreen(
    viewModel: AdminSettingsViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showDisclaimerDialog by remember { mutableStateOf(false) }
    var showReseedDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                is AdminSettingsEffect.NavigateBack -> onBack()
            }
        }
    }

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
                            value = state.patientName,
                            onValueChange = { viewModel.sendIntent(AdminSettingsIntent.PatientNameChanged(it)) },
                            label = { Text("اسم المريض (مثال: جدو)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = state.caregiverName,
                            onValueChange = { viewModel.sendIntent(AdminSettingsIntent.CaregiverNameChanged(it)) },
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
                            value = state.graceMinutes,
                            onValueChange = { viewModel.sendIntent(AdminSettingsIntent.GraceMinutesChanged(it)) },
                            label = { Text("مهلة التأخير بالدقائق قبل اعتبار الدواء فائتاً (افتراضي ٦٠)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = state.realertMinutes,
                            onValueChange = { viewModel.sendIntent(AdminSettingsIntent.RealertMinutesChanged(it)) },
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
                    onClick = { viewModel.sendIntent(AdminSettingsIntent.SaveSettings) },
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

            // Reseed Demo Data Button (debug builds only: it deletes all medicines and history)
            if (BuildConfig.DEBUG) {
                item {
                    OutlinedButton(
                        onClick = { showReseedDialog = true },
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
    }

    if (showReseedDialog) {
        AlertDialog(
            onDismissRequest = { showReseedDialog = false },
            title = {
                Text(
                    text = "مسح كل البيانات؟",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = "هيتم مسح كل الأدوية والمواعيد والسجل ورقم الطوارئ، وتحميل الأدوية التجريبية مكانها. الرقم السري والإعدادات مش هيتغيروا.",
                    fontFamily = CairoFontFamily,
                    fontSize = 16.sp
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        showReseedDialog = false
                        viewModel.sendIntent(AdminSettingsIntent.ReseedDemoData)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                ) {
                    Text("امسح وحمّل التجريبي", fontFamily = CairoFontFamily)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReseedDialog = false }) {
                    Text("إلغاء", fontFamily = CairoFontFamily)
                }
            }
        )
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
