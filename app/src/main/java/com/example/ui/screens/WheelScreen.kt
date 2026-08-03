package com.example.ui.screens

import android.graphics.Paint
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.model.WheelSlice
import com.example.ui.viewmodel.WheelViewModel

private fun safeParseColor(colorHex: String, defaultColor: Color = Color.Gray): Color {
    return try {
        val cleaned = colorHex.trim()
        val hex = if (cleaned.startsWith("#")) cleaned else "#$cleaned"
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        defaultColor
    }
}

// Vibrantly professional colors to use as defaults (with rich classic colors like Red, Green, Blue, etc.)
val PresetColors = listOf(
    "#FF3B30", // Vibrant Red
    "#34C759", // Vibrant Green
    "#007AFF", // Vibrant Blue
    "#FFCC00", // Vibrant Yellow
    "#FF9500", // Vibrant Orange
    "#AF52DE", // Vibrant Purple
    "#008080", // Deep Teal
    "#FF2D55", // Vibrant Pink
    "#5AC8FA", // Bright Cyan
    "#4CD964", // Bright Light Green
    "#D0BCFF", // Lavender (Theme Primary)
    "#EFB8C8", // Soft Pink (Theme Tertiary)
    "#CCC2DC", // Grayish Lavender (Theme Secondary)
    "#EADDFF"  // Light Lavender
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WheelScreen(viewModel: WheelViewModel) {
    val slices by viewModel.activeSlices.collectAsState()
    val wheelTitle by viewModel.wheelTitle.collectAsState()
    val isSpinning by viewModel.isSpinning.collectAsState()
    val rotationAngle by viewModel.wheelRotation.collectAsState()
    val winner by viewModel.spinWinner.collectAsState()
    val isFavorite by viewModel.isCurrentWheelFavorite.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var sliceToEditIndex by remember { mutableStateOf<Int?>(null) }
    var editTitleMode by remember { mutableStateOf(false) }
    var titleInput by remember { mutableStateOf(wheelTitle) }

    LaunchedEffect(wheelTitle) {
        titleInput = wheelTitle
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp), // space for bottom nav
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(16.dp)
        ) {
            // 1. Wheel Title / Rename Row
            item {
                Spacer(modifier = Modifier.height(8.dp))
                if (editTitleMode) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = titleInput,
                            onValueChange = { titleInput = it },
                            label = { Text("Wheel Name") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = {
                                viewModel.setWheelTitle(titleInput)
                                editTitleMode = false
                            },
                            colors = IconButtonDefaults.iconButtonColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Save Name")
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = wheelTitle.ifBlank { "Untitled Wheel" },
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onBackground,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(
                            onClick = { editTitleMode = true },
                            modifier = Modifier.size(36.dp)
                        ) {
                            Icon(
                                Icons.Default.Edit,
                                contentDescription = "Edit Name",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            // 2. Spinning Wheel Graphic Centerpiece
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    if (slices.isEmpty()) {
                        // Empty Wheel State
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .border(
                                    width = 3.dp,
                                    color = MaterialTheme.colorScheme.outlineVariant,
                                    shape = CircleShape
                                )
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                modifier = Modifier.padding(24.dp)
                            ) {
                                Icon(
                                    Icons.Default.HelpOutline,
                                    contentDescription = "No Slices",
                                    tint = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "Add names to get started",
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    } else {
                        // Drawing the wheel
                        WheelCanvas(
                            slices = slices,
                            rotationAngle = rotationAngle,
                            modifier = Modifier.fillMaxSize()
                        )

                        // Top pointer indicator
                        WheelPointer(
                            modifier = Modifier
                                .align(Alignment.TopCenter)
                                .offset(y = (-12).dp)
                        )

                        // Center spin button hub (styled to match the Elegant Dark center pin)
                        Button(
                            onClick = { viewModel.spinWheel() },
                            enabled = !isSpinning,
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.background,
                                contentColor = MaterialTheme.colorScheme.primary,
                                disabledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.8f),
                                disabledContentColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
                            ),
                            elevation = ButtonDefaults.buttonElevation(
                                defaultElevation = 8.dp,
                                pressedElevation = 2.dp
                            ),
                            modifier = Modifier
                                .size(76.dp)
                                .border(
                                    width = 4.dp,
                                    color = MaterialTheme.colorScheme.outline,
                                    shape = CircleShape
                                )
                                .shadow(8.dp, CircleShape)
                        ) {
                            Text(
                                text = "SPIN",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            // 3. Wheel Save and Favorite Quick Controls
            if (slices.isNotEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = { viewModel.saveCurrentWheel() },
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                                contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        ) {
                            Icon(Icons.Default.Save, contentDescription = "Save Wheel")
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Save Wheel")
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        IconButton(
                            onClick = { viewModel.toggleFavoriteCurrentWheel() },
                            colors = IconButtonDefaults.filledIconButtonColors(
                                containerColor = if (isFavorite) MaterialTheme.colorScheme.tertiaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            modifier = Modifier.size(48.dp)
                        ) {
                            Icon(
                                imageVector = if (isFavorite) Icons.Default.Star else Icons.Default.StarBorder,
                                contentDescription = "Favorite Wheel",
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }

            // 4. Header of Slices List
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Wheel Slices (${slices.size})",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Row {
                        if (slices.isNotEmpty()) {
                            TextButton(
                                onClick = { viewModel.clearAllSlices() },
                                colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                            ) {
                                Icon(Icons.Default.ClearAll, contentDescription = "Clear All")
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Clear All")
                            }
                        }
                        Button(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Slice")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add")
                        }
                    }
                }
            }

            // 5. Scrollable Slice Items List
            if (slices.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Your wheel is currently empty.",
                                fontSize = 15.sp,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(onClick = { showAddDialog = true }) {
                                Text("Add Your First Entry")
                            }
                        }
                    }
                }
            } else {
                itemsIndexed(slices) { index, slice ->
                    SliceItemRow(
                        index = index,
                        slice = slice,
                        onEdit = { sliceToEditIndex = index },
                        onDelete = { viewModel.removeSlice(index) }
                    )
                }
            }
        }

        // Celebrate popup (Winner display)
        AnimatedVisibility(
            visible = winner != null,
            enter = fadeIn() + scaleIn(animationSpec = tween(500)),
            exit = fadeOut(),
            modifier = Modifier.align(Alignment.Center)
        ) {
            winner?.let { win ->
                WinnerDialog(
                    winner = win,
                    onDismiss = { viewModel.dismissWinnerPopup() }
                )
            }
        }
    }

    // dialog for ADDING a slice
    if (showAddDialog) {
        SliceEditDialog(
            title = "Add New Slice",
            initialName = "",
            initialColorHex = PresetColors[slices.size % PresetColors.size],
            onDismiss = { showAddDialog = false },
            onConfirm = { name, color ->
                if (name.isNotBlank()) {
                    viewModel.addSlice(name, color)
                }
                showAddDialog = false
            }
        )
    }

    // dialog for EDITING a slice
    sliceToEditIndex?.let { index ->
        val slice = slices.getOrNull(index)
        if (slice != null) {
            SliceEditDialog(
                title = "Edit Slice",
                initialName = slice.name,
                initialColorHex = slice.colorHex,
                onDismiss = { sliceToEditIndex = null },
                onConfirm = { name, color ->
                    if (name.isNotBlank()) {
                        viewModel.updateSlice(index, name, color)
                    }
                    sliceToEditIndex = null
                }
            )
        } else {
            sliceToEditIndex = null
        }
    }
}

@Composable
fun WheelCanvas(
    slices: List<WheelSlice>,
    rotationAngle: Float,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val strokeWidth = with(density) { 6.dp.toPx() }
    val outlineColor = MaterialTheme.colorScheme.outline
    val backgroundColor = MaterialTheme.colorScheme.background

    Canvas(modifier = modifier) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val radius = size.minDimension / 2.1f
        val sweepAngle = 360f / slices.size

        // 1. Draw each colorful arc slice
        slices.forEachIndexed { i, slice ->
            val startAngle = (i * sweepAngle + rotationAngle) % 360f
            drawArc(
                color = safeParseColor(slice.colorHex),
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = true,
                size = size
            )

            // Draw divider radial lines
            val borderAngleRad = Math.toRadians(startAngle.toDouble())
            val lineEndX = center.x + (radius * Math.cos(borderAngleRad)).toFloat()
            val lineEndY = center.y + (radius * Math.sin(borderAngleRad)).toFloat()
            drawLine(
                color = Color.White.copy(alpha = 0.4f),
                start = center,
                end = Offset(lineEndX, lineEndY),
                strokeWidth = 1.5.dp.toPx()
            )
        }

        // 2. Draw slice name texts pointing radially inwards (brightness-aware contrast)
        drawIntoCanvas { canvas ->
            slices.forEachIndexed { i, slice ->
                val startAngle = (i * sweepAngle + rotationAngle) % 360f
                val midAngle = startAngle + sweepAngle / 2f
                
                canvas.nativeCanvas.save()
                canvas.nativeCanvas.rotate(midAngle, center.x, center.y)

                // Calculate background luminance for perfect text contrast
                val sliceColor = try {
                    android.graphics.Color.parseColor(slice.colorHex)
                } catch (e: Exception) {
                    android.graphics.Color.WHITE
                }
                val r = android.graphics.Color.red(sliceColor)
                val g = android.graphics.Color.green(sliceColor)
                val b = android.graphics.Color.blue(sliceColor)
                val luminance = 0.299f * r + 0.587f * g + 0.114f * b
                
                val (textColor, shadowColor) = if (luminance > 160) {
                    android.graphics.Color.parseColor("#1C1B1F") to android.graphics.Color.WHITE
                } else {
                    android.graphics.Color.WHITE to android.graphics.Color.BLACK
                }

                // Render parameters
                val paint = Paint().apply {
                    color = textColor
                    textSize = if (slices.size > 10) 36f else 46f
                    textAlign = Paint.Align.RIGHT
                    isFakeBoldText = true
                    isAntiAlias = true
                    setShadowLayer(4f, 1f, 1f, shadowColor)
                }

                // Draw text slightly inside the outer boundary
                val xPos = center.x + radius * 0.9f
                val yPos = center.y + (paint.textSize / 3f) // vertical correction
                
                val displayText = if (slice.name.length > 12) slice.name.take(10) + ".." else slice.name
                canvas.nativeCanvas.drawText(displayText, xPos, yPos, paint)
                
                canvas.nativeCanvas.restore()
            }
        }

        // 3. Draw outer dual-layered wheel border rim
        // Outer dark purple-gray rim
        drawCircle(
            color = outlineColor,
            radius = radius + strokeWidth / 2f,
            style = Stroke(width = strokeWidth)
        )
        // Inner absolute dark charcoal rim
        drawCircle(
            color = backgroundColor,
            radius = radius,
            style = Stroke(width = strokeWidth / 2f)
        )
    }
}

@Composable
fun WheelPointer(modifier: Modifier = Modifier) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val outlineColor = MaterialTheme.colorScheme.outline
    Canvas(modifier = modifier.size(24.dp, 32.dp)) {
        val path = Path().apply {
            moveTo(size.width / 2f, size.height) // pointer tip pointing down
            lineTo(0f, 0f) // top-left corner
            lineTo(size.width, 0f) // top-right corner
            close()
        }
        
        // Draw physical pointer shadow
        drawPath(
            path = path,
            color = Color.Black.copy(alpha = 0.3f)
        )
        // Elegant Lavender pointer matching the theme primary
        drawPath(
            path = path,
            color = primaryColor
        )
        // Subtly blended outline border
        drawPath(
            path = path,
            color = outlineColor,
            style = Stroke(width = 1.5.dp.toPx())
        )
    }
}

@Composable
fun SliceItemRow(
    index: Int,
    slice: WheelSlice,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Color circular tag
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .background(
                            color = safeParseColor(slice.colorHex),
                            shape = CircleShape
                        )
                        .border(1.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = slice.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Row {
                IconButton(onClick = onEdit) {
                    Icon(
                        Icons.Default.Palette,
                        contentDescription = "Edit Slice Color/Name",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onDelete) {
                    Icon(
                        Icons.Default.Delete,
                        contentDescription = "Delete Slice",
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SliceEditDialog(
    title: String,
    initialName: String,
    initialColorHex: String,
    onDismiss: () -> Unit,
    onConfirm: (name: String, colorHex: String) -> Unit
) {
    var nameInput by remember { mutableStateOf(initialName) }
    var selectedColorHex by remember { mutableStateOf(initialColorHex) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Slice Label") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text("Select Slice Color", fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(8.dp))
                
                // Horizontal preset color chips grid
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PresetColors.take(6).forEach { colorHex ->
                        ColorChip(
                            colorHex = colorHex,
                            isSelected = selectedColorHex == colorHex,
                            onSelect = { selectedColorHex = colorHex }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    PresetColors.drop(6).take(6).forEach { colorHex ->
                        ColorChip(
                            colorHex = colorHex,
                            isSelected = selectedColorHex == colorHex,
                            onSelect = { selectedColorHex = colorHex }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nameInput, selectedColorHex) },
                enabled = nameInput.isNotBlank()
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
fun ColorChip(
    colorHex: String,
    isSelected: Boolean,
    onSelect: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .background(
                color = safeParseColor(colorHex),
                shape = CircleShape
            )
            .border(
                width = if (isSelected) 3.dp else 1.dp,
                color = if (isSelected) MaterialTheme.colorScheme.primary else Color.White.copy(alpha = 0.5f),
                shape = CircleShape
            )
            .clip(CircleShape)
            .clickable { onSelect() },
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                Icons.Default.Check,
                contentDescription = "Selected",
                tint = Color.White,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun WinnerDialog(
    winner: WheelSlice,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
                .shadow(16.dp, RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    Icons.Default.Celebration,
                    contentDescription = "Celebration",
                    tint = safeParseColor(winner.colorHex),
                    modifier = Modifier.size(72.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "WINNER!",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                
                // Centered colored winner banner
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = safeParseColor(winner.colorHex)
                    )
                ) {
                    Text(
                        text = winner.name,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp, horizontal = 24.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Awesome!")
                }
            }
        }
    }
}
