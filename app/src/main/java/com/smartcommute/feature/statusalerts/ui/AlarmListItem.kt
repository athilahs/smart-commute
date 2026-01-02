package com.smartcommute.feature.statusalerts.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smartcommute.feature.statusalerts.domain.model.StatusAlert

/**
 * List item composable for displaying a status alarm.
 * Shows time, weekdays, multiple tube lines with badges, and enable/disable toggle.
 */
@Composable
fun AlarmListItem(
    alarm: StatusAlert,
    onToggleEnabled: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left side: Time and details
            Column(
                modifier = Modifier.weight(1f)
            ) {
                // Time display
                Text(
                    text = alarm.getDisplayTime(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (alarm.isEnabled) {
                        MaterialTheme.colorScheme.onSurface
                    } else {
                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f)
                    }
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Days display
                Text(
                    text = alarm.getDisplayDays(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (alarm.isEnabled) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Tube lines badges
                if (alarm.selectedTubeLines.isNotEmpty()) {
                    TubeLineBadges(
                        lineIds = alarm.selectedTubeLines,
                        isEnabled = alarm.isEnabled
                    )
                }
            }

            // Right side: Enable/disable toggle
            Switch(
                checked = alarm.isEnabled,
                onCheckedChange = onToggleEnabled
            )
        }
    }
}

/**
 * Displays tube line badges in a row.
 */
@Composable
private fun TubeLineBadges(
    lineIds: Set<String>,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Show first 3 lines as badges, then "+X more" if there are more
        val displayLines = lineIds.take(3)
        val remainingCount = lineIds.size - displayLines.size

        displayLines.forEach { lineId ->
            TubeLineBadge(
                lineName = lineId.capitalize(),
                isEnabled = isEnabled
            )
        }

        if (remainingCount > 0) {
            Text(
                text = "+$remainingCount",
                style = MaterialTheme.typography.bodySmall,
                color = if (isEnabled) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                },
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(start = 4.dp)
            )
        }
    }
}

/**
 * Individual tube line badge.
 */
@Composable
private fun TubeLineBadge(
    lineName: String,
    isEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(4.dp),
        color = if (isEnabled) {
            getLineColor(lineName).copy(alpha = 0.2f)
        } else {
            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        }
    ) {
        Text(
            text = lineName,
            style = MaterialTheme.typography.labelSmall,
            color = if (isEnabled) {
                getLineColor(lineName)
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
            },
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

/**
 * Returns color for a tube line based on its name.
 * These are approximate colors for London Underground lines.
 */
private fun getLineColor(lineName: String): Color {
    return when (lineName.lowercase()) {
        "bakerloo" -> Color(0xFFB36305)
        "central" -> Color(0xFFDC241F)
        "circle" -> Color(0xFFFFD300)
        "district" -> Color(0xFF00782A)
        "hammersmith & city", "hammersmith" -> Color(0xFFF3A9BB)
        "jubilee" -> Color(0xFFA0A5A9)
        "metropolitan" -> Color(0xFF9B0056)
        "northern" -> Color(0xFF000000)
        "piccadilly" -> Color(0xFF003688)
        "victoria" -> Color(0xFF0098D4)
        "waterloo & city", "waterloo" -> Color(0xFF95CDBA)
        "elizabeth" -> Color(0xFF7156A5)
        else -> Color.Gray
    }
}

/**
 * Extension function to capitalize first letter.
 */
private fun String.capitalize(): String {
    return this.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
}
