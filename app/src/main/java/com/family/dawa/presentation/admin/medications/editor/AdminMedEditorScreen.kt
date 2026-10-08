package com.family.dawa.presentation.admin.medications.editor

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.family.dawa.core.image.ImageStore
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.MealRelation
import com.family.dawa.domain.model.Schedule
import com.family.dawa.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel
import org.koin.compose.koinInject
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMedEditorScreen(
    medicationId: Long,
    viewModel: AdminMedEditorViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val imageStore: ImageStore = koinInject()

    LaunchedEffect(medicationId) {
        viewModel.sendIntent(AdminMedEditorIntent.Load(medicationId))
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AdminMedEditorEffect.NavigateBack -> onBack()
            }
        }
    }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val savedPath = imageStore.saveFromUri(uri)
            if (savedPath != null) {
                viewModel.sendIntent(AdminMedEditorIntent.PhotoSelected(savedPath))
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (medicationId == 0L) "إضافة دواء جديد" else "تعديل الدواء",
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
            // Basic Info
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
                            text = "بيانات الدواء الأساسية",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary
                        )

                        OutlinedTextField(
                            value = state.name,
                            onValueChange = { viewModel.sendIntent(AdminMedEditorIntent.NameChanged(it)) },
                            label = { Text("اسم الدواء (مثال: بانادول للصداع)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = state.strength,
                            onValueChange = { viewModel.sendIntent(AdminMedEditorIntent.StrengthChanged(it)) },
                            label = { Text("التركيز (اختياري، مثال: ٥٠٠ مجم)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )

                        OutlinedTextField(
                            value = state.notes,
                            onValueChange = { viewModel.sendIntent(AdminMedEditorIntent.NotesChanged(it)) },
                            label = { Text("ملاحظات (مثال: مع كوب ماء كبير)", fontFamily = CairoFontFamily) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(14.dp)
                        )
                    }
                }
            }

            // Photo Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "صورة الدواء (الأهم لتيتا)",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        if (!state.photoPath.isNullOrEmpty() && File(state.photoPath!!).exists()) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFF0EFEB)),
                                contentAlignment = Alignment.Center
                            ) {
                                AsyncImage(
                                    model = File(state.photoPath!!),
                                    contentDescription = null,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                                Button(
                                    onClick = { photoPickerLauncher.launch("image/*") },
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary)
                                ) {
                                    Text("تغيير الصورة 📷", fontFamily = CairoFontFamily)
                                }
                                OutlinedButton(
                                    onClick = { viewModel.sendIntent(AdminMedEditorIntent.PhotoRemoved) }
                                ) {
                                    Text("حذف الصورة", fontFamily = CairoFontFamily, color = RedPrimary)
                                }
                            }
                        } else {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(140.dp)
                                    .clip(RoundedCornerShape(20.dp))
                                    .background(Color(0xFFF5F3EE))
                                    .border(2.dp, Color.LightGray, RoundedCornerShape(20.dp))
                                    .clickable { photoPickerLauncher.launch("image/*") },
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("📷", fontSize = 40.sp)
                                    Spacer(modifier = Modifier.height(6.dp))
                                    Text(
                                        text = "اضغط هنا لاختيار صورة علبة الدواء أو الحبة",
                                        fontFamily = CairoFontFamily,
                                        fontSize = 16.sp,
                                        color = TextSecondary
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Audio Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "تسجيل صوتي بصوت العائلة (اختياري)",
                            fontFamily = CairoFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = TextPrimary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Text(
                            text = "سجل جملة مألوفة لتيتا مثل: (يا تيتا ده دوا الضغط بتاع الصبح)",
                            fontFamily = CairoFontFamily,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (!state.isRecording) {
                                Button(
                                    onClick = { viewModel.sendIntent(AdminMedEditorIntent.StartRecordingAudio) },
                                    colors = ButtonDefaults.buttonColors(containerColor = RedPrimary)
                                ) {
                                    Text("بدء التسجيل 🎙️", fontFamily = CairoFontFamily)
                                }
                            } else {
                                Button(
                                    onClick = { viewModel.sendIntent(AdminMedEditorIntent.StopRecordingAudio) },
                                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                                ) {
                                    Text("إيقاف وحفظ ⏹️", fontFamily = CairoFontFamily)
                                }
                            }

                            if (!state.audioPath.isNullOrEmpty() && !state.isRecording) {
                                Button(
                                    onClick = { viewModel.sendIntent(AdminMedEditorIntent.PlayRecordedAudio) },
                                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary)
                                ) {
                                    Text("سماع التسجيل 🔊", fontFamily = CairoFontFamily)
                                }
                            }
                        }
                    }
                }
            }

            // Schedules Section
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "مواعيد وجرعات الدواء",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                    TextButton(
                        onClick = { viewModel.sendIntent(AdminMedEditorIntent.AddSchedule) }
                    ) {
                        Text("➕ إضافة موعد آخر", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                    }
                }
            }

            itemsIndexed(state.schedules) { index, schedule ->
                ScheduleRowEditor(
                    schedule = schedule,
                    onUpdate = { updated -> viewModel.sendIntent(AdminMedEditorIntent.UpdateSchedule(index, updated)) },
                    onDelete = if (state.schedules.size > 1) {
                        { viewModel.sendIntent(AdminMedEditorIntent.RemoveSchedule(index)) }
                    } else null
                )
            }

            // Save Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { viewModel.sendIntent(AdminMedEditorIntent.SaveMedication) },
                    enabled = state.name.isNotBlank() && !state.isSaving,
                    colors = ButtonDefaults.buttonColors(containerColor = GreenPrimary),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(60.dp)
                ) {
                    Text(
                        text = if (state.isSaving) "جاري الحفظ..." else "حفظ الدواء والمواعيد ✔",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun ScheduleRowEditor(
    schedule: Schedule,
    onUpdate: (Schedule) -> Unit,
    onDelete: (() -> Unit)?
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "الموعد: ${ArabicFormatters.formatMinutesOfDay(schedule.timeOfDayMinutes)}",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                if (onDelete != null) {
                    IconButton(onClick = onDelete) {
                        Icon(Icons.Default.Delete, contentDescription = "حذف", tint = RedPrimary)
                    }
                }
            }

            Text(text = "اختر الوقت:", fontFamily = CairoFontFamily, fontSize = 14.sp, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val presets = listOf(
                    480 to "٨ ص",
                    840 to "٢ ظ",
                    1200 to "٨ م",
                    1350 to "١٠:٣٠ م"
                )
                for ((minutes, label) in presets) {
                    val isSelected = schedule.timeOfDayMinutes == minutes
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdate(schedule.copy(timeOfDayMinutes = minutes)) },
                        label = { Text(label, fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold) }
                    )
                }
            }

            Text(text = "الجرعة:", fontFamily = CairoFontFamily, fontSize = 14.sp, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val qtyOptions = listOf(
                    1 to "نص قرص",
                    2 to "قرص واحد",
                    4 to "قرصين",
                    6 to "٣ أقراص"
                )
                for ((halves, label) in qtyOptions) {
                    val isSelected = schedule.quantityHalves == halves
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdate(schedule.copy(quantityHalves = halves)) },
                        label = { Text(label, fontFamily = CairoFontFamily) }
                    )
                }
            }

            Text(text = "العلاقة بالوجبات:", fontFamily = CairoFontFamily, fontSize = 14.sp, color = TextSecondary)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                val mealOptions = listOf(
                    MealRelation.NONE to "بدون",
                    MealRelation.BEFORE_BREAKFAST to "قبل الفطار",
                    MealRelation.AFTER_BREAKFAST to "بعد الفطار",
                    MealRelation.AFTER_LUNCH to "بعد الغدا",
                    MealRelation.AFTER_DINNER to "بعد العشا"
                )
                for ((rel, label) in mealOptions) {
                    val isSelected = schedule.mealRelation == rel
                    FilterChip(
                        selected = isSelected,
                        onClick = { onUpdate(schedule.copy(mealRelation = rel)) },
                        label = { Text(label, fontFamily = CairoFontFamily) }
                    )
                }
            }
        }
    }
}
