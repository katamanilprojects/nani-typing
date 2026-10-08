package com.manacdc.nanityping1

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

/**
 * Level 3: Minimalist Kinetic Physics Canvas.
 *
 * Implements real physical dynamics (gravity ramps, braking inertia, elastic bounce,
 * measured crawl, and high-velocity acceleration) using Jetpack Compose's hardware-accelerated
 * Animatable engine.
 *
 * Visual Aesthetic: Pure, tactile engineering model. Zero cartoon clutter or fireworks.
 */
@Composable
fun KineticPhysicsOverlay(
    action: KineticActionType,
    onFinish: () -> Unit
) {
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.12f)) // Subtle focus isolation
    ) {
        if (constraints.maxWidth <= 0 || constraints.maxHeight <= 0) {
            return@BoxWithConstraints
        }

        val screenWidth = constraints.maxWidth.toFloat()
        val screenHeight = constraints.maxHeight.toFloat()

        val vehicleWidth = 160f
        val vehicleHeight = 68f

        val offsetX = remember { Animatable(-300f) }
        val offsetY = remember { Animatable(screenHeight * 0.5f) }
        val rotation = remember { Animatable(0f) }
        val scaleX = remember { Animatable(1f) }
        val scaleY = remember { Animatable(1f) }
        val opacity = remember { Animatable(1f) }

        var showRamp by remember { mutableStateOf(false) }
        val rampStartX = screenWidth * 0.15f
        val rampStartY = screenHeight * 0.30f
        val rampEndX = screenWidth * 0.85f
        val rampEndY = screenHeight * 0.75f

        LaunchedEffect(action) {
            when (action) {
                KineticActionType.FAST -> {
                    // High-velocity linear rush across the 1080p canvas (~450ms)
                    offsetY.snapTo(screenHeight * 0.5f)
                    offsetX.snapTo(-vehicleWidth - 50f)
                    offsetX.animateTo(
                        targetValue = screenWidth + 100f,
                        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing)
                    )
                    delay(100)
                    onFinish()
                }

                KineticActionType.SLOW -> {
                    // Deliberate, measured crawl across the screen (3.5s)
                    offsetY.snapTo(screenHeight * 0.5f)
                    offsetX.snapTo(-vehicleWidth)
                    offsetX.animateTo(
                        targetValue = screenWidth + 50f,
                        animationSpec = tween(durationMillis = 3500, easing = LinearEasing)
                    )
                    onFinish()
                }

                KineticActionType.STOP -> {
                    // Quick entry followed by abrupt braking, suspension tilt, and recoil
                    offsetY.snapTo(screenHeight * 0.5f)
                    offsetX.snapTo(-vehicleWidth)
                    offsetX.animateTo(
                        targetValue = (screenWidth - vehicleWidth) * 0.5f,
                        animationSpec = tween(durationMillis = 380, easing = LinearOutSlowInEasing)
                    )
                    // Braking inertia tilt
                    rotation.animateTo(-7f, animationSpec = tween(50))
                    rotation.animateTo(
                        0f,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMedium
                        )
                    )
                    delay(1200)
                    opacity.animateTo(0f, animationSpec = tween(300))
                    onFinish()
                }

                KineticActionType.DOWN -> {
                    // Gravitational ramp incline descent (matching his physical pillow/doormat ramps)
                    showRamp = true
                    offsetX.snapTo(rampStartX - 50f)
                    offsetY.snapTo(rampStartY - 40f)
                    rotation.snapTo(26f) // Align with incline slope

                    // Accelerate down the ramp under simulated gravity
                    launch {
                        offsetX.animateTo(
                            targetValue = rampEndX,
                            animationSpec = tween(durationMillis = 900, easing = FastOutLinearInEasing)
                        )
                    }
                    launch {
                        offsetY.animateTo(
                            targetValue = rampEndY - 30f,
                            animationSpec = tween(durationMillis = 900, easing = FastOutLinearInEasing)
                        )
                    }
                    delay(920)
                    // Level out onto the flat surface
                    rotation.animateTo(0f, animationSpec = tween(120))
                    offsetX.animateTo(
                        targetValue = screenWidth + 50f,
                        animationSpec = tween(durationMillis = 450, easing = LinearOutSlowInEasing)
                    )
                    onFinish()
                }

                KineticActionType.UP -> {
                    // Powering uphill against gravity
                    showRamp = true
                    offsetX.snapTo(rampEndX + 20f)
                    offsetY.snapTo(rampEndY - 30f)
                    rotation.snapTo(-154f) // Facing uphill left

                    launch {
                        offsetX.animateTo(
                            targetValue = rampStartX,
                            animationSpec = tween(durationMillis = 1100, easing = LinearOutSlowInEasing)
                        )
                    }
                    launch {
                        offsetY.animateTo(
                            targetValue = rampStartY - 40f,
                            animationSpec = tween(durationMillis = 1100, easing = LinearOutSlowInEasing)
                        )
                    }
                    delay(1150)
                    rotation.animateTo(-180f, animationSpec = tween(120))
                    delay(600)
                    opacity.animateTo(0f, animationSpec = tween(300))
                    onFinish()
                }

                KineticActionType.BOUNCE -> {
                    // Free-fall drop with elastic spring rebound and squash-and-stretch
                    val groundY = screenHeight * 0.65f
                    offsetX.snapTo((screenWidth - vehicleWidth) * 0.5f)
                    offsetY.snapTo(-vehicleHeight - 40f)

                    // Drop to ground with spring rebound
                    offsetY.animateTo(
                        targetValue = groundY,
                        animationSpec = spring(
                            dampingRatio = Spring.DampingRatioHighBouncy,
                            stiffness = Spring.StiffnessLow
                        )
                    )
                    // Squash on impact
                    scaleX.animateTo(1.25f, animationSpec = tween(80))
                    scaleY.animateTo(0.75f, animationSpec = tween(80))
                    scaleX.animateTo(1.0f, animationSpec = spring(Spring.DampingRatioMediumBouncy))
                    scaleY.animateTo(1.0f, animationSpec = spring(Spring.DampingRatioMediumBouncy))

                    delay(900)
                    opacity.animateTo(0f, animationSpec = tween(300))
                    onFinish()
                }

                KineticActionType.JUMP -> {
                    // Parabolic arc jump
                    val groundY = screenHeight * 0.60f
                    val jumpApexY = screenHeight * 0.25f
                    val startX = (screenWidth - vehicleWidth) * 0.40f

                    offsetX.snapTo(startX)
                    offsetY.snapTo(groundY)

                    // Ascend to apex
                    launch {
                        offsetX.animateTo(startX + 180f, tween(400, easing = LinearEasing))
                    }
                    offsetY.animateTo(jumpApexY, tween(400, easing = FastOutSlowInEasing))

                    // Descend back to ground
                    launch {
                        offsetX.animateTo(startX + 360f, tween(400, easing = LinearEasing))
                    }
                    offsetY.animateTo(groundY, tween(400, easing = FastOutLinearInEasing))

                    delay(800)
                    opacity.animateTo(0f, animationSpec = tween(300))
                    onFinish()
                }
            }
        }

        // Render clean structural ramp line if DOWN or UP is active
        if (showRamp) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawLine(
                    color = Color(0xFF0B1B3D).copy(alpha = 0.45f),
                    start = Offset(rampStartX, rampStartY),
                    end = Offset(rampEndX, rampEndY),
                    strokeWidth = 10f,
                    cap = StrokeCap.Round
                )
                // Ramp base ground support
                drawLine(
                    color = Color(0xFF0B1B3D).copy(alpha = 0.25f),
                    start = Offset(rampEndX, rampEndY),
                    end = Offset(screenWidth, rampEndY),
                    strokeWidth = 6f,
                    cap = StrokeCap.Square
                )
            }
        }

        // The Tactile Minimalist Vehicle
        Box(
            modifier = Modifier
                .offset { IntOffset(offsetX.value.roundToInt(), offsetY.value.roundToInt()) }
                .rotate(rotation.value)
                .scale(scaleX.value, scaleY.value)
                .alpha(opacity.value)
        ) {
            TactileVehicle(
                actionLabel = action.name
            )
        }
    }
}

/**
 * Minimalist, tactile wooden/architectural-style vehicle.
 * Free from cartoon eyes or flashy lights; treats the child's engineering curiosity with dignity.
 */
@Composable
private fun TactileVehicle(
    actionLabel: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .width(160.dp)
            .height(68.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0B1B3D), // Deep solid navy chassis
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Upper aerodynamic cabin with label
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sleek cabin window block
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFFDD0).copy(alpha = 0.85f), // Warm cream window
                    modifier = Modifier
                        .width(42.dp)
                        .height(18.dp)
                ) {}

                // Embedded bold kinetic label
                Text(
                    text = actionLabel,
                    color = Color(0xFFFF4500), // Vibrant orange
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.SansSerif
                )
            }

            // Lower chassis & dual tactile wheels
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 2.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Front wheel
                TactileWheel()
                // Rear wheel
                TactileWheel()
            }
        }
    }
}

@Composable
private fun TactileWheel() {
    Box(
        modifier = Modifier
            .size(24.dp)
            .background(Color(0xFF1E293B), shape = CircleShape), // Charcoal tire
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(Color(0xFFFF4500), shape = CircleShape) // Orange wheel hub
        )
    }
}
