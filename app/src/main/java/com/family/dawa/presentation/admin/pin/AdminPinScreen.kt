package com.family.dawa.presentation.admin.pin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.ui.theme.*
import kotlinx.coroutines.flow.collectLatest
import org.koin.androidx.compose.koinViewModel

@Composable
fun AdminPinScreen(
    viewModel: AdminPinViewModel = koinViewModel(),
    onPinSuccess: () -> Unit,
    onCancel: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                AdminPinEffect.NavigateToDashboard -> onPinSuccess()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BgCalm)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(modifier = Modifier.height(20.dp))
            Text("🔒", fontSize = 56.sp)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "دخول شاشة إدارة الأدوية",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 28.sp,
                color = TextPrimary
            )
            Text(
                text = "أدخل رقم المرور السري (الافتراضي ١٢٣٤)",
                fontFamily = CairoFontFamily,
                fontSize = 18.sp,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // PIN Dots Indicator
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 4) {
                    val isFilled = i < state.enteredPin.length
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(
                                if (state.isError) RedPrimary
                                else if (isFilled) GreenPrimary
                                else Color.LightGray.copy(alpha = 0.5f)
                            )
                    )
                }
            }

            if (state.isError) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "رقم المرور غير صحيح",
                    fontFamily = CairoFontFamily,
                    color = RedPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Numeric Keypad
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val rows = listOf(
                listOf("1", "2", "3"),
                listOf("4", "5", "6"),
                listOf("7", "8", "9"),
                listOf("C", "0", "←")
            )

            for (row in rows) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    for (key in row) {
                        PinKeyButton(
                            label = if (key in "0".."9") ArabicFormatters.toArabicDigits(key) else key,
                            onClick = {
                                when (key) {
                                    "C" -> viewModel.sendIntent(AdminPinIntent.Clear)
                                    "←" -> viewModel.sendIntent(AdminPinIntent.Backspace)
                                    else -> viewModel.sendIntent(AdminPinIntent.DigitEntered(key))
                                }
                            }
                        )
                    }
                }
            }
        }

        Button(
            onClick = onCancel,
            colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent, contentColor = TextSecondary),
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Text(
                text = "الرجوع لشاشة تيتا",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp
            )
        }
    }
}

@Composable
private fun PinKeyButton(label: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(76.dp)
            .clip(CircleShape)
            .background(Color.White)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = TextPrimary
        )
    }
}
