package com.family.dawa.presentation.admin.medications

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.domain.model.Medication
import com.family.dawa.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminMedListScreen(
    viewModel: AdminMedListViewModel = koinViewModel(),
    onAddMedication: () -> Unit,
    onEditMedication: (Long) -> Unit,
    onBack: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "قائمة الأدوية",
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = onAddMedication,
                containerColor = GreenPrimary,
                contentColor = Color.White,
                shape = RoundedCornerShape(20.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "إضافة دواء جديد",
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }
        },
        containerColor = BgCalm
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (state.medications.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "لا توجد أدوية مضافة بعد.\nاضغط على الزر أدناه لإضافة أول دواء.",
                            fontFamily = CairoFontFamily,
                            fontSize = 18.sp,
                            color = TextSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                items(state.medications) { med ->
                    MedicationListItem(
                        medication = med,
                        onEdit = { onEditMedication(med.id) },
                        onToggleArchive = { viewModel.sendIntent(AdminMedListIntent.ToggleArchive(med)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun MedicationListItem(
    medication: Medication,
    onEdit: () -> Unit,
    onToggleArchive: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onEdit)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(if (medication.active) GreenSurface else Color.LightGray.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(if (medication.active) "💊" else "📦", fontSize = 28.sp)
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = medication.name,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = if (medication.active) TextPrimary else Color.Gray
                    )
                    if (medication.strength.isNotEmpty()) {
                        Text(
                            text = medication.strength,
                            fontFamily = CairoFontFamily,
                            fontSize = 14.sp,
                            color = TextSecondary
                        )
                    }
                }
            }

            TextButton(onClick = onToggleArchive) {
                Text(
                    text = if (medication.active) "أرشفة" else "تفعيل",
                    fontFamily = CairoFontFamily,
                    color = if (medication.active) RedPrimary else GreenPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
