package com.smartcommute.feature.linedetails.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun LineDetailsHeader(
    lineName: String,
    statusShortText: String,
    headerImageRes: String,
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
                    contentDescription = "Station image for $lineName",
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

                // Line name and status (bottom-left)
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
                        color = Color.White
                    )
                    Text(
                        text = statusShortText,
                        style = MaterialTheme.typography.bodyLarge,
                        color = Color.White
                    )
                }
            }

            // Line icon (at bottom, overlapping image and card section)
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp)
                    .padding(bottom = iconOffset / 2)
                    .size(iconSize)
                    .zIndex(10f)
                    .background(Color.White, shape = MaterialTheme.shapes.medium)
            ) {
                // Placeholder for line icon - actual icon will be added based on line color
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

/**
 * Linear interpolation between two Dp values
 */
private fun lerp(start: Dp, stop: Dp, fraction: Float): Dp {
    return start + (stop - start) * fraction
}
