package com.smartcommute.feature.linestatus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smartcommute.core.ui.theme.*
import com.smartcommute.feature.linestatus.domain.model.StatusType

@Composable
fun StatusIndicator(
    statusType: StatusType,
    modifier: Modifier = Modifier
) {
    val (backgroundColor, iconTint) = when (statusType) {
        StatusType.GOOD_SERVICE -> status_good_service to Color.White
        StatusType.MINOR_DELAYS -> status_minor_delays to Color.White
        StatusType.MAJOR_DELAYS -> status_major_delays to Color.White
        StatusType.SEVERE_DELAYS -> status_severe_delays to Color.White
        StatusType.CLOSURE -> status_closure to Color.White
        StatusType.SERVICE_DISRUPTION -> status_service_disruption to Color.White
    }

    val icon = when (statusType) {
        StatusType.GOOD_SERVICE -> Icons.Default.CheckCircle
        else -> Icons.Default.Warning
    }

    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = iconTint,
            modifier = Modifier
                .size(24.dp)
                .padding(4.dp)
        )
    }
}
