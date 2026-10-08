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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.family.dawa.core.time.ArabicFormatters
import com.family.dawa.domain.model.DoseItem
import com.family.dawa.domain.model.MealRelation
import com.family.dawa.ui.theme.CairoFontFamily
import java.io.File

@Composable
fun DoseCard(
    item: DoseItem,
    modifier: Modifier = Modifier,
    photoHeight: Int = 180
) {
    val borderColor = if (item.colorTag != 0) Color(item.colorTag) else Color(0x22000000)

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(32.dp))
            .background(Color.White)
            .border(3.dp, borderColor, RoundedCornerShape(32.dp))
            .padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 1. Large Medication Photo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(photoHeight.dp)
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
                Text("💊", fontSize = 72.sp)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // 2. Pill Pictogram (💊 💊)
        PillPictogram(quantityHalves = item.quantityHalves)

        Spacer(modifier = Modifier.height(6.dp))

        // 3. Written Quantity ("قرص واحد", "قرصين")
        Text(
            text = ArabicFormatters.formatPillQuantity(item.quantityHalves),
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = Color(0xFF1E1E1E)
        )

        // 4. Medication Name
        Text(
            text = item.medicationName,
            fontFamily = CairoFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 20.sp,
            color = Color(0xFF555555)
        )

        // 5. Meal badge if applicable
        if (item.mealRelation != MealRelation.NONE) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFEFEF))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(item.mealRelation.icon, fontSize = 16.sp)
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = item.mealRelation.arabicLabel,
                    fontFamily = CairoFontFamily,
                    fontSize = 15.sp,
                    color = Color(0xFF444444)
                )
            }
        }
    }
}
