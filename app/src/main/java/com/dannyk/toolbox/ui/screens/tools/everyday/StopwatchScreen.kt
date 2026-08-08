package com.dannyk.toolbox.ui.screens.tools.everyday

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.geometry.Size
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import kotlin.math.*

@Composable
fun StopwatchScreen(navHostController: NavHostController) {
    var isRunning by remember { mutableStateOf(false) }
    var elapsedTime by remember { mutableLongStateOf(0L) }
    var laps by remember { mutableStateOf(listOf<Long>()) }
    
    // Track start time for accurate timing
    var startTime by remember { mutableLongStateOf(0L) }
    var accumulatedTime by remember { mutableLongStateOf(0L) }
    
    // Animation for progress ring
    val animatedProgress by animateFloatAsState(
        targetValue = if (isRunning) 1f else 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "progress"
    )
    
    // Timer coroutine
    LaunchedEffect(isRunning) {
        if (isRunning) {
            startTime = System.currentTimeMillis() - accumulatedTime
            while (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                delay(10L) // Update every 10ms for smooth display
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolHeader(
            title = "Stopwatch",
            subtitle = "Precision timing with lap recording",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main time display with circular progress
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Progress ring background
            Canvas(modifier = Modifier.size(240.dp)) {
                drawArc(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                )
                
                // Animated progress arc when running
                if (isRunning) {
                    drawArc(
                        color = MaterialTheme.colorScheme.primary,
                        startAngle = -90f,
                        sweepAngle = 360f * animatedProgress,
                        useCenter = false,
                        style = Stroke(width = 8.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }
            
            // Time display
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = formatStopwatchTime(elapsedTime),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                Text(
                    text = formatMilliseconds(elapsedTime),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 20.sp,
                    color = MaterialTheme.colorScheme.primary,
                    letterSpacing = 1.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Lap / Reset button
            OutlinedButton(
                onClick = {
                    if (isRunning) {
                        // Record lap
                        laps = laps + elapsedTime
                    } else {
                        // Reset everything
                        elapsedTime = 0L
                        accumulatedTime = 0L
                        laps = emptyList()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Icon(
                    imageVector = if (isRunning && elapsedTime > 0) Icons.Default.Replay else Icons.Default.Refresh,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "Lap" else "Reset",
                    fontWeight = FontWeight.SemiBold
                )
            }
            
            // Start / Pause button
            Button(
                onClick = {
                    if (isRunning) {
                        isRunning = false
                        accumulatedTime = elapsedTime
                    } else {
                        isRunning = true
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isRunning) 
                        MaterialTheme.colorScheme.errorContainer else 
                        MaterialTheme.colorScheme.primary,
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
                    text = if (isRunning) "Pause" else "Start",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Laps list
        if (laps.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Lap",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = "Split Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(2f)
                        )
                        Text(
                            text = "Total Time",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.weight(2f),
                            textAlign = androidx.compose.ui.text.style.TextAlign.End
                        )
                    }
                    
                    Divider(modifier = Modifier.fillMaxWidth())
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(laps.reversed().indexed()) { (index, lapTime) ->
                            val lapNumber = laps.size - index
                            val prevLapTime = if (index < laps.reversed().size - 1) {
                                laps.reversed()[index + 1]
                            } else 0L
                            
                            val splitTime = lapTime - prevLapTime
                            
                            // Find best and worst laps for highlighting
                            val splitTimes = calculateSplitTimes(laps)
                            val bestLapIndex = splitTimes.indexOf(splitTimes.minOrNull())
                            val worstLapIndex = splitTimes.indexOf(splitTimes.maxOrNull())
                            val currentSplitIndex = laps.size - lapNumber
                            
                            val isBestLap = currentSplitIndex == bestLapIndex && laps.size > 1
                            val isWorstLap = currentSplitIndex == worstLapIndex && laps.size > 1
                            
                            val backgroundColor = when {
                                isBestLap -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                isWorstLap -> MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f)
                                else -> Color.Transparent
                            }
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(backgroundColor)
                                    .padding(vertical = 10.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Lap number with indicator
                                Row(
                                    modifier = Modifier.weight(1f),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(
                                                when {
                                                    isBestLap -> MaterialTheme.colorScheme.primary
                                                    isWorstLap -> MaterialTheme.colorScheme.error
                                                    else -> MaterialTheme.colorScheme.surfaceContainerest
                                                }
                                            ),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text(
                                            text = "$lapNumber",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = when {
                                                isBestLap -> MaterialTheme.colorScheme.onPrimary
                                                isWorstLap -> MaterialTheme.colorScheme.onError
                                                else -> MaterialTheme.colorScheme.onSurface
                                            }
                                        )
                                    }
                                }
                                
                                // Split time
                                Text(
                                    text = formatLapTime(splitTime),
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isBestLap || isWorstLap) FontWeight.Bold else FontWeight.Normal,
                                    color = when {
                                        isBestLap -> MaterialTheme.colorScheme.primary
                                        isWorstLap -> MaterialTheme.colorScheme.error
                                        else -> MaterialTheme.colorScheme.onSurface
                                    },
                                    modifier = Modifier.weight(2f)
                                )
                                
                                // Total time
                                Text(
                                    text = formatLapTime(lapTime),
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.End,
                                    modifier = Modifier.weight(2f)
                                )
                            }
                            
                            if (lapNumber > 1) {
                                Divider(modifier = Modifier.fillMaxWidth(), 
                                    modifier = Modifier.padding(horizontal = 8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        // Stats at bottom
        if (laps.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${laps.size}",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Laps",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val avgLap = if (laps.isNotEmpty()) elapsedTime / laps.size else 0L
                        Text(
                            text = formatLapTime(avgLap),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        Text(
                            text = "Avg Lap",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                
                Card(
                    modifier = Modifier.weight(1f),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                    )
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        val splitTimes = calculateSplitTimes(laps)
                        val fastest = splitTimes.minOrNull() ?: 0L
                        Text(
                            text = formatLapTime(fastest),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Text(
                            text = "Fastest",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun formatStopwatchTime(timeMs: Long): String {
    val hours = TimeUnit.MILLISECONDS.toHours(timeMs)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs) % 60
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) % 60
    
    return if (hours > 0) {
        String.format("%02d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}

private fun formatMilliseconds(timeMs: Long): String {
    val millis = timeMs % 1000
    return String.format(".%03d", millis)
}

private fun formatLapTime(timeMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) % 60
    val millis = timeMs % 1000
    
    return if (minutes > 0) {
        String.format("%d:%02d.%03d", minutes, seconds, millis)
    } else {
        String.format("%d.%03d", seconds, millis)
    }
}

private fun calculateSplitTimes(laps: List<Long>): List<Long> {
    val splitTimes = mutableListOf<Long>()
    var prevTime = 0L
    for (lap in laps) {
        splitTimes.add(lap - prevTime)
        prevTime = lap
    }
    return splitTimes
}

private fun <T> List<T>.indexed(): List<IndexedValue<T>> {
    return mapIndexed { index, t -> IndexedValue(index, t) }
}
