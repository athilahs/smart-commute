package com.smartcommute.feature.linedetails.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.smartcommute.R

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LineDetailsHeader(
    lineId: String,
    lineName: String,
    statusShortText: String,
    lineColor: Color,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier,
    contentAlpha: Float = 1f
) {
    val density = LocalDensity.current
    val statusBarHeight = WindowInsets.statusBars.getTop(density)

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val headerHeight = (maxHeight * 0.33f).coerceAtMost(300.dp)
        val iconSize = 64.dp
        val iconOffset = iconSize / 2

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight + iconOffset + with(density) { statusBarHeight.toDp() })
        ) {
            // Header image container - extends behind status bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight + with(density) { statusBarHeight.toDp() })
            ) {
                // Header background - using Box with gradient brush for better rendering
                val gradient = getLineGradient(lineId)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight + with(density) { statusBarHeight.toDp() })
                        .background(gradient)
                )

                // Dark gradient scrim for text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight + with(density) { statusBarHeight.toDp() })
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    Color.Black.copy(alpha = 0.7f)
                                ),
                                startY = 0f,
                                endY = Float.POSITIVE_INFINITY
                            )
                        )
                )

                // Line name and status (bottom-left) with shared element transition
                Column(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(16.dp)
                        .padding(bottom = iconOffset)
                        .alpha(contentAlpha)
                ) {
                    Text(
                        text = lineName,
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "line_name_$lineId"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                    Text(
                        text = statusShortText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "line_status_$lineId"),
                            animatedVisibilityScope = animatedVisibilityScope
                        )
                    )
                }
            }

            // Line icon (at bottom, overlapping image and card section) with shared element
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
                    .padding(bottom = iconOffset / 2)
                    .size(iconSize)
                    .alpha(contentAlpha)
                    .clip(CircleShape)
                    .background(lineColor.copy(alpha = 0.15f))
                    .sharedElement(
                        rememberSharedContentState(key = "line_icon_$lineId"),
                        animatedVisibilityScope = animatedVisibilityScope
                    )
                    .zIndex(10f),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_tfl_roundel),
                    contentDescription = null,
                    tint = lineColor,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
    }
}

@Composable
private fun getLineGradient(lineId: String): Brush {
    // Normalize the line ID
    val normalizedId = lineId.lowercase().replace("-", "").replace(" ", "")

    return when (normalizedId) {
        "bakerloo" -> Brush.linearGradient(
            colors = listOf(Color(0xFFB36305), Color(0xFF8B4E04), Color(0xFFB36305))
        )
        "central" -> Brush.linearGradient(
            colors = listOf(Color(0xFFDC241F), Color(0xFFB01C18), Color(0xFFDC241F))
        )
        "circle" -> Brush.linearGradient(
            colors = listOf(Color(0xFFFFD329), Color(0xFFE6BE24), Color(0xFFFFD329))
        )
        "district" -> Brush.linearGradient(
            colors = listOf(Color(0xFF007D32), Color(0xFF006428), Color(0xFF007D32))
        )
        "hammersmithcity", "hammersmith&city" -> Brush.linearGradient(
            colors = listOf(Color(0xFFF491A8), Color(0xFFE17A91), Color(0xFFF491A8))
        )
        "jubilee" -> Brush.linearGradient(
            colors = listOf(Color(0xFFA1A5A7), Color(0xFF8A8E90), Color(0xFFA1A5A7))
        )
        "metropolitan" -> Brush.linearGradient(
            colors = listOf(Color(0xFF9B0058), Color(0xFF7A0046), Color(0xFF9B0058))
        )
        "northern" -> Brush.linearGradient(
            colors = listOf(Color(0xFF000000), Color(0xFF1A1A1A), Color(0xFF000000))
        )
        "piccadilly" -> Brush.linearGradient(
            colors = listOf(Color(0xFF0019A8), Color(0xFF001486), Color(0xFF0019A8))
        )
        "victoria" -> Brush.linearGradient(
            colors = listOf(Color(0xFF0098D8), Color(0xFF007AAD), Color(0xFF0098D8))
        )
        "waterloocity", "waterloo&city" -> Brush.linearGradient(
            colors = listOf(Color(0xFF93CEBA), Color(0xFF7AB9A6), Color(0xFF93CEBA))
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(0xFF808080), Color(0xFF606060), Color(0xFF808080))
        )
    }
}
