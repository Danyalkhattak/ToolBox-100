package com.dannyk.toolbox.ui.screens.tools.everyday

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import kotlinx.coroutines.delay
import java.util.concurrent.TimeUnit

@Composable
fun CountdownTimerScreen(navHostController: NavHostController) {
    var totalDuration by remember { mutableLongStateOf(5 * 60 * 1000L) } // Default 5 minutes
    var remainingTime by remember { mutableLongStateOf(5 * 60 * 1000L) }
    var isRunning by remember { mutableStateOf(false) }
    var isCompleted by remember { mutableStateOf(false) }
    var countUpPastZero by remember { mutableStateOf(false) }
    var overTime by remember { mutableLongStateOf(0L) }
    
    // Input mode for setting time
    var isInputMode by remember { mutableStateOf(true) }
    
    // Time input values (in minutes and seconds)
    var inputMinutes by remember { mutableIntStateOf(5) }
    var inputSeconds by remember { mutableIntStateOf(0) }
    
    // Preset durations in milliseconds
    val presets = listOf(
        "1 min" to 1 * 60 * 1000L,
        "5 min" to 5 * 60 * 1000L,
        "10 min" to 10 * 60 * 1000L,
        "15 min" to 15 * 60 * 1000L,
        "30 min" to 30 * 60 * 1000L,
        "1 hr" to 60 * 60 * 1000L
    )
    
    // Animation for progress ring
    val animatedProgress = animateFloatAsState(
        targetValue = if (!isInputMode && totalDuration > 0) {
            if (remainingTime > 0 || !countUpPastZero) {
                remainingTime.toFloat() / totalDuration.toFloat()
            } else 1f
        } else 1f,
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "progress"
    )
    
    // Pulse animation when completed
    val pulseScale by animateFloatAsState(
        targetValue = if (isCompleted && isRunning) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )
    
    // Timer coroutine
    LaunchedEffect(isRunning) {
        while (isRunning) {
            delay(50L)
            if (remainingTime > 0) {
                remainingTime -= 50L
                if (remainingTime <= 0) {
                    remainingTime = 0
                    isCompleted = true
                    if (countUpPastZero) {
                        // Continue counting up
                        overTime += 50L
                    } else {
                        isRunning = false
                    }
                }
            } else if (countUpPastZero) {
                overTime += 50L
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ToolHeader(
            title = "Countdown Timer",
            subtitle = "Set a duration and start counting down",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main timer display with progress ring
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Progress ring
            Canvas(modifier = Modifier.size(if (isCompleted) 240.dp * pulseScale else 240.dp)) {
                val strokeWidth = 12.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                val radius = diameter / 2
                val topLeft = Offset(
                    (size.width - diameter) / 2,
                    (size.height - diameter) / 2
                )
                
                // Background arc
                drawArc(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = topLeft,
                    size = Size(diameter, diameter)
                )
                
                // Progress arc
                if (!isInputMode) {
                    val sweepAngle = -360f * animatedProgress.value
                    val progressColor = when {
                        isCompleted -> MaterialTheme.colorScheme.error
                        remainingTime < totalDuration * 0.1f -> MaterialTheme.colorScheme.error
                        remainingTime < totalDuration * 0.3f -> MaterialTheme.colorScheme.tertiary
                        else -> MaterialTheme.colorScheme.primary
                    }
                    
                    drawArc(
                        color = progressColor,
                        startAngle = -90f,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = Size(diameter, diameter)
                    )
                }
            }
            
            // Time display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (isInputMode) {
                    // Input mode - show time picker
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        // Minutes input
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                IconButton(
                                    onClick = { 
                                        if (inputMinutes < 999) inputMinutes++
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Increase",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Text(
                                    text = String.format("%02d", inputMinutes),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(60.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                
                                IconButton(
                                    onClick = { 
                                        if (inputMinutes > 0) inputMinutes--
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Decrease",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                        
                        Text(
                            text = ":",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 36.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        
                        // Seconds input
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainer
                            )
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                IconButton(
                                    onClick = { 
                                        if (inputSeconds < 59) inputSeconds += 5
                                        else inputSeconds = 59
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowUp,
                                        contentDescription = "Increase",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Text(
                                    text = String.format("%02d", inputSeconds),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 36.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.width(60.dp),
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                                
                                IconButton(
                                    onClick = { 
                                        if (inputSeconds >= 5) inputSeconds -= 5
                                        else inputSeconds = 0
                                    },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = "Decrease",
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                } else {
                    // Display mode - show countdown
                    Text(
                        text = formatCountdownTime(if (remainingTime > 0) remainingTime else 0L),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCompleted) MaterialTheme.colorScheme.error 
                               else MaterialTheme.colorScheme.onSurface,
                        letterSpacing = 2.sp
                    )
                    
                    if (isCompleted && countUpPastZero && overTime > 0) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "+${formatCountdownTime(overTime)}",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 18.sp,
                            color = MaterialTheme.colorScheme.error,
                            letterSpacing = 1.sp
                        )
                    } else if (isCompleted) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "TIME'S UP!",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.error,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Preset buttons
        if (isInputMode) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 16.dp)
            ) {
                items(presets) { (label, duration) ->
                    FilterChip(
                        selected = totalDuration == duration && 
                                   inputMinutes == TimeUnit.MILLISECONDS.toMinutes(duration) &&
                                   inputSeconds == (TimeUnit.MILLISECONDS.toSeconds(duration) % 60).toInt(),
                        onClick = {
                            totalDuration = duration
                            remainingTime = duration
                            inputMinutes = TimeUnit.MILLISECONDS.toMinutes(duration).toInt()
                            inputSeconds = (TimeUnit.MILLISECONDS.toSeconds(duration) % 60).toInt()
                        },
                        label = { Text(label) },
                        leadingIcon = if (totalDuration == duration) {
                            { Icon(Icons.Default.Check, null, Modifier.size(16.dp)) }
                        } else null
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
        
        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (isInputMode) {
                // Set button to confirm time
                Button(
                    onClick = {
                        totalDuration = (inputMinutes * 60 + inputSeconds) * 1000L
                        remainingTime = totalDuration
                        isInputMode = false
                        isCompleted = false
                        overTime = 0L
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Timer, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Set Timer", fontWeight = FontWeight.Bold)
                }
            } else {
                // Reset button
                OutlinedButton(
                    onClick = {
                        isRunning = false
                        isInputMode = true
                        isCompleted = false
                        overTime = 0L
                        remainingTime = totalDuration
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reset")
                }
                
                // Start/Pause button
                Button(
                    onClick = {
                        if (isCompleted) {
                            // Restart
                            remainingTime = totalDuration
                            isCompleted = false
                            overTime = 0L
                        }
                        isRunning = !isRunning
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRunning) 
                            MaterialTheme.colorScheme.errorContainer else 
                            if (isCompleted) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.primary,
                        contentColor = if (isRunning) 
                            MaterialTheme.colorScheme.onErrorContainer else 
                            MaterialTheme.colorScheme.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when {
                            isCompleted -> "Restart"
                            isRunning -> "Pause"
                            else -> "Start"
                        },
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Options
        if (!isInputMode) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Count Up Past Zero",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Continue counting after timer ends",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = countUpPastZero,
                        onCheckedChange = { countUpPastZero = it }
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick info card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
            ),
            shape = RoundedCornerShape(16.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Timer Info",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = if (isInputMode) "Select a preset or set custom time"
                        else "Total: ${formatCountdownTime(totalDuration)} • ${if (isRunning) "Running..." else "Ready"}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatCountdownTime(timeMs: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) % 60
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
