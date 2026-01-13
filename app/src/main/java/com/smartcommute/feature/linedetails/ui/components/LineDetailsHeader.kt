package com.smartcommute.feature.linedetails.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.FastOutSlowInEasing
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
                val scrimEndColor = getLineGradientEndColor(lineId)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight + with(density) { statusBarHeight.toDp() })
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.Black.copy(alpha = 0.6f),
                                    scrimEndColor.copy(alpha = 0.3f)
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
                            animatedVisibilityScope = animatedVisibilityScope,
                            boundsTransform = { _, _ ->
                                tween(
                                    durationMillis = 500,
                                    easing = FastOutSlowInEasing
                                )
                            }
                        )
                    )
                    Text(
                        text = statusShortText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White,
                        modifier = Modifier.sharedElement(
                            rememberSharedContentState(key = "line_status_$lineId"),
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
                    .background(lineColor.copy(alpha = 0.50f))
                    .sharedElement(
                        rememberSharedContentState(key = "line_icon_$lineId"),
                        animatedVisibilityScope = animatedVisibilityScope,
                        boundsTransform = { _, _ ->
                            tween(
                                durationMillis = 500,
                                easing = FastOutSlowInEasing
                            )
                        }
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
            colors = listOf(Color(0xFFB36305), Color(0xFFD88A30), Color.White)
        )
        "central" -> Brush.linearGradient(
            colors = listOf(Color(0xFFDC241F), Color(0xFFE85C56), Color.White)
        )
        "circle" -> Brush.linearGradient(
            colors = listOf(Color(0xFFFFD329), Color(0xFFE6BE24), Color.Black)
        )
        "district" -> Brush.linearGradient(
            colors = listOf(Color(0xFF007D32), Color(0xFF33A35C), Color.White)
        )
        "hammersmithcity", "hammersmith&city" -> Brush.linearGradient(
            colors = listOf(Color(0xFFF491A8), Color(0xFFE17A91), Color.Black)
        )
        "jubilee" -> Brush.linearGradient(
            colors = listOf(Color(0xFFA1A5A7), Color(0xFF8A8E90), Color.Black)
        )
        "metropolitan" -> Brush.linearGradient(
            colors = listOf(Color(0xFF9B0058), Color(0xFFC03380), Color.White)
        )
        "northern" -> Brush.linearGradient(
            colors = listOf(Color(0xFF000000), Color(0xFF666666), Color.White)
        )
        "piccadilly" -> Brush.linearGradient(
            colors = listOf(Color(0xFF0019A8), Color(0xFF3366CC), Color.White)
        )
        "victoria" -> Brush.linearGradient(
            colors = listOf(Color(0xFF0098D8), Color(0xFF33B3E6), Color.White)
        )
        "waterloocity", "waterloo&city" -> Brush.linearGradient(
            colors = listOf(Color(0xFF93CEBA), Color(0xFF7AB9A6), Color.Black)
        )
        else -> Brush.linearGradient(
            colors = listOf(Color(0xFF808080), Color(0xFF606060), Color.White)
        )
    }
}

@Composable
private fun getLineGradientEndColor(lineId: String): Color {
    // Normalize the line ID
    val normalizedId = lineId.lowercase().replace("-", "").replace(" ", "")

    return when (normalizedId) {
        "bakerloo" -> Color.White
        "central" -> Color.White
        "circle" -> Color(0xFFFFD329)
        "district" -> Color.White
        "hammersmithcity", "hammersmith&city" -> Color(0xFFF491A8)
        "jubilee" -> Color(0xFFA1A5A7)
        "metropolitan" -> Color.White
        "northern" -> Color.White
        "piccadilly" -> Color.White
        "victoria" -> Color.White
        "waterloocity", "waterloo&city" -> Color(0xFF93CEBA)
        else -> Color(0xFF808080)
    }
}
