package com.example.monotodo.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.monotodo.ui.theme.MonoPalette
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * GPU-accelerated press-bounce modifier for silky tactile buttons and cards.
 */
fun Modifier.bounceClick(
    scaleDown: Float = 0.96f,
    onClick: () -> Unit
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) scaleDown else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "bounceScale"
    )
    this
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = onClick
        )
}

/**
 * Continuous pure black-to-white monochrome gradient with softly drifting ambient light orbs.
 */
@Composable
fun AnimatedMonochromeBackdrop(modifier: Modifier = Modifier) {
    val infiniteTransition = rememberInfiniteTransition(label = "monoBackdrop")
    val drift by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 9000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "gradientDrift"
    )
    val orbAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = (2.0 * PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 16000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbOrbit"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val bgBrush = Brush.verticalGradient(
            0.00f to MonoPalette.PureBlack,
            (0.20f + drift * 0.05f) to MonoPalette.Obsidian,
            (0.44f + drift * 0.06f) to Color(0xFF28282E),
            (0.70f + drift * 0.05f) to Color(0xFFA8A8B2),
            0.91f to MonoPalette.OffWhite,
            1.00f to MonoPalette.PureWhite
        )
        drawRect(brush = bgBrush)

        // Subtle floating silver-white halo near upper-right dark zone
        val orb1Center = Offset(
            x = size.width * (0.82f + 0.08f * cos(orbAngle)),
            y = size.height * (0.14f + 0.04f * sin(orbAngle))
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.11f),
                    Color.White.copy(alpha = 0.03f),
                    Color.Transparent
                ),
                center = orb1Center,
                radius = size.width * 0.55f
            ),
            radius = size.width * 0.55f,
            center = orb1Center
        )

        // Subtle dark obsidian aura drifting near lower-left white zone
        val orb2Center = Offset(
            x = size.width * (0.18f + 0.09f * sin(orbAngle)),
            y = size.height * (0.86f + 0.04f * cos(orbAngle))
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    Color.Black.copy(alpha = 0.08f),
                    Color.Black.copy(alpha = 0.02f),
                    Color.Transparent
                ),
                center = orb2Center,
                radius = size.width * 0.50f
            ),
            radius = size.width * 0.50f,
            center = orb2Center
        )
    }
}

/**
 * Smooth spring-animated circular progress ring with monochrome sweep gradient.
 */
@Composable
fun AnimatedMonoProgressRing(
    progress: Float,
    ringSize: Dp = 54.dp,
    strokeWidth: Dp = 5.dp
) {
    val animatedSweep by animateFloatAsState(
        targetValue = (progress.coerceIn(0f, 1f)) * 360f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "ringSweep"
    )
    val percent = (progress.coerceIn(0f, 1f) * 100f).toInt()

    Box(
        modifier = Modifier.size(ringSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokePx = strokeWidth.toPx()
            val inset = strokePx / 2f
            val arcSize = Size(size.width - strokePx, size.height - strokePx)
            drawArc(
                color = Color.White.copy(alpha = 0.16f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = Offset(inset, inset),
                size = arcSize,
                style = Stroke(width = strokePx, cap = StrokeCap.Round)
            )
            if (animatedSweep > 0.5f) {
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(
                            MonoPalette.Silver,
                            MonoPalette.PureWhite,
                            MonoPalette.Mist,
                            MonoPalette.PureWhite
                        )
                    ),
                    startAngle = -90f,
                    sweepAngle = animatedSweep,
                    useCenter = false,
                    topLeft = Offset(inset, inset),
                    size = arcSize,
                    style = Stroke(width = strokePx, cap = StrokeCap.Round)
                )
            }
        }
        Text(
            text = "$percent%",
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = MonoPalette.PureWhite
        )
    }
}

/**
 * Radial monochrome particle sparkle burst around a checkbox when toggled complete.
 */
@Composable
fun CheckSparkleBurst(
    trigger: Boolean,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(1f) }
    val prevTrigger = remember { androidx.compose.runtime.mutableStateOf(trigger) }
    LaunchedEffect(trigger) {
        val wasDone = prevTrigger.value
        prevTrigger.value = trigger
        if (!wasDone && trigger) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 480, easing = FastOutSlowInEasing)
            )
        }
    }
    val p = progress.value
    if (p < 0.99f) {
        Canvas(modifier = modifier.size(44.dp)) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxR = size.minDimension * 0.52f
            val count = 8
            for (i in 0 until count) {
                val angle = (i * (360f / count)) * (PI / 180.0)
                val dist = maxR * (0.35f + 0.65f * p)
                val pos = Offset(
                    x = center.x + (cos(angle) * dist).toFloat(),
                    y = center.y + (sin(angle) * dist).toFloat()
                )
                val dotRadius = (3.4.dp.toPx()) * (1f - p)
                drawCircle(
                    color = if (i % 2 == 0) MonoPalette.PureBlack else MonoPalette.SlateGray,
                    radius = dotRadius.coerceAtLeast(0.5f),
                    center = pos,
                    alpha = (1f - p).coerceIn(0f, 1f)
                )
            }
        }
    }
}

/**
 * Full-screen monochrome celebration burst triggered when 100% of tasks are completed.
 */
@Composable
fun MonochromeCelebrationOverlay(
    burstNonce: Int,
    modifier: Modifier = Modifier
) {
    val progress = remember { Animatable(1f) }

    LaunchedEffect(burstNonce) {
        if (burstNonce > 0) {
            progress.snapTo(0f)
            progress.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 1050, easing = FastOutSlowInEasing)
            )
        }
    }

    val p = progress.value
    if (burstNonce > 0 && p < 0.99f) {
        Canvas(modifier = modifier.fillMaxSize()) {
            val origin = Offset(size.width * 0.5f, size.height * 0.24f)
            val particleCount = 26
            for (i in 0 until particleCount) {
                val angle = (i * (360f / particleCount) + (i % 3) * 9f) * (PI / 180.0)
                val speed = size.width * (0.28f + (i % 5) * 0.08f)
                val gravity = size.height * 0.14f * p * p
                val x = origin.x + (cos(angle) * speed * p).toFloat()
                val y = origin.y + (sin(angle) * speed * p).toFloat() + gravity
                val alpha = (1f - p).coerceIn(0f, 1f)
                val radius = ((3.5f + (i % 4) * 1.4f).dp.toPx()) * (1f - p * 0.45f)
                val color = when (i % 3) {
                    0 -> MonoPalette.PureWhite
                    1 -> MonoPalette.Silver
                    else -> MonoPalette.PureBlack
                }
                drawCircle(
                    color = color,
                    radius = radius.coerceAtLeast(1f),
                    center = Offset(x, y),
                    alpha = alpha
                )
            }
        }
    }
}
