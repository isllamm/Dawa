package com.family.dawa.presentation.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.family.dawa.ui.theme.CairoFontFamily

@Composable
fun BigActionButton(
    text: String,
    icon: String,
    containerColor: Color,
    contentColor: Color = Color.White,
    enabled: Boolean = true,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        shape = RoundedCornerShape(32.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = containerColor.copy(alpha = 0.45f),
            disabledContentColor = contentColor.copy(alpha = 0.6f)
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp, pressedElevation = 2.dp),
        contentPadding = PaddingValues(vertical = 20.dp, horizontal = 24.dp),
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 100.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = icon,
                fontSize = 42.sp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = text,
                fontFamily = CairoFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
        }
    }
}
