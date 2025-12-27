package com.smartcommute.feature.linestatus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.smartcommute.feature.linestatus.domain.model.UndergroundLine

@Composable
fun LineStatusItem(
    line: UndergroundLine,
    lineColor: Color,
    modifier: Modifier = Modifier
) {
    val statusText = when (line.status.type) {
        com.smartcommute.feature.linestatus.domain.model.StatusType.GOOD_SERVICE -> "Good Service"
        com.smartcommute.feature.linestatus.domain.model.StatusType.MINOR_DELAYS -> "Minor Delays"
        com.smartcommute.feature.linestatus.domain.model.StatusType.MAJOR_DELAYS -> "Major Delays"
        com.smartcommute.feature.linestatus.domain.model.StatusType.SEVERE_DELAYS -> "Severe Delays"
        com.smartcommute.feature.linestatus.domain.model.StatusType.CLOSURE -> "Closure"
        com.smartcommute.feature.linestatus.domain.model.StatusType.SERVICE_DISRUPTION -> "Service Disruption"
    }

    val contentDesc = "${line.name} line: $statusText${
        if (line.status.description.isNotEmpty()) ". ${line.status.description}" else ""
    }"

    Card(
        modifier = modifier
            .fillMaxWidth()
            .semantics { contentDescription = contentDesc },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Line color indicator (logo placeholder)
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(lineColor)
            )

            Spacer(modifier = Modifier.width(16.dp))

            // Line name and status
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = line.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (line.status.description.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = line.status.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Status indicator
            StatusIndicator(statusType = line.status.type)
        }
    }
}
