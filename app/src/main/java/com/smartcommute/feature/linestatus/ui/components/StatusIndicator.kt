package com.smartcommute.feature.linestatus.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.smartcommute.R
import com.smartcommute.core.ui.theme.*
import com.smartcommute.feature.linestatus.domain.model.StatusType

@Composable
fun StatusIndicator(
    statusType: StatusType,
    modifier: Modifier = Modifier
) {
    val (iconColor, iconResId, contentDescResId) = when (statusType) {
        StatusType.GOOD_SERVICE -> Triple(status_good_service, R.drawable.ic_status_check_circle, R.string.status_good_service)
        StatusType.MINOR_DELAYS -> Triple(status_minor_delays, R.drawable.ic_status_warning_triangle, R.string.status_minor_delays)
        StatusType.MAJOR_DELAYS -> Triple(status_major_delays, R.drawable.ic_status_error_circle, R.string.status_major_delays)
        StatusType.SEVERE_DELAYS -> Triple(status_severe_delays, R.drawable.ic_status_error_circle, R.string.status_severe_delays)
        StatusType.CLOSURE -> Triple(status_closure, R.drawable.ic_status_error_circle, R.string.status_closure)
        StatusType.SERVICE_DISRUPTION -> Triple(status_service_disruption, R.drawable.ic_status_warning_triangle, R.string.status_service_disruption)
    }

    Icon(
        painter = painterResource(id = iconResId),
        contentDescription = stringResource(contentDescResId),
        tint = iconColor,
        modifier = modifier.size(28.dp)
    )
}
