package com.family.dawa.ui.caregiver

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.*
import com.family.dawa.ui.components.*
import com.family.dawa.ui.theme.*
import kotlinx.coroutines.delay

@Composable
fun CaregiverScreen(
    viewModel: CaregiverViewModel,
    onOpenAdminPin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val homeState by viewModel.homeState.collectAsState()
    val doneSlot by viewModel.isDoneOverlay.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BgCalm)
    ) {
        if (doneSlot != null) {
            // Done state takes precedence
            CaregiverDoneContent()
        } else {
            when (val state = homeState) {
                is HomeState.Due -> {
                    CaregiverDueContent(
                        state = state,
                        onConfirmTaken = { viewModel.onConfirmTaken(state.slot) },
                        onReplayVoice = { viewModel.replayVoice() }
                    )
                }
                is HomeState.Missed -> {
                    CaregiverMissedContent(
                        state = state,
                        onAcknowledge = { viewModel.onAcknowledgeMissed(state.slot) },
                        onReplayVoice = { viewModel.replayVoice() }
                    )
                }
                is HomeState.Idle -> {
                    CaregiverIdleContent(
                        state = state,
                        onReplayVoice = { viewModel.replayVoice() },
                        onOpenAdminPin = onOpenAdminPin
                    )
                }
                is HomeState.Done -> {
                    CaregiverDoneContent()
                }
            }
        }
    }
}

/**
 * STATE A: IDLE (مفيش دوا دلوقتي - أخضر مريح)
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CaregiverIdleContent(
    state: HomeState.Idle,
    onReplayVoice: () -> Unit,
    onOpenAdminPin: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top bar with faint hidden Admin button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .combinedClickable(
                        onLongClick = onOpenAdminPin,
                        onClick = {} // regular tap does nothing
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = Color.LightGray.copy(alpha = 0.5f),
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Central Calm Status Card
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text("🟢", fontSize = 80.sp)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "مفيش دوا دلوقتي",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 42.sp,
                color = GreenPrimary,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "كل حاجة تمام، ارتاحي يا غالية",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            // Next Dose Preview Card
            if (state.nextSlot != null) {
                Spacer(modifier = Modifier.height(28.dp))
                NextDosePreviewCard(slot = state.nextSlot)
            }
        }

        // Bottom Strip: Speaker + Today dots
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            SpeakerButton(onClick = onReplayVoice)
            Spacer(modifier = Modifier.height(16.dp))
            TodayDotsStrip(todaySlots = state.todaySlots)
        }
    }
}

@Composable
fun NextDosePreviewCard(slot: Slot) {
    val timeInfo = ArabicFormatters.timeOfDayInfo(slot.timeMinutes)
    val formattedTime = ArabicFormatters.formatMinutesOfDay(slot.timeMinutes)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(28.dp))
            .background(Color.White)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(timeInfo.emoji, fontSize = 28.sp)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "الدوا الجاي: $formattedTime (${timeInfo.label})",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // First medication preview item
        val firstItem = slot.items.firstOrNull()
        if (firstItem != null) {
            Text(
                text = "${firstItem.medicationName} (${ArabicFormatters.formatPillQuantity(firstItem.quantityHalves)})",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp,
                color = TextSecondary
            )
        }
    }
}

/**
 * STATE B: DUE (دلوقتي معاد الدوا - أصفر/ذهبي دافئ وواضح)
 */
@Composable
fun CaregiverDueContent(
    state: HomeState.Due,
    onConfirmTaken: () -> Unit,
    onReplayVoice: () -> Unit
) {
    // 3-second accidental tap guard on arrival
    var isConfirmEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(state.slot.key) {
        isConfirmEnabled = false
        delay(3000) // disabled for the first 3 seconds
        isConfirmEnabled = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmberSurface)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text("🔔", fontSize = 44.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "دلوقتي معاد الدوا",
                    fontFamily = CairoFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 38.sp,
                    color = Color(0xFF945B00)
                )
            }
            Text(
                text = "خدي الأدوية اللي على الشاشة",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 22.sp,
                color = TextPrimary
            )
        }

        // Middle: Medication Cards Grid / List
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            when (state.items.size) {
                1 -> {
                    DoseCard(
                        item = state.items[0],
                        photoHeight = 240,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                2 -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        DoseCard(item = state.items[0], photoHeight = 150)
                        DoseCard(item = state.items[1], photoHeight = 150)
                    }
                }
                else -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.items) { item ->
                            DoseCard(item = item, photoHeight = 120)
                        }
                    }
                }
            }
        }

        // Bottom Actions: Speaker + Huge Confirm Button
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            SpeakerButton(onClick = onReplayVoice)

            Spacer(modifier = Modifier.height(14.dp))

            BigActionButton(
                text = if (isConfirmEnabled) "اديت الدوا" else "لحظة واحدة...",
                icon = "✔",
                containerColor = GreenPrimary,
                enabled = isConfirmEnabled,
                onClick = onConfirmTaken
            )
        }
    }
}

/**
 * STATE C: DONE (تمام - تأكيد كبير لمدة 4 ثوانٍ)
 */
@Composable
fun CaregiverDoneContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenSurface),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp)
        ) {
            Text("✅", fontSize = 110.sp)
            Spacer(modifier = Modifier.height(20.dp))
            Text(
                text = "تمام",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 58.sp,
                color = GreenPrimary
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "تسلم إيدك يا غالية.. خلصنا",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp,
                color = TextPrimary,
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * STATE D: MISSED (الدوا اتأخر معاده - أحمر هادئ بدون إرشادات طبية)
 */
@Composable
fun CaregiverMissedContent(
    state: HomeState.Missed,
    onAcknowledge: () -> Unit,
    onReplayVoice: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RedSurface)
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Header
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("⛔", fontSize = 60.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "الدوا ده اتأخر معاده",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 36.sp,
                color = RedPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))
            val timeStr = ArabicFormatters.formatMinutesOfDay(state.slot.timeMinutes)
            Text(
                text = "معاد الساعة $timeStr",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Medium,
                fontSize = 24.sp,
                color = TextSecondary
            )
        }

        // Preview of missed medications
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            state.slot.items.forEach { item ->
                DoseCard(item = item, photoHeight = 130)
            }
        }

        // Bottom Actions: Call + Acknowledge ("حاضر")
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            SpeakerButton(onClick = onReplayVoice)

            Spacer(modifier = Modifier.height(12.dp))

            // Call Family Member Button (if contact configured)
            if (state.contact != null && state.contact.phone.isNotEmpty()) {
                BigActionButton(
                    text = "كلّمي ${state.contact.name}",
                    icon = "📞",
                    containerColor = Color(0xFF1976D2),
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                            data = Uri.parse("tel:${state.contact.phone}")
                        }
                        context.startActivity(dialIntent)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Acknowledge Button: "حاضر"
            BigActionButton(
                text = "حاضر",
                icon = "👍",
                containerColor = Color(0xFF616161),
                onClick = onAcknowledge
            )
        }
    }
}
