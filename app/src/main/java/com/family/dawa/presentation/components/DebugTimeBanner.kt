package com.family.dawa.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.BuildConfig
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.usecase.settings.GetSettingsUseCase
import com.family.dawa.ui.theme.CairoFontFamily
import kotlinx.coroutines.flow.map
import org.koin.compose.koinInject

/**
 * Wraps a whole screen. In debug builds, while a fast-forward time offset is active, it shows a
 * red warning strip on top: with an offset the app thinks it's later than it is, so real
 * reminders show as missed. Release builds never apply an offset, so this shows nothing there.
 */
@Composable
fun DebugTimeBannerHost(content: @Composable () -> Unit) {
    if (!BuildConfig.DEBUG) {
        content()
        return
    }

    val getSettings: GetSettingsUseCase = koinInject()
    val offsetMs by remember(getSettings) { getSettings().map { it.debugTimeOffsetMs } }
        .collectAsState(initial = 0L)

    Column(modifier = Modifier.fillMaxSize()) {
        if (offsetMs != 0L) {
            Text(
                text = "⚠️ وضع التجربة: الوقت متقدم ${ArabicFormatters.toArabicDigits(offsetMs / 60_000)} دقيقة — المنبهات مش هتظبط",
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFFC62828))
                    .statusBarsPadding()
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                // The strip already covers the status bar, so the screen below shouldn't pad for it again
                .then(if (offsetMs != 0L) Modifier.consumeWindowInsets(WindowInsets.statusBars) else Modifier)
        ) {
            content()
        }
    }
}
