package com.example.monotodo.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.monotodo.ui.theme.MonoPalette

/**
 * Cached black-to-white vertical gradient background.
 * Drawn once per viewport size with drawWithCache for 0% idle GPU overhead.
 */
@Composable
fun MonochromeGradientBackdrop(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .drawWithCache {
                val brush = Brush.verticalGradient(
                    0.00f to MonoPalette.PureBlack,
                    0.26f to Color(0xFF141418),
                    0.52f to Color(0xFF2D2D33),
                    0.82f to Color(0xFFDCDCE2),
                    1.00f to MonoPalette.PureWhite
                )
                onDrawBehind {
                    drawRect(brush = brush)
                }
            }
    )
}

/**
 * Ultra-minimal 3dp animated monochrome progress hairline.
 * Isolated composable so animating fill never recomposes the rest of the screen.
 */
@Composable
fun MinimalProgressHairline(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val animatedFill by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioNoBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "progressHairline"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.14f))
    ) {
        if (animatedFill > 0.004f) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedFill)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(MonoPalette.Silver, MonoPalette.PureWhite)
                        )
                    )
            )
        }
    }
}
