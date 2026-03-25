package com.smartcommute.feature.linedetails.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartcommute.R
import com.smartcommute.core.ui.theme.*
import com.smartcommute.feature.linedetails.domain.model.UndergroundLineDetails
import com.smartcommute.feature.linestatus.domain.model.StatusType
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val TextPrimary = Color(0xFF0A0A0A)
private val TextSecondary = Color(0xFF4A5565)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LineDetailsHeader(
    lineDetails: UndergroundLineDetails,
    lineColor: Color,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    val statusColor = when (lineDetails.status.type) {
        StatusType.GOOD_SERVICE -> status_good_service
        StatusType.MINOR_DELAYS -> status_minor_delays
        StatusType.MAJOR_DELAYS -> status_major_delays
        StatusType.SEVERE_DELAYS -> status_severe_delays
        StatusType.CLOSURE -> status_closure
        StatusType.SERVICE_DISRUPTION -> status_service_disruption
    }

    val statusIconResId = when (lineDetails.status.type) {
        StatusType.GOOD_SERVICE -> R.drawable.ic_status_check_circle
        StatusType.MINOR_DELAYS -> R.drawable.ic_status_warning_triangle
        StatusType.SERVICE_DISRUPTION -> R.drawable.ic_status_warning_triangle
        else -> R.drawable.ic_status_error_circle
    }

    val statusTextResId = when (lineDetails.status.type) {
        StatusType.GOOD_SERVICE -> R.string.status_good_service
        StatusType.MINOR_DELAYS -> R.string.status_minor_delays
        StatusType.MAJOR_DELAYS -> R.string.status_major_delays
        StatusType.SEVERE_DELAYS -> R.string.status_severe_delays
        StatusType.CLOSURE -> R.string.status_closure
        StatusType.SERVICE_DISRUPTION -> R.string.status_service_disruption
    }

    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.getDefault())
    val formattedDate = dateFormat.format(Date(lineDetails.lastUpdated))

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(top = 24.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Large circular tinted icon
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(CircleShape)
                .background(lineColor.copy(alpha = 0.15f))
                .sharedElement(
                    rememberSharedContentState(key = "line_icon_${lineDetails.id}"),
                    animatedVisibilityScope = animatedVisibilityScope,
                    boundsTransform = { _, _ ->
                        tween(durationMillis = 500, easing = FastOutSlowInEasing)
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(id = R.drawable.ic_tfl_roundel),
                contentDescription = null,
                tint = lineColor,
                modifier = Modifier.size(52.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Line name
        Text(
            text = lineDetails.name,
            fontSize = 30.sp,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "line_name_${lineDetails.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 500, easing = FastOutSlowInEasing)
                }
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Status icon + text
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.sharedElement(
                rememberSharedContentState(key = "line_status_${lineDetails.id}"),
                animatedVisibilityScope = animatedVisibilityScope,
                boundsTransform = { _, _ ->
                    tween(durationMillis = 500, easing = FastOutSlowInEasing)
                }
            )
        ) {
            Icon(
                painter = painterResource(id = statusIconResId),
                contentDescription = null,
                tint = statusColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(statusTextResId),
                fontSize = 18.sp,
                color = statusColor
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Last updated timestamp
        Text(
            text = "Last updated: $formattedDate",
            fontSize = 14.sp,
            color = TextSecondary
        )
    }
}
