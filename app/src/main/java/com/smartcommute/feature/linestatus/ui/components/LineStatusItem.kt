package com.smartcommute.feature.linestatus.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.unit.dp
import com.smartcommute.R
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine

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
        com.smartcommute.feature.linestatus.domain.model.StatusType.GOOD_SERVICE -> R.string.status_good_service
        com.smartcommute.feature.linestatus.domain.model.StatusType.MINOR_DELAYS -> R.string.status_minor_delays
        com.smartcommute.feature.linestatus.domain.model.StatusType.MAJOR_DELAYS -> R.string.status_major_delays
        com.smartcommute.feature.linestatus.domain.model.StatusType.SEVERE_DELAYS -> R.string.status_severe_delays
        com.smartcommute.feature.linestatus.domain.model.StatusType.CLOSURE -> R.string.status_closure
        com.smartcommute.feature.linestatus.domain.model.StatusType.SERVICE_DISRUPTION -> R.string.status_service_disruption
    }

    val statusText = stringResource(id = statusTextResId)
    val contentDesc = stringResource(id = R.string.cd_line_status, line.name, statusText)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = contentDesc },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // TfL roundel icon with line color - shared element
            Box(
                modifier = Modifier
                    .size(52.dp)
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
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Line name and status - shared elements
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = line.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
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
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
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

            Spacer(modifier = Modifier.width(16.dp))

            // Status indicator
            StatusIndicator(statusType = line.status.type)
        }
    }
}
