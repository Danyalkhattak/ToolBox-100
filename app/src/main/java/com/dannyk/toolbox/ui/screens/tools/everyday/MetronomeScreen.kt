package com.dannyk.toolbox.ui.screens.tools.everyday

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import kotlinx.coroutines.delay
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect

data class TimeSignature(
    val beats: Int,
    val noteValue: Int,
    val displayName: String
)

val timeSignatures = listOf(
    TimeSignature(2, 4, "2/4"),
    TimeSignature(3, 4, "3/4"),
    TimeSignature(4, 4, "4/4"),
    TimeSignature(6, 8, "6/8")
)

@Composable
fun MetronomeScreen(navHostController: NavHostController) {
    var bpm by remember { mutableIntStateOf(120) }
    var isPlaying by remember { mutableStateOf(false) }
    var currentBeat by remember { mutableIntStateOf(0) }
    var selectedTimeSignature by remember { mutableStateOf(timeSignatures[2]) } // Default 4/4
    
    // Tap tempo state
    val tapTimes = remember { mutableListOf<Long>() }
    
    // Animation for beat indicator
    val beatScale by animateFloatAsState(
        targetValue = if (isPlaying && currentBeat == 0) 1.3f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "beatScale"
    )
    
    // Sub-beat animation (for non-first beats)
    val subBeatScale by animateFloatAsState(
        targetValue = if (isPlaying && currentBeat > 0) 1.15f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh
        ),
        label = "subBeatScale"
    )
    
    // Flash animation for visual feedback
    var flashAlpha by remember { mutableFloatStateOf(0f) }
    
    LaunchedEffect(flashAlpha) {
        if (flashAlpha > 0f) {
            delay(100)
            flashAlpha = 0f
        }
    }
    
    // Metronome coroutine
    LaunchedEffect(isPlaying, bpm, selectedTimeSignature) {
        if (isPlaying) {
            currentBeat = 0
            while (isPlaying) {
                flashAlpha = 1f
                
                // Calculate delay based on BPM
                val intervalMs = (60000L / bpm).toLong()
                
                // Move to next beat
                delay(intervalMs)
                
                currentBeat = (currentBeat + 1) % selectedTimeSignature.beats
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
            title = "Metronome",
            subtitle = "Keep steady tempo with visual and audio cues",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Main beat display
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp),
            contentAlignment = Alignment.Center
        ) {
            // Background circle for flash effect
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = flashAlpha * 0.3f))
            )
            
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                // BPM Display
                Text(
                    text = "$bpm",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = -2.sp
                )
                
                Text(
                    text = "BPM",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                // Beat indicators
                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    repeat(selectedTimeSignature.beats) { index ->
                        val isFirstBeat = index == 0
                        val isActiveBeat = index == currentBeat
                        
                        val scale = if (isActiveBeat) {
                            if (isFirstBeat) beatScale else subBeatScale
                        } else 1f
                        
                        val color = when {
                            !isPlaying -> MaterialTheme.colorScheme.surfaceContainerLowest
                            isActiveBeat && isFirstBeat -> MaterialTheme.colorScheme.primary
                            isActiveBeat -> MaterialTheme.colorScheme.secondary
                            else -> MaterialTheme.colorScheme.surfaceContainerLowest
                        }
                        
                        Box(
                            modifier = Modifier
                                .size(if (isFirstBeat) 48.dp else 40.dp)
                                .scale(scale)
                                .clip(CircleShape)
                                .background(color),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!isPlaying || isActiveBeat) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isActiveBeat) 
                                        MaterialTheme.colorScheme.onPrimary 
                                    else 
                                        MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // BPM Control
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = { 
                            if (bpm > 20) bpm -= 5
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Icon(Icons.Default.Remove, contentDescription = "Decrease BPM")
                    }
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Slider(
                            value = bpm.toFloat(),
                            onValueChange = { bpm = it.toInt() },
                            valueRange = 20f..240f,
                            modifier = Modifier.width(200.dp)
                        )
                        Text(
                            text = "${getBPMDescription(bpm)}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    IconButton(
                        onClick = { 
                            if (bpm < 240) bpm += 5
                        },
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceContainer)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = "Increase BPM")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Tap Tempo & Play controls
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Tap Tempo button
            OutlinedButton(
                onClick = {
                    val now = System.currentTimeMillis()
                    tapTimes.add(now)
                    
                    // Keep only last 4 taps
                    while (tapTimes.size > 4) {
                        tapTimes.removeAt(0)
                    }
                    
                    // Calculate average interval if we have at least 2 taps
                    if (tapTimes.size >= 2) {
                        var totalInterval = 0L
                        for (i in 1 until tapTimes.size) {
                            totalInterval += tapTimes[i] - tapTimes[i - 1]
                        }
                        val avgInterval = totalInterval / (tapTimes.size - 1)
                        val calculatedBpm = (60000 / avgInterval).toInt().coerceIn(20, 240)
                        bpm = calculatedBpm
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(Icons.Default.TouchApp, null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Tap Tempo")
            }
            
            // Play/Pause button
            Button(
                onClick = { isPlaying = !isPlaying },
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isPlaying) 
                        MaterialTheme.colorScheme.errorContainer else 
                        MaterialTheme.colorScheme.primary,
                    contentColor = if (isPlaying) 
                        MaterialTheme.colorScheme.onErrorContainer else 
                        MaterialTheme.colorScheme.onPrimary
                )
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Stop else Icons.Default.PlayArrow,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isPlaying) "Stop" else "Start",
                    fontWeight = FontWeight.Bold
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Time Signature selection
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
                    text = "Time Signature",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Beat pattern visualization
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    timeSignatures.forEach { signature ->
                        val isSelected = selectedTimeSignature == signature
                        
                        Surface(
                            onClick = { 
                                selectedTimeSignature = signature
                                currentBeat = 0
                            },
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) 
                                MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else 
                                Color.Transparent
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                // Visual representation of the pattern
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    repeat(signature.beats) { index ->
                                        Box(
                                            modifier = Modifier
                                                .size(16.dp)
                                                .clip(CircleShape)
                                                .background(
                                                    if (isSelected) 
                                                        if (index == 0) MaterialTheme.colorScheme.primary
                                                        else MaterialTheme.colorScheme.secondary
                                                    else 
                                                        MaterialTheme.colorScheme.surfaceContainerLowest
                                                )
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.height(8.dp))
                                
                                Text(
                                    text = signature.displayName,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) 
                                        MaterialTheme.colorScheme.primary else 
                                        MaterialTheme.colorScheme.onSurface
                                )
                                
                                Text(
                                    text = "${signature.beats} beats",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick BPM presets
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(listOf(
                "Largo" to 40,
                "Adagio" to 66,
                "Andante" to 76,
                "Moderato" to 108,
                "Allegro" to 120,
                "Vivace" to 168,
                "Presto" to 180
            )) { (name, presetBpm) ->
                FilterChip(
                    selected = bpm == presetBpm,
                    onClick = { bpm = presetBpm },
                    label = { 
                        Column {
                            Text(name, fontSize = 11.sp)
                            Text("$presetBpm", fontSize = 10.sp)
                        }
                    }
                )
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Info card
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
                    imageVector = Icons.Default.MusicNote,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "Metronome Info",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Text(
                        text = "Beat interval: ${(60000f / bpm).toInt()}ms • ${selectedTimeSignature.displayName} time",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.8f)
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
    }
}

private fun getBPMDescription(bpm: Int): String = when (bpm) {
    in 20..40 -> "Grave/Largo (very slow)"
    in 41..55 -> "Largo (very slow)"
    in 56..65 -> "Adagio (slow)"
    in 66..76 -> "Andante (walking pace)"
    in 77..97 -> "Moderato (moderate)"
    in 98..109 -> "Allegretto (moderately fast)"
    in 110..168 -> "Allegro (fast)"
    in 169..200 -> "Vivace/Presto (very fast)"
    else -> "Prestissimo (extremely fast)"
}
