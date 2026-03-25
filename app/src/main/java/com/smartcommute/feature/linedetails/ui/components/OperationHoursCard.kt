package com.smartcommute.feature.linedetails.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartcommute.R

private val LabelColor = Color(0xFF4A5565)
private val ValueColor = Color(0xFF0A0A0A)
private val BorderColor = Color(0x1A000000)

@Composable
fun OperationHoursCard(
    modifier: Modifier = Modifier
) {
    val title = stringResource(R.string.card_operation_hours)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(0.686.dp, BorderColor)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Schedule,
                    contentDescription = title,
                    modifier = Modifier.size(20.dp),
                    tint = ValueColor
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = ValueColor
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            HoursRow(
                label = stringResource(R.string.label_monday_friday),
                value = stringResource(R.string.hours_weekday)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HoursRow(
                label = stringResource(R.string.label_saturday),
                value = stringResource(R.string.hours_saturday)
            )
            Spacer(modifier = Modifier.height(12.dp))
            HoursRow(
                label = stringResource(R.string.label_sunday),
                value = stringResource(R.string.hours_sunday)
            )
        }
    }
}

@Composable
private fun HoursRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, fontSize = 16.sp, color = LabelColor)
        Text(text = value, fontSize = 16.sp, color = ValueColor)
    }
}
