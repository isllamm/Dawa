package com.family.dawa.presentation.admin.health

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.core.permissions.HealthCheckItem
import com.family.dawa.ui.theme.*
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminHealthCheckScreen(
    viewModel: AdminHealthViewModel = koinViewModel(),
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "فحص الصلاحيات والشاومي",
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
            item {
                Text(
                    text = "لضمان رن المنبه وظهور الشاشة في الموعد بدقة على هاتف شاومي، تأكد من تفعيل جميع الصلاحيات التالية:",
                    fontFamily = CairoFontFamily,
                    fontSize = 16.sp,
                    color = TextSecondary
                )
            }

            items(state.items) { item ->
                HealthItemCard(
                    item = item,
                    onFix = {
                        item.fixIntent?.let { intent ->
                            try {
                                context.startActivity(intent)
                            } catch (_: Exception) {}
                        }
                    }
                )
            }

            item {
                Button(
                    onClick = { viewModel.sendIntent(AdminHealthIntent.Refresh(context)) },
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Text("إعادة فحص الصلاحيات الآن 🔄", fontFamily = CairoFontFamily, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun HealthItemCard(
    item: HealthCheckItem,
    onFix: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (item.isOk) Color.White else RedSurface
        ),
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = item.title,
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = TextPrimary
                )
                Text(
                    text = if (item.isOk) "مسموح به ✅" else "مطلوب ❌",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (item.isOk) GreenPrimary else RedPrimary
                )
            }

            Text(
                text = item.description,
                fontFamily = CairoFontFamily,
                fontSize = 14.sp,
                color = TextSecondary
            )

            if (!item.isOk && item.fixIntent != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Button(
                    onClick = onFix,
                    colors = ButtonDefaults.buttonColors(containerColor = AmberPrimary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = item.fixActionLabel,
                        fontFamily = CairoFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
