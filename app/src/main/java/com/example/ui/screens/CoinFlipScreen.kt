package com.example.ui.screens

import android.graphics.Paint
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.viewmodel.WheelViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun CoinFlipScreen(viewModel: WheelViewModel) {
    val isFlipping by viewModel.isFlipping.collectAsState()
    val rotationX by viewModel.coinRotationX.collectAsState()
    val coinResult by viewModel.coinResult.collectAsState()
    val flipHistory by viewModel.coinFlipHistory.collectAsState()

    // Formatting date helper
    val dateFormat = remember { SimpleDateFormat("hh:mm:ss a, dd MMM", Locale.getDefault()) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp)
        ) {
            // 1. Coin Flip Header
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Coin Flipper",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary, // Theme Primary (Lavender)
                    textAlign = TextAlign.Center
                )
                Text(
                    text = "Test your luck in 3D space",
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp)
                )
                Spacer(modifier = Modifier.height(36.dp))
            }

            // 2. Realistic 3D Coin Graphic
            item {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .graphicsLayer {
                            this.rotationX = rotationX
                            this.cameraDistance = 12f * density
                        }
                        .shadow(16.dp, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    // Decide whether we are showing the FRONT (Heads) or BACK (Tails) based on the angle
                    val angleMod = (rotationX % 360f + 360f) % 360f
                    val isHeadsFace = angleMod < 90f || angleMod > 270f

                    if (isHeadsFace) {
                        HeadsFace()
                    } else {
                        // We must mirror the backside rendering vertically because rotationX rotates it upside down!
                        TailsFace(modifier = Modifier.graphicsLayer { this.rotationX = 180f })
                    }
                }
                Spacer(modifier = Modifier.height(36.dp))
            }

            // 3. FLIP Action Button
            item {
                Button(
                    onClick = { viewModel.flipCoin() },
                    enabled = !isFlipping,
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary, // Theme Primary (Lavender)
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                        disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                        disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.5f)
                    ),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 8.dp,
                        pressedElevation = 2.dp
                    ),
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(52.dp)
                ) {
                    Text(
                        text = if (isFlipping) "FLIPPING..." else "FLIP COIN",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
            }

            // 4. Dynamic Coin landing readout
            item {
                if (coinResult != null && !isFlipping) {
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (coinResult == "Heads") Color(0xFF3E2723) else Color(0xFF263238)
                        ),
                        modifier = Modifier.padding(horizontal = 24.dp)
                    ) {
                        Text(
                            text = "Result: ${coinResult!!.uppercase()}",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.padding(horizontal = 32.dp, vertical = 12.dp)
                        )
                    }
                } else if (isFlipping) {
                    Text(
                        text = "Spinning in mid-air...",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                }
                Spacer(modifier = Modifier.height(32.dp))
            }

            // 5. Coin History Header and List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Default.History,
                            contentDescription = "Coin History",
                            tint = MaterialTheme.colorScheme.primary, // Theme primary lavender
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Recent Flips",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground
                        )
                    }
                    if (flipHistory.isNotEmpty()) {
                        IconButton(onClick = { viewModel.clearCoinHistory() }) {
                            Icon(
                                Icons.Default.DeleteSweep,
                                contentDescription = "Clear History",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }

            if (flipHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 12.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = "No coin flips yet. Flip the coin to save results!",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp)
                        )
                    }
                }
            } else {
                items(flipHistory) { record ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(32.dp)
                                        .background(
                                            color = if (record.result == "Heads") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary,
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = record.result.take(1),
                                        fontWeight = FontWeight.Black,
                                        fontSize = 15.sp,
                                        color = Color.Black
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = record.result,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            }
                            Text(
                                text = dateFormat.format(Date(record.timestamp)),
                                fontSize = 12.sp,
                                color = Color.White.copy(alpha = 0.5f)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun HeadsFace(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.1f
        val strokeGold = 8.dp.toPx()

        // Shiny gold background gradient
        val shinyBrush = Brush.radialGradient(
            colors = listOf(Color(0xFFFFF2A3), Color(0xFFFFD700), Color(0xFFB8860B)),
            center = center,
            radius = radius
        )

        drawCircle(
            brush = shinyBrush,
            radius = radius,
            center = center
        )

        // Outer rim border
        drawCircle(
            color = Color(0xFF8B6508),
            radius = radius,
            center = center,
            style = Stroke(width = strokeGold)
        )

        // Inner dotted ring
        drawCircle(
            color = Color(0xFF8B6508).copy(alpha = 0.4f),
            radius = radius * 0.85f,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Center Embossed "H"
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = android.graphics.Color.parseColor("#5C4033")
                textSize = size.minDimension * 0.4f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
                setShadowLayer(5f, 2f, 2f, android.graphics.Color.WHITE)
            }
            
            val yPos = center.y - ((paint.descent() + paint.ascent()) / 2f)
            canvas.nativeCanvas.drawText("H", center.x, yPos, paint)

            // HEADS caption
            val smallPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#5C4033")
                textSize = size.minDimension * 0.08f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.nativeCanvas.drawText("HEADS", center.x, center.y + radius * 0.6f, smallPaint)
        }
    }
}

@Composable
fun TailsFace(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.1f
        val strokeSilver = 8.dp.toPx()

        // Shiny silver gradient
        val shinyBrush = Brush.radialGradient(
            colors = listOf(Color(0xFFF8F9FA), Color(0xFFC0C0C0), Color(0xFF708090)),
            center = center,
            radius = radius
        )

        drawCircle(
            brush = shinyBrush,
            radius = radius,
            center = center
        )

        // Outer rim border
        drawCircle(
            color = Color(0xFF4F4F4F),
            radius = radius,
            center = center,
            style = Stroke(width = strokeSilver)
        )

        // Inner dotted ring
        drawCircle(
            color = Color(0xFF4F4F4F).copy(alpha = 0.4f),
            radius = radius * 0.85f,
            center = center,
            style = Stroke(width = 2.dp.toPx())
        )

        // Center Embossed "T"
        drawIntoCanvas { canvas ->
            val paint = Paint().apply {
                color = android.graphics.Color.parseColor("#2F4F4F")
                textSize = size.minDimension * 0.4f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
                setShadowLayer(5f, 2f, 2f, android.graphics.Color.WHITE)
            }
            
            val yPos = center.y - ((paint.descent() + paint.ascent()) / 2f)
            canvas.nativeCanvas.drawText("T", center.x, yPos, paint)

            // TAILS caption
            val smallPaint = Paint().apply {
                color = android.graphics.Color.parseColor("#2F4F4F")
                textSize = size.minDimension * 0.08f
                textAlign = Paint.Align.CENTER
                isFakeBoldText = true
                isAntiAlias = true
            }
            canvas.nativeCanvas.drawText("TAILS", center.x, center.y + radius * 0.6f, smallPaint)
        }
    }
}
