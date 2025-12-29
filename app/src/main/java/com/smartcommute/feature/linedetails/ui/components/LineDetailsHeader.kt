package com.smartcommute.feature.linedetails.ui.components

import androidx.compose.animation.AnimatedVisibilityScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.smartcommute.R

@OptIn(ExperimentalGlideComposeApi::class, ExperimentalSharedTransitionApi::class)
@Composable
fun SharedTransitionScope.LineDetailsHeader(
    lineId: String,
    lineName: String,
    statusShortText: String,
    lineColor: Color,
    headerImageRes: String,
    animatedVisibilityScope: AnimatedVisibilityScope,
    modifier: Modifier = Modifier
) {
    BoxWithConstraints(
        modifier = modifier.fillMaxWidth()
    ) {
        val headerHeight = (maxHeight * 0.33f).coerceAtMost(300.dp)
        val iconSize = 64.dp
        val iconOffset = iconSize / 2

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(headerHeight + iconOffset)
        ) {
            // Header image container
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(headerHeight)
            ) {
                // Header image
                val imageResId = getHeaderImageResourceId(headerImageRes)
                GlideImage(
                    model = imageResId,
                    contentDescription = stringResource(R.string.format_station_image_desc, lineName),
                    modifier = Modifier.fillMaxWidth().height(headerHeight),
                    contentScale = ContentScale.Crop
                ) {
                    it.error(android.R.drawable.ic_menu_gallery)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                }

                // Dark gradient scrim for text readability
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(headerHeight)
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
private fun getHeaderImageResourceId(headerImageRes: String): Int {
    // Map resource names to drawable IDs
    // For now, return placeholder - actual images will be added in T031
    return android.R.drawable.ic_menu_gallery
}
