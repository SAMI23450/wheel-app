package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashFinished: () -> Unit) {
    // 0: Welcome text phase, 1: Author text phase
    var phase by remember { mutableStateOf(0) }

    // Constant spinning animation for the background wheel icon
    val infiniteTransition = rememberInfiniteTransition(label = "Splash Wheel Spin")
    val spinRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(4000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "Spin Rotation"
    )

    val wheelPulse by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Pulse Scaling"
    )

    // Animation progress states for texts
    val textAlpha = remember { Animatable(0f) }
    val textScale = remember { Animatable(0.7f) }

    // Sequential timing trigger
    LaunchedEffect(Unit) {
        // --- PHASE 1: Welcome to Wheel App ---
        textAlpha.animateTo(1f, animationSpec = tween(1000, easing = EaseOutCubic))
        textScale.animateTo(1f, animationSpec = tween(1000, easing = EaseOutCubic))
        
        delay(1500) // Hold welcome text for 1.5 seconds (total ~2.5 seconds)
        
        // Fade out
        textAlpha.animateTo(0f, animationSpec = tween(500, easing = EaseInCubic))
        textScale.snapTo(0.7f)
        
        // Transition to Phase 2
        phase = 1
        
        // --- PHASE 2: Made It By dorowu48 ---
        textAlpha.animateTo(1f, animationSpec = tween(800, easing = EaseOutCubic))
        textScale.animateTo(1f, animationSpec = tween(800, easing = EaseOutCubic))
        
        delay(1500) // Hold author text for 1.5 seconds
        
        // Fade out before transition
        textAlpha.animateTo(0f, animationSpec = tween(500, easing = EaseInCubic))
        
        onSplashFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        // 1. Spinning, pulsing decorative background Wheel Canvas
        Canvas(
            modifier = Modifier
                .size(240.dp)
                .scale(wheelPulse)
        ) {
            val center = size / 2f
            val radius = size.minDimension / 2.5f
            val strokeWidth = 5.dp.toPx()
            
            // Draw outer wheel rim
            drawCircle(
                color = Color(0xFF6750A4).copy(alpha = 0.15f),
                radius = radius,
                style = Stroke(width = strokeWidth)
            )

            // Draw inner hub
            drawCircle(
                color = Color(0xFF6750A4).copy(alpha = 0.25f),
                radius = radius * 0.15f
            )

            // Draw spokes rotating
            val spokes = 8
            for (i in 0 until spokes) {
                val angleRad = Math.toRadians((i * (360f / spokes) + spinRotation).toDouble())
                val startX = center.width + (radius * 0.15f * Math.cos(angleRad)).toFloat()
                val startY = center.height + (radius * 0.15f * Math.sin(angleRad)).toFloat()
                val endX = center.width + (radius * Math.cos(angleRad)).toFloat()
                val endY = center.height + (radius * Math.sin(angleRad)).toFloat()
                
                drawLine(
                    color = Color(0xFF6750A4).copy(alpha = 0.25f),
                    start = androidx.compose.ui.geometry.Offset(startX, startY),
                    end = androidx.compose.ui.geometry.Offset(endX, endY),
                    strokeWidth = 3.dp.toPx()
                )
            }
        }

        // 2. Animated Center Text
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier
                    .splashGraphicsLayer(
                        alpha = textAlpha.value,
                        scaleX = textScale.value,
                        scaleY = textScale.value
                    )
            ) {
                if (phase == 0) {
                    Text(
                        text = "Welcome to\nWheel App",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center,
                        lineHeight = 40.sp,
                        letterSpacing = 1.sp
                    )
                } else {
                    Text(
                        text = "Made By\ndorowu48",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.secondary,
                        textAlign = TextAlign.Center,
                        lineHeight = 28.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}

// Inline helper for graphicsLayer translation to avoid creating extra code
private fun Modifier.splashGraphicsLayer(
    alpha: Float,
    scaleX: Float,
    scaleY: Float
): Modifier = this.then(
    Modifier.scale(scaleX, scaleY).graphicsLayer {
        this.alpha = alpha
    }
)
