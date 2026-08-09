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
import androidx.compose.ui.graphics.Color as ComposeColor
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.*
import androidx.compose.runtime.LaunchedEffect

data class PomodoroSession(
    val id: Int,
    val type: SessionType,
    val duration: Long,
    val completedAt: Long = System.currentTimeMillis()
)

enum class SessionType {
    WORK, SHORT_BREAK, LONG_BREAK;
    
    fun displayName(): String = when (this) {
        WORK -> "Work"
        SHORT_BREAK -> "Short Break"
        LONG_BREAK -> "Long Break"
    }
    
    fun color(): ComposeColor = when (this) {
        WORK -> ComposeColor(0xFFE53935)
        SHORT_BREAK -> ComposeColor(0xFF43A047)
        LONG_BREAK -> ComposeColor(0xFF1E88E5)
    }
}

@Composable
fun PomodoroTimerScreen(navHostController: NavHostController) {
    // Settings
    var workDuration by remember { mutableIntStateOf(25) } // minutes
    var shortBreakDuration by remember { mutableIntStateOf(5) } // minutes
    var longBreakDuration by remember { mutableIntStateOf(15) } // minutes
    var sessionsBeforeLongBreak by remember { mutableIntStateOf(4) }
    var autoStartBreaks by remember { mutableStateOf(false) }
    var autoStartWork by remember { mutableStateOf(false) }
    
    // State
    var currentSessionType by remember { mutableStateOf(SessionType.WORK) }
    var currentSessionNumber by remember { mutableIntStateOf(1) }
    var isRunning by remember { mutableStateOf(false) }
    var remainingTime by remember { mutableLongStateOf(25 * 60 * 1000L) }
    var completedSessions by remember { mutableStateOf(listOf<PomodoroSession>()) }
    var showSettings by remember { mutableStateOf(false) }
    var showHistory by remember { mutableStateOf(false) }
    
    // Calculate total duration for current session type
    val currentTotalDuration = when (currentSessionType) {
        SessionType.WORK -> workDuration * 60 * 1000L
        SessionType.SHORT_BREAK -> shortBreakDuration * 60 * 1000L
        SessionType.LONG_BREAK -> longBreakDuration * 60 * 1000L
    }
    
    // Initialize remaining time when session type changes
    LaunchedEffect(currentSessionType) {
        if (!isRunning) {
            remainingTime = currentTotalDuration
        }
    }
    
    // Animation for progress ring
    val animatedProgress by animateFloatAsState(
        targetValue = if (currentTotalDuration > 0) 
            remainingTime.toFloat() / currentTotalDuration.toFloat() else 1f,
        animationSpec = tween(300, easing = EaseOutCubic),
        label = "progress"
    )
    
    // Pulse animation for break indicator
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isRunning && currentSessionType != SessionType.WORK) 1f else 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
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
                    isRunning = false
                    
                    // Record completed session
                    completedSessions = completedSessions + PomodoroSession(
                        id = completedSessions.size + 1,
                        type = currentSessionType,
                        duration = currentTotalDuration
                    )
                    
                    // Determine next session
                    when (currentSessionType) {
                        SessionType.WORK -> {
                            if (currentSessionNumber % sessionsBeforeLongBreak == 0) {
                                currentSessionType = SessionType.LONG_BREAK
                            } else {
                                currentSessionType = SessionType.SHORT_BREAK
                            }
                            if (autoStartBreaks) {
                                remainingTime = when (currentSessionType) {
                                    SessionType.LONG_BREAK -> longBreakDuration * 60 * 1000L
                                    else -> shortBreakDuration * 60 * 1000L
                                }
                                isRunning = true
                            }
                        }
                        SessionType.SHORT_BREAK, SessionType.LONG_BREAK -> {
                            currentSessionNumber++
                            currentSessionType = SessionType.WORK
                            remainingTime = workDuration * 60 * 1000L
                            if (autoStartWork) {
                                isRunning = true
                            }
                        }
                    }
                }
            }
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolHeader(
            title = "Pomodoro Timer",
            subtitle = "Stay focused with the Pomodoro technique",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Session tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChip(
                selected = currentSessionType == SessionType.WORK,
                onClick = { 
                    if (!isRunning) {
                        currentSessionType = SessionType.WORK
                        remainingTime = workDuration * 60 * 1000L
                    }
                },
                label = { Text("Focus") },
                leadingIcon = if (currentSessionType == SessionType.WORK) {
                    { Icon(Icons.Default.Work, null, Modifier.size(16.dp)) }
                } else null
            )
            
            FilterChip(
                selected = currentSessionType == SessionType.SHORT_BREAK,
                onClick = { 
                    if (!isRunning) {
                        currentSessionType = SessionType.SHORT_BREAK
                        remainingTime = shortBreakDuration * 60 * 1000L
                    }
                },
                label = { Text("Short Break") },
                leadingIcon = if (currentSessionType == SessionType.SHORT_BREAK) {
                    { Icon(Icons.Default.Coffee, null, Modifier.size(16.dp)) }
                } else null
            )
            
            FilterChip(
                selected = currentSessionType == SessionType.LONG_BREAK,
                onClick = { 
                    if (!isRunning) {
                        currentSessionType = SessionType.LONG_BREAK
                        remainingTime = longBreakDuration * 60 * 1000L
                    }
                },
                label = { Text("Long Break") },
                leadingIcon = if (currentSessionType == SessionType.LONG_BREAK) {
                    { Icon(Icons.Default.BeachAccess, null, Modifier.size(16.dp)) }
                } else null
            )
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main timer display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(260.dp)
                .padding(horizontal = 32.dp),
            contentAlignment = Alignment.Center
        ) {
            // Progress ring
                val _surfacevariantColor = MaterialTheme.colorScheme.surfaceVariant
            Canvas(modifier = Modifier.size(220.dp)) {
                val strokeWidth = 10.dp.toPx()
                val diameter = size.minDimension - strokeWidth
                
                // Background arc
                drawArc(
                    color = _surfacevariantColor,
                    startAngle = -90f,
                    sweepAngle = 360f,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2),
                    size = androidx.compose.ui.geometry.Size(diameter, diameter)
                )
                
                // Progress arc
                val sweepAngle = -360f * animatedProgress
                drawArc(
                    color = currentSessionType.color(),
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset((size.width - diameter) / 2, (size.height - diameter) / 2),
                    size = androidx.compose.ui.geometry.Size(diameter, diameter)
                )
            }
            
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Session type label
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = currentSessionType.color().copy(alpha = pulseAlpha)
                ) {
                    Text(
                        text = currentSessionType.displayName(),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = ComposeColor.White,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // Time display
                Text(
                    text = formatPomodoroTime(remainingTime),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 2.sp
                )
                
                Spacer(modifier = Modifier.height(4.dp))
                
                // Session counter
                Text(
                    text = "Session $currentSessionNumber of $sessionsBeforeLongBreak",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Control buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = {
                    isRunning = false
                    remainingTime = currentTotalDuration
                },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Reset")
            }
            
            Button(
                onClick = { isRunning = !isRunning },
                modifier = Modifier
                    .weight(1.5f)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = currentSessionType.color(),
                    contentColor = ComposeColor.White
                )
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isRunning) "Pause" else "Start Focus",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Visual Pomodoro indicators (tomatoes)
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
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Today's Progress",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Tomato grid
                val todaySessions = completedSessions.count { it.type == SessionType.WORK }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    repeat(sessionsBeforeLongBreak) { index ->
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    if (index < todaySessions % sessionsBeforeLongBreak || 
                                        (todaySessions > 0 && index < (todaySessions - 1) % sessionsBeforeLongBreak + 1)) {
                                        if ((index + 1) == sessionsBeforeLongBreak && todaySessions >= sessionsBeforeLongBreak) {
                                            ComposeColor(0xFFFF9800) // Gold for completed set
                                        } else {
                                            ComposeColor(0xFFE53935) // Red for tomato
                                        }
                                    } else if (index < currentSessionNumber - 1 || 
                                              (currentSessionType != SessionType.WORK && index < currentSessionNumber - 1 + 1)) {
                                        ComposeColor(0xFFE53935)
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerLowest
                                    }
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (index < todaySessions || 
                               (currentSessionType != SessionType.WORK && index < currentSessionNumber - 1)) {
                                Icon(
                                    imageVector = Icons.Default.Eco,
                                    contentDescription = null,
                                    tint = ComposeColor.White,
                                    modifier = Modifier.size(18.dp)
                                )
                            } else {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    
                    // Stats
                    Column(horizontalAlignment = Alignment.End) {
                        Text(
                            text = "$todaySessions pomodoros",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "${todaySessions * workDuration} min focused",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Settings and History buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { showSettings = !showSettings },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Settings, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Settings")
            }
            
            OutlinedButton(
                onClick = { showHistory = !showHistory },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.History, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("History (${completedSessions.size})")
            }
        }
        
        // Settings panel
        if (showSettings) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Timer Settings",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Work duration
                    LabeledSlider(
                        label = "Focus Duration",
                        value = workDuration,
                        onValueChange = { 
                            workDuration = it
                            if (currentSessionType == SessionType.WORK && !isRunning) {
                                remainingTime = it * 60 * 1000L
                            }
                        },
                        valueRange = 1..60,
                        unit = "min"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Short break duration
                    LabeledSlider(
                        label = "Short Break",
                        value = shortBreakDuration,
                        onValueChange = { 
                            shortBreakDuration = it
                            if (currentSessionType == SessionType.SHORT_BREAK && !isRunning) {
                                remainingTime = it * 60 * 1000L
                            }
                        },
                        valueRange = 1..30,
                        unit = "min"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Long break duration
                    LabeledSlider(
                        label = "Long Break",
                        value = longBreakDuration,
                        onValueChange = { 
                            longBreakDuration = it
                            if (currentSessionType == SessionType.LONG_BREAK && !isRunning) {
                                remainingTime = it * 60 * 1000L
                            }
                        },
                        valueRange = 5..60,
                        unit = "min"
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    // Sessions before long break
                    LabeledSlider(
                        label = "Sessions Before Long Break",
                        value = sessionsBeforeLongBreak,
                        onValueChange = { sessionsBeforeLongBreak = it },
                        valueRange = 2..8,
                        unit = ""
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Auto-start toggles
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Auto-start breaks", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Automatically start break after focus",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = autoStartBreaks, onCheckedChange = { autoStartBreaks = it })
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(text = "Auto-start focus", style = MaterialTheme.typography.bodyMedium)
                            Text(
                                text = "Automatically start focus after break",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(checked = autoStartWork, onCheckedChange = { autoStartWork = it })
                    }
                }
            }
        }
        
        // History panel
        if (showHistory && completedSessions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        text = "Session History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(completedSessions.reversed()) { session ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .clip(CircleShape)
                                        .background(session.type.color())
                                )
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = session.type.displayName(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Text(
                                    text = formatPomodoroTime(session.duration),
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun LabeledSlider(
    label: String,
    value: Int,
    onValueChange: (Int) -> Unit,
    valueRange: IntRange,
    unit: String
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = label, style = MaterialTheme.typography.bodyMedium)
            Text(
                text = "$value $unit",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }
        
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = valueRange.first.toFloat()..valueRange.last.toFloat(),
            steps = valueRange.last - valueRange.first - 1
        )
    }
}

private fun formatPomodoroTime(timeMs: Long): String {
    val minutes = TimeUnit.MILLISECONDS.toMinutes(timeMs)
    val seconds = TimeUnit.MILLISECONDS.toSeconds(timeMs) % 60
    return String.format("%02d:%02d", minutes, seconds)
}
