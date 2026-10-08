package com.family.dawa.presentation.caregiver

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.HomeState
import com.family.dawa.domain.model.Slot
import androidx.lifecycle.compose.LifecycleStartEffect
import com.family.dawa.presentation.components.BigActionButton
import com.family.dawa.presentation.components.DoseCardGrid
import com.family.dawa.presentation.components.SpeakerButton
import com.family.dawa.presentation.components.TodayDotsStrip
import com.family.dawa.ui.theme.*
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel

@Composable
fun CaregiverScreen(
    viewModel: CaregiverViewModel = koinViewModel(),
    onOpenAdminPin: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.state.collectAsState()

    LifecycleStartEffect(viewModel) {
        viewModel.sendIntent(CaregiverIntent.ScreenVisibilityChanged(visible = true))
        onStopOrDispose {
            viewModel.sendIntent(CaregiverIntent.ScreenVisibilityChanged(visible = false))
        }
    }

    // The caregiver screens already use very large text. Ignore the phone's font-size setting
    // here so a large system font can't push the medicine cards or buttons off the screen.
    val density = LocalDensity.current
    CompositionLocalProvider(LocalDensity provides Density(density.density, fontScale = 1f)) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(BgCalm)
        ) {
            if (!uiState.isLoaded) {
                // Plain background until the first real state arrives, rather than a misleading "no medicine now".
            } else if (uiState.doneOverlaySlot != null) {
                CaregiverDoneContent()
            } else {
                when (val home = uiState.homeState) {
                    is HomeState.Due -> {
                        CaregiverDueContent(
                            state = home,
                            onConfirmTaken = { viewModel.sendIntent(CaregiverIntent.ConfirmTaken(home.slot)) },
                            onReplayVoice = { viewModel.sendIntent(CaregiverIntent.ReplayVoice) }
                        )
                    }
                    is HomeState.Missed -> {
                        CaregiverMissedContent(
                            state = home,
                            onAcknowledge = { viewModel.sendIntent(CaregiverIntent.AcknowledgeMissed(home.slot)) },
                            onReplayVoice = { viewModel.sendIntent(CaregiverIntent.ReplayVoice) }
                        )
                    }
                    is HomeState.Idle -> {
                        CaregiverIdleContent(
                            state = home,
                            onReplayVoice = { viewModel.sendIntent(CaregiverIntent.ReplayVoice) },
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
}

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
            .systemBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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
                        onClick = {}
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

            if (state.nextSlot != null) {
                Spacer(modifier = Modifier.height(28.dp))
                NextDosePreviewCard(slot = state.nextSlot)
            }
        }

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

@Composable
fun CaregiverDueContent(
    state: HomeState.Due,
    onConfirmTaken: () -> Unit,
    onReplayVoice: () -> Unit
) {
    var isConfirmEnabled by remember { mutableStateOf(false) }

    LaunchedEffect(state.slot.key) {
        isConfirmEnabled = false
        delay(3000)
        isConfirmEnabled = true
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(AmberSurface)
            .systemBarsPadding()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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

        DoseCardGrid(
            items = state.items,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )

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

@Composable
fun CaregiverDoneContent() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(GreenSurface)
            .systemBarsPadding(),
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
            .systemBarsPadding()
            .padding(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
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

        DoseCardGrid(
            items = state.slot.items,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            SpeakerButton(onClick = onReplayVoice)
            Spacer(modifier = Modifier.height(12.dp))

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

            BigActionButton(
                text = "حاضر",
                icon = "👍",
                containerColor = Color(0xFF616161),
                onClick = onAcknowledge
            )
        }
    }
}
