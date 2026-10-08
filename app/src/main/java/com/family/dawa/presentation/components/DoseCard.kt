package com.family.dawa.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.DoseItem
import com.family.dawa.domain.model.MealRelation
import com.family.dawa.ui.theme.CairoFontFamily
import java.io.File

/** How much room a card has, from roomy to tight. Chosen by [DoseCardGrid] from the number of medicines. */
enum class DoseCardStyle {
    /** One medicine: big photo on top, all details below. */
    Regular,
    /** Two medicines, one per row: photo beside the details so the photo stays large. */
    Wide,
    /** Three or four medicines in a 2×2 grid: smaller text. */
    Compact,
    /** Five or more: photo, pill pictogram and meal icon only (the voice still reads each name). */
    Minimal
}

/**
 * One medicine card. In the vertical styles the photo takes whatever height is left after the
 * details, so the caller must give the card a bounded height (or pass [photoHeight]).
 */
@Composable
fun DoseCard(
    item: DoseItem,
    modifier: Modifier = Modifier,
    style: DoseCardStyle = DoseCardStyle.Regular,
    photoHeight: Int? = null
) {
    val borderColor = if (item.colorTag != 0) Color(item.colorTag) else Color(0x22000000)
    val tight = style == DoseCardStyle.Compact || style == DoseCardStyle.Minimal
    val cardModifier = modifier
        .clip(RoundedCornerShape(if (tight) 24.dp else 32.dp))
        .background(Color.White)
        .border(3.dp, borderColor, RoundedCornerShape(if (tight) 24.dp else 32.dp))
        .padding(if (tight) 8.dp else 14.dp)

    if (style == DoseCardStyle.Wide) {
        Row(
            modifier = cardModifier,
            verticalAlignment = Alignment.CenterVertically
        ) {
            MedicationPhoto(
                item = item,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DoseDetails(item = item, style = style)
            }
        }
        return
    }

    Column(
        modifier = cardModifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Large medication photo: fixed height if given, otherwise all the space left over
        MedicationPhoto(
            item = item,
            placeholderSize = if (tight) 40 else 72,
            modifier = Modifier
                .fillMaxWidth()
                .then(if (photoHeight != null) Modifier.height(photoHeight.dp) else Modifier.weight(1f))
        )

        Spacer(modifier = Modifier.height(if (tight) 6.dp else 12.dp))

        DoseDetails(item = item, style = style)
    }
}

@Composable
private fun MedicationPhoto(
    item: DoseItem,
    modifier: Modifier = Modifier,
    placeholderSize: Int = 72
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFFF3EFEA)),
        contentAlignment = Alignment.Center
    ) {
        if (!item.primaryPhotoPath.isNullOrEmpty() && File(item.primaryPhotoPath).exists()) {
            AsyncImage(
                model = File(item.primaryPhotoPath),
                contentDescription = item.medicationName,
                contentScale = ContentScale.Fit,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Text("💊", fontSize = placeholderSize.sp)
        }
    }
}

/** Pill pictogram, written quantity, name and meal badge, sized for the card style. */
@Composable
private fun DoseDetails(item: DoseItem, style: DoseCardStyle) {
    val tight = style == DoseCardStyle.Compact || style == DoseCardStyle.Minimal

    // 2. Pill Pictogram (💊 💊)
    PillPictogram(quantityHalves = item.quantityHalves, scale = if (tight) 0.7f else 1f)

    if (style != DoseCardStyle.Minimal) {
        Spacer(modifier = Modifier.height(if (tight) 2.dp else 6.dp))

        // 3. Written Quantity ("قرص واحد", "قرصين")
        DetailText(
            text = ArabicFormatters.formatPillQuantity(item.quantityHalves),
            fontWeight = FontWeight.Bold,
            fontSize = if (tight) 18 else 24,
            color = Color(0xFF1E1E1E)
        )

        // 4. Medication Name
        DetailText(
            text = item.medicationName,
            fontWeight = FontWeight.SemiBold,
            fontSize = if (tight) 15 else 20,
            color = Color(0xFF555555)
        )
    }

    // 5. Meal badge if applicable (icon only when space is tight)
    if (item.mealRelation != MealRelation.NONE) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFEFEFEF))
                .padding(horizontal = if (tight) 6.dp else 10.dp, vertical = if (tight) 2.dp else 4.dp)
        ) {
            Text(item.mealRelation.icon, fontSize = if (tight) 13.sp else 16.sp)
            if (style != DoseCardStyle.Minimal) {
                Spacer(modifier = Modifier.width(6.dp))
                DetailText(
                    text = item.mealRelation.arabicLabel,
                    fontWeight = FontWeight.Normal,
                    fontSize = if (tight) 12 else 15,
                    color = Color(0xFF444444)
                )
            }
        }
    }
}

@Composable
private fun DetailText(text: String, fontWeight: FontWeight, fontSize: Int, color: Color) {
    Text(
        text = text,
        fontFamily = CairoFontFamily,
        fontWeight = fontWeight,
        fontSize = fontSize.sp,
        color = color,
        textAlign = TextAlign.Center,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
    )
}
