package com.smartcommute.feature.linestatus.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smartcommute.R
import com.smartcommute.core.ui.theme.*
import com.smartcommute.feature.linestatus.domain.model.StatusType
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine

private val TextPrimary = Color(0xFF0A0A0A)

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LineStatusItem(
    line: UndergroundLine,
    lineColor: Color,
    animatedVisibilityScope: AnimatedVisibilityScope,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val statusTextResId = when (line.status.type) {
        StatusType.GOOD_SERVICE -> R.string.status_good_service
        StatusType.MINOR_DELAYS -> R.string.status_minor_delays
        StatusType.MAJOR_DELAYS -> R.string.status_major_delays
        StatusType.SEVERE_DELAYS -> R.string.status_severe_delays
        StatusType.CLOSURE -> R.string.status_closure
        StatusType.SERVICE_DISRUPTION -> R.string.status_service_disruption
    }

    val statusColor = when (line.status.type) {
        StatusType.GOOD_SERVICE -> status_good_service
        StatusType.MINOR_DELAYS -> status_minor_delays
        StatusType.MAJOR_DELAYS -> status_major_delays
        StatusType.SEVERE_DELAYS -> status_severe_delays
        StatusType.CLOSURE -> status_closure
        StatusType.SERVICE_DISRUPTION -> status_service_disruption
    }

    val statusText = stringResource(id = statusTextResId)
    val contentDesc = stringResource(id = R.string.cd_line_status, line.name, statusText)

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .semantics { contentDescription = contentDesc }
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TfL roundel icon with tinted circular background - shared element
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(lineColor.copy(alpha = 0.15f))
                    .sharedElement(
                        rememberSharedContentState(key = "line_icon_${line.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            )
                        }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_tfl_roundel),
                    contentDescription = null,
                    tint = lineColor,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Line name and status text
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = line.name,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextPrimary,
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "line_name_${line.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            )
                        }
                    )
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = statusColor,
                    modifier = Modifier.sharedElement(
                        rememberSharedContentState(key = "line_status_${line.id}"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            )
                        }
                    )
                )
            }

            // Trailing status indicator icon
            StatusIndicator(statusType = line.status.type)
        }

        // Bottom border
        HorizontalDivider(
            thickness = 0.686.dp,
            color = Color.Black.copy(alpha = 0.1f)
        )
    }
}
