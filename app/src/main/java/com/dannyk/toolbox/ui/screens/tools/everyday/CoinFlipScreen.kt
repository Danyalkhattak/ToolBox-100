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
import kotlinx.coroutines.launch
import kotlin.random.Random
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.foundation.ScrollState
import kotlin.math.*

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
        targetValue = if (isFlipping) 1.15f else 1f,
        animationSpec = keyframes {
            durationMillis = 1500
            0.0f at 0 using LinearOutSlowInEasing
            1.2f at 300 using FastOutSlowInEasing
            1.1f at 600 using FastOutSlowInEasing
            1.18f at 900 using FastOutSlowInEasing
            1.0f at 1500 using FastOutSlowInEasing
        },
        label = "coinScale"
    )
    
    // Y-axis float animation (using raw pixel values)
    val floatAnimation by animateFloatAsState(
        targetValue = if (isFlipping) -30f else 0f,
        animationSpec = keyframes {
            durationMillis = 1500
            0f at 0
            -20f at 250
            0f at 500
            -25f at 750
            0f at 1000
            -15f at 1250
            0f at 1500
        },
        label = "coinFloat"
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
                .height(300.dp),
            contentAlignment = Alignment.Center
        ) {
            // Animated shadow/glow effect
            if (isFlipping || currentResult != null) {
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .offset(y = 20.dp)
                        .clip(CircleShape)
                        .background(
                            MaterialTheme.colorScheme.primary.copy(
                                alpha = if (isFlipping) 0.25f else 0.12f
                            )
                        )
                )
            }
            
            // Coin container with animations
            Box(
                modifier = Modifier
                    .size(160.dp * scaleAnimation)
                    .offset(y = floatAnimation.dp)
                    .graphicsLayer {
                        // Apply 3D-like rotation effect
                        val radians = Math.toRadians(rotationAnimation.toDouble())
                        scaleX = kotlin.math.abs(kotlin.math.cos(radians)).toFloat()
                    }
                    .clip(CircleShape)
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val centerX = size.width / 2
                    val centerY = size.height / 2
                    val radius = size.minDimension / 2 - 8.dp.toPx()
                    
                    // Outer glow ring
                    drawCircle(
                        color = Color(0xFFFFB300).copy(alpha = 0.3f),
                        center = Offset(centerX, centerY),
                        radius = radius + 8.dp.toPx(),
                        style = Stroke(width = 4.dp.toPx())
                    )
                    
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
                        Color(0xFFBDBDBD) // Silver for tails
                    } else {
                        Color(0xFFFFC107) // Default gold while flipping
                    }
                    
                    drawCircle(
                        color = faceColor,
                        center = Offset(centerX, centerY),
                        radius = radius
                    )
                    
                    // Inner decorative ring
                    drawCircle(
                        color = faceColor.copy(alpha = 0.5f),
                        center = Offset(centerX, centerY),
                        radius = radius - 14.dp.toPx(),
                        style = Stroke(width = 2.dp.toPx())
                    )
                    
                    // Inner circle detail
                    drawCircle(
                        color = faceColor.copy(alpha = 0.3f),
                        center = Offset(centerX, centerY),
                        radius = radius - 22.dp.toPx(),
                        style = Stroke(width = 1.dp.toPx())
                    )
                }
                
                // Text on coin
                Box(contentAlignment = Alignment.Center) {
                    if (isFlipping) {
                        // Spinning indicator
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(50.dp),
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text="FLIPPING...",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    } else if (currentResult != null) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = currentResult!!.emoji,
                                style = MaterialTheme.typography.displayLarge,
                                fontWeight = FontWeight.Black,
                                fontSize = 64.sp,
                                color = if (currentResult == CoinSide.HEADS) 
                                    Color(0xFF5D4037) else Color(0xFF424242)
                            )
                            Text(
                                text = currentResult!!.displayName.toUpperCase(),
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 3.sp,
                                color = if (currentResult == CoinSide.HEADS) 
                                    Color(0xFF5D4037) else Color(0xFF424242)
                            )
                        }
                    } else {
                        // Initial state
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.HelpOutline,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text="TAP TO FLIP",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f),
                                fontWeight = FontWeight.Medium,
                                letterSpacing = 1.sp
                            )
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Flip button - more prominent
        Button(
            onClick = {
                if (!isFlipping) {
                    kotlinx.coroutines.GlobalScope.launch {
                        performFlip()
                    }
                }
            },
            enabled = !isFlipping,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(60.dp),
            shape = RoundedCornerShape(30.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            ),
            elevation = ButtonDefaults.buttonElevation(
                defaultElevation = 8.dp,
                pressedElevation = 4.dp
            )
        ) {
            Icon(
                imageVector = if (isFlipping) Icons.Default.HourglassTop else Icons.Default.Casino,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = if (isFlipping) "FLIPPING..." else "FLIP COIN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Statistics cards - horizontal scrollable
        HorizontalPagerStyleStats(headsCount, tailsCount, currentStreak, streakType, 
                                 bestHeadsStreak, bestTailsStreak)
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Secondary stats row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StatCard(
                title = "Total Flips",
                value = "${headsCount + tailsCount}",
                icon = Icons.Default.Numbers,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
            StatCard(
                title = "Best Streak",
                value = "H:$bestHeadsStreak T:$bestTailsStreak",
                icon = Icons.Default.EmojiEvents,
                modifier = Modifier.fillMaxWidth().weight(1f)
            )
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
            shape = RoundedCornerShape(16.dp)
        ) {
            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Reset All Statistics")
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
                shape = RoundedCornerShape(20.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.History, null, tint = MaterialTheme.colorScheme.primary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Recent Flips",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                        Text(
                            text = "${flipHistory.size.coerceAtMost(10)} shown",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // History as compact chips
                    HistoryFlowRow(
                        mainAxisSpacing = 8.dp,
                        crossAxisSpacing = 8.dp,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        flipHistory.reversed().take(10).reversed().forEach { result ->
                            Surface(
                                shape = CircleShape,
                                color = if (result.side == CoinSide.HEADS) 
                                    Color(0xFFFFD54F) else Color(0xFFE0E0E0)
                            ) {
                                Text(
                                    text = result.side.emoji,
                                    modifier = Modifier.padding(10.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (result.side == CoinSide.HEADS) 
                                        Color(0xFF5D4037) else Color(0xFF424242)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun HorizontalPagerStyleStats(
    headsCount: Int,
    tailsCount: Int,
    currentStreak: Int,
    streakType: CoinSide?,
    bestHeadsStreak: Int,
    bestTailsStreak: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Heads stats card
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFFFF8E1)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFFFD54F),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "H",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF5D4037)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$headsCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF5D4037)
                )
                Text(
                    text = "Heads",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF8D6E63)
                )
                Text(
                    text = "${if (headsCount + tailsCount > 0) headsCount * 100 / (headsCount + tailsCount) else 0}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFFA1887F)
                )
            }
        }
        
        // Tails stats card
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF5F5F5)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFFE0E0E0),
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "T",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF424242)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$tailsCount",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF424242)
                )
                Text(
                    text = "Tails",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF757575)
                )
                Text(
                    text = "${if (headsCount + tailsCount > 0) tailsCount * 100 / (headsCount + tailsCount) else 0}%",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF9E9E9E)
                )
            }
        }
        
        // Streak info card
        Card(
            modifier = Modifier.fillMaxWidth().weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(44.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "$currentStreak",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Text(
                    text = "Streak",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = streakType?.displayName ?: "-",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    icon: ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f)
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
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// Simple flow layout for history chips using Column
@Composable
private fun HistoryFlowRow(
    mainAxisSpacing: Dp = 8.dp,
    crossAxisSpacing: Dp = 8.dp,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    // Use a simple Column with wrapped content
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(crossAxisSpacing)
    ) {
        content()
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
