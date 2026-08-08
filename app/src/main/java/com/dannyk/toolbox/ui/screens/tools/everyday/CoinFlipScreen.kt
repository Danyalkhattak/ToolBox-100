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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import kotlinx.coroutines.delay
import kotlin.random.Random
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.graphicsLayer

enum class CoinSide(val displayName: String, val emoji: String) {
    HEADS("Heads", "H"),
    TAILS("Tails", "T")
}

data class FlipResult(
    val id: Int,
    val side: CoinSide,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun CoinFlipScreen(navHostController: NavHostController) {
    var isFlipping by remember { mutableStateOf(false) }
    var currentResult by remember { mutableStateOf<CoinSide?>(null) }
    var flipRotation by remember { mutableFloatStateOf(0f) }
    
    // Statistics
    var headsCount by remember { mutableIntStateOf(0) }
    var tailsCount by remember { mutableIntStateOf(0) }
    var currentStreak by remember { mutableIntStateOf(0) }
    var streakType by remember { mutableStateOf<CoinSide?>(null) }
    var bestHeadsStreak by remember { mutableIntStateOf(0) }
    var bestTailsStreak by remember { mutableIntStateOf(0) }
    
    // History
    var flipHistory by remember { mutableStateOf(listOf<FlipResult>()) }
    
    // Animation for coin flip
    val rotationAnimation by animateFloatAsState(
        targetValue = if (isFlipping) 180f * 10 else 0f, // 10 full rotations
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        ),
        label = "coinRotation"
    )
    
    // Scale animation for bounce effect
    val scaleAnimation by animateFloatAsState(
        targetValue = if (isFlipping) 1.1f else 1f,
        animationSpec = keyframes {
            durationMillis = 1500
            0.0f at 0 using LinearOutSlowInEasing
            1.15f at 300 using FastOutSlowInEasing
            1.05f at 600 using FastOutSlowInEasing
            1.1f at 900 using FastOutSlowInEasing
            1.0f at 1500 using FastOutSlowInEasing
        },
        label = "coinScale"
    )
    
    // Handle flip action
    suspend fun performFlip() {
        isFlipping = true
        
        // Determine result after animation starts
        delay(750) // Halfway through animation
        
        val result = if (Random.nextBoolean()) CoinSide.HEADS else CoinSide.TAILS
        currentResult = result
        
        delay(750) // Complete animation
        
        isFlipping = false
        
        // Update statistics
        when (result) {
            CoinSide.HEADS -> headsCount++
            CoinSide.TAILS -> tailsCount++
        }
        
        // Update streak
        if (result == streakType) {
            currentStreak++
        } else {
            streakType = result
            currentStreak = 1
        }
        
        // Update best streaks
        when (result) {
            CoinSide.HEADS -> if (currentStreak > bestHeadsStreak) bestHeadsStreak = currentStreak
            CoinSide.TAILS -> if (currentStreak > bestTailsStreak) bestTailsStreak = currentStreak
        }
        
        // Add to history
        flipHistory = flipHistory + FlipResult(
            id = flipHistory.size + 1,
            side = result
        )
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ToolHeader(
            title = "Coin Flip",
            subtitle = "Let chance decide with a virtual coin toss",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Coin display area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            contentAlignment = Alignment.Center
        ) {
            // Outer glow effect when flipping or showing result
            if (isFlipping || currentResult != null) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = if (isFlipping) 0.2f else 0.1f
                            )
                        )
                )
            }
            
            // Coin
            Box(
                modifier = Modifier
                    .size(160.dp * scaleAnimation)
                    .graphicsLayer {
                        // Apply 3D-like rotation effect
                        scaleX = kotlin.math.abs(kotlin.math.cos(kotlin.math.toRadians(rotationAnimation.toDouble()))).toFloat()
                    }
                    .clip(CircleSize())
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.minDimension / 2 - 8.dp.toPx()
                    
                    // Coin edge (3D effect)
                    drawCircle(
                        color = Color(0xFFFFB300),
                        center = Offset(centerX, centerY),
                        radius = radius + 4.dp.toPx(),
                        style = Stroke(width = 6.dp.toPx())
                    )
                    
                    // Main coin face
                    val faceColor = if (!isFlipping && currentResult == CoinSide.HEADS) {
                        Color(0xFFFFD54F) // Gold for heads
                    } else if (!isFlipping && currentResult == CoinSide.TAILS) {
                        Color(0xFFE0E0E0) // Silver for tails
                    } else {
                        Color(0xFFFFC107) // Default gold while flipping
                    }
                    
                    drawCircle(
                        color = faceColor,
                        center = Offset(centerX, centerY),
                        radius = radius
                    )
                    
                    // Inner circle detail
                    drawCircle(
                        color = faceColor.copy(alpha = 0.5f),
                        center = Offset(centerX, centerY),
                        radius = radius - 12.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
                
                // Text on coin
                Box(contentAlignment = Alignment.Center) {
                    if (isFlipping) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(60.dp),
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            strokeWidth = 3.dp
                        )
                    } else if (currentResult != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentResult!!.emoji,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Bold,
                                fontSize = 56.sp,
                                color = if (currentResult == CoinSide.HEADS) 
                                    Color(0xFF5D4037) else Color(0xFF424242)
                            )
                            Text(
                                text = currentResult!!.displayName.toUpperCase(),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (currentResult == CoinSide.HEADS) 
                                    Color(0xFF5D4037) else Color(0xFF424242)
                            )
                        }
                    } else {
                        Icon(
                            imageVector = Icons.Default.HelpOutline,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Flip button
        Button(
            onClick = {
                if (!isFlipping) {
                    // Launch coroutine for flip animation
                    kotlinx.coroutines.GlobalScope.launch {
                        performFlip()
                    }
                }
            },
            enabled = !isFlipping,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = if (isFlipping) Icons.Default.HourglassTop else Icons.Default.Casino,
                contentDescription = null,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isFlipping) "Flipping..." else "FLIP COIN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Statistics cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Heads stats
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFFF8E1) // Light amber
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFFFD54F)
                    ) {
                        Text(
                            text = "H",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF5D4037)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$headsCount",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5D4037)
                    )
                    Text(
                        text = "${if (headsCount + tailsCount > 0) headsCount * 100 / (headsCount + tailsCount) else 0}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF8D6E63)
                    )
                }
            }
            
            // Tails stats
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFFFAFAFA) // Light gray
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Surface(
                        shape = CircleShape,
                        color = Color(0xFFE0E0E0)
                    ) {
                        Text(
                            text = "T",
                            modifier = Modifier.padding(8.dp),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF424242)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$tailsCount",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF424242)
                    )
                    Text(
                        text = "${if (headsCount + tailsCount > 0) tailsCount * 100 / (headsCount + tailsCount) else 0}%",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF757575)
                    )
                }
            }
            
            // Streak info
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "$currentStreak",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = streakType?.displayName ?: "-",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Best streaks and total flips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Flips",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${headsCount + tailsCount}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Best Streaks",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "H:$bestHeadsStreak T:$bestTailsStreak",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Reset button
        OutlinedButton(
            onClick = {
                headsCount = 0
                tailsCount = 0
                currentStreak = 0
                streakType = null
                bestHeadsStreak = 0
                bestTailsStreak = 0
                currentResult = null
                flipHistory = emptyList()
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(Icons.Default.DeleteSweep, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset Statistics")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Recent history
        if (flipHistory.isNotEmpty()) {
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
                        Text(
                            text = "Recent Flips",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = "Last ${flipHistory.size.coerceAtMost(20)} of ${flipHistory.size}",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 150.dp)
                    ) {
                        items(flipHistory.reversed().take(20).reversed()) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = if (result.side == CoinSide.HEADS) 
                                        Color(0xFFFFD54F) else Color(0xFFE0E0E0)
                                ) {
                                    Text(
                                        text = result.side.emoji,
                                        modifier = Modifier.padding(6.dp),
                                        style = MaterialTheme.typography.labelLarge,
                                        fontWeight = FontWeight.Bold,
                                        color = if (result.side == CoinSide.HEADS) 
                                            Color(0xFF5D4037) else Color(0xFF424242)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = "#${result.id}: ${result.side.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Text(
                                    text = formatTimestamp(result.timestamp),
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
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 1000 -> "Just now"
        diff < 60_000 -> "${diff / 1000}s ago"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        else -> "${diff / 3600_000}h ago"
    }
}

// Custom circular clip shape
private fun CircleSize() = CircleShape
