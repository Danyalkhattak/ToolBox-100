package com.dannyk.toolbox.ui.screens.tools.everyday

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import kotlinx.coroutines.delay
import kotlin.random.Random

data class DiceResult(
    val id: Int,
    val dice: List<Int>,
    val sides: Int,
    val total: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun DiceRollerScreen(navHostController: NavHostController) {
    var numberOfDice by remember { mutableIntStateOf(2) }
    var diceSides by remember { mutableIntStateOf(6) }
    var currentResults by remember { mutableStateOf(listOf<Int>()) }
    var isRolling by remember { mutableStateOf(false) }
    var rollHistory by remember { mutableStateOf(listOf<DiceResult>()) }
    
    // Animation values for each die
    val animationProgress = remember { mutableFloatStateOf(0f) }
    
    // Available dice configurations
    val availableSides = listOf(4, 6, 8, 10, 12, 20, 100)
    
    // Roll animation
    LaunchedEffect(isRolling) {
        if (isRolling) {
            // Animate for ~1 second
            val duration = 1000L
            val steps = 20
            val stepDelay = duration / steps
            
            repeat(steps) { step ->
                // Generate random intermediate results for visual effect
                currentResults = List(numberOfDice) { Random.nextInt(1, diceSides + 1) }
                animationProgress.floatValue = step.toFloat() / steps
                delay(stepDelay)
            }
            
            // Final result
            currentResults = List(numberOfDice) { Random.nextInt(1, diceSides + 1) }
            
            // Add to history
            rollHistory = rollHistory + DiceResult(
                id = rollHistory.size + 1,
                dice = currentResults.toList(),
                sides = diceSides,
                total = currentResults.sum()
            )
            
            isRolling = false
            animationProgress.floatValue = 1f
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ToolHeader(
            title = "Dice Roller",
            subtitle = "Roll virtual dice for games and decisions",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Dice display area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (currentResults.isEmpty()) {
                    // Empty state - show placeholder dice
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(minOf(numberOfDice, 4)),
                        modifier = Modifier.height(if (numberOfDice <= 3) 100.dp else 180.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(numberOfDice) { index ->
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerest.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Casino,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f),
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Text(
                        text = "Tap ROLL to start",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    // Show rolled dice
                    val rotationAngle = if (isRolling) animationProgress.floatValue * 360f else 0f
                    
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(minOf(currentResults.size, 4)),
                        modifier = Modifier.height(if (currentResults.size <= 3) 100.dp else 180.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(currentResults.size) { index ->
                            val value = currentResults[index]
                            
                            Box(
                                modifier = Modifier
                                    .size(70.dp)
                                    .graphicsLayer {
                                        if (isRolling) {
                                            rotationZ = rotationAngle * (if (index % 2 == 0) 1 else -1)
                                            scaleX = 0.9f + 0.1f * Random.nextFloat()
                                            scaleY = 0.9f + 0.1f * Random.nextFloat()
                                        }
                                    },
                                contentAlignment = Alignment.Center
                            ) {
                                DieView(value = value, sides = diceSides)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Total display
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Total:",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                text = "${currentResults.sum()}",
                                fontFamily = FontFamily.Monospace,
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            if (currentResults.size > 1) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "(${currentResults.average().format(1)} avg)",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Roll button
        Button(
            onClick = { 
                if (!isRolling) {
                    kotlinx.coroutines.GlobalScope.launch {
                        isRolling = true
                    }
                }
            },
            enabled = !isRolling,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 48.dp)
                .height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Icon(
                imageVector = if (isRolling) Icons.Default.Autorenew else Icons.Default.Casino,
                contentDescription = null,
                modifier = Modifier.size(26.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = if (isRolling) "ROLLING..." else "ROLL DICE",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            )
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Configuration section
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
                    text = "Configuration",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Number of dice selector
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(text = "Number of Dice", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "How many dice to roll at once",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = { if (numberOfDice > 1) numberOfDice-- },
                            enabled = !isRolling && numberOfDice > 1
                        ) {
                            Icon(Icons.Default.Remove, contentDescription = "Decrease")
                        }
                        
                        Text(
                            text = "$numberOfDice",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.width(40.dp),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        
                        IconButton(
                            onClick = { if (numberOfDice < 10) numberOfDice++ },
                            enabled = !isRolling && numberOfDice < 10
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Increase")
                        }
                    }
                }
                
                HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))
                
                // Dice type selector
                Text(
                    text = "Dice Type ($diceSides-sided)",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(availableSides) { sides ->
                        FilterChip(
                            selected = diceSides == sides,
                            onClick = { 
                                if (!isRolling) {
                                    diceSides = sides
                                    currentResults = emptyList() // Reset when changing type
                                }
                            },
                            label = { 
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("D$sides")
                                    if (sides == 100) Text(" (percentile)")
                                }
                            }
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Quick roll buttons
        Text(
            text = "Quick Roll Presets",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(horizontal = 16.dp)
        ) {
            items(listOf(
                "1d6" to Pair(1, 6),
                "2d6" to Pair(2, 6),
                "1d20" to Pair(1, 20),
                "2d20" to Pair(2, 20),
                "3d6" to Pair(3, 6),
                "4d6" to Pair(4, 6),
                "1d100" to Pair(1, 100),
                "1d8" to Pair(1, 8)
            )) { (label, config) ->
                OutlinedButton(
                    onClick = {
                        if (!isRolling) {
                            numberOfDice = config.first
                            diceSides = config.second
                            kotlinx.coroutines.GlobalScope.launch {
                                isRolling = true
                            }
                        }
                    },
                    enabled = !isRolling,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(label, fontWeight = FontWeight.SemiBold)
                }
            }
        }
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Roll history
        if (rollHistory.isNotEmpty()) {
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
                            text = "Roll History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { rollHistory = emptyList() }) {
                            Text("Clear", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 150.dp)
                    ) {
                        items(rollHistory.reversed().take(15).reversed()) { result ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Roll number
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "#${result.id}",
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Individual dice results
                                result.dice.forEach { value ->
                                    MiniDie(value = value, sides = result.sides)
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                // Total
                                Text(
                                    text = "= ${result.total}",
                                    fontFamily = FontFamily.Monospace,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                // Dice notation
                                Text(
                                    text = "${result.dice.size}d${result.sides}",
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

@Composable
private fun DieView(value: Int, sides: Int) {
    val backgroundColor = when {
        sides == 20 -> Color(0xFFE53935) // Red for d20
        sides == 12 -> Color(0xFF43A047) // Green for d12
        sides == 10 -> Color(0xFF1E88E5) // Blue for d10
        sides == 8 -> Color(0xFF8E24AA) // Purple for d8
        sides == 6 -> Color(0xFFE53935) // Red for d6
        sides == 4 -> Color(0xFFFB8C00) // Orange for d4
        sides == 100 -> Color(0xFF00897B) // Teal for percentile
        else -> MaterialTheme.colorScheme.primary
    }
    
    Box(
        modifier = Modifier
            .size(70.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        if (sides <= 20 || sides == 100) {
            // Show number for standard dice
            Text(
                text = if (sides == 100 && value == 100) "00" else "$value",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        } else {
            // For very large numbers or special cases
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun MiniDie(value: Int, sides: Int) {
    val backgroundColor = when {
        sides == 20 -> Color(0xFFE53935)
        sides == 12 -> Color(0xFF43A047)
        sides == 10 -> Color(0xFF1E88E5)
        sides == 8 -> Color(0xFF8E24AA)
        sides == 6 -> Color(0xFFE53935)
        sides == 4 -> Color(0xFFFB8C00)
        sides == 100 -> Color(0xFF00897B)
        else -> MaterialTheme.colorScheme.primary
    }
    
    Box(
        modifier = Modifier
            .size(28.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (value >= 100) "00" else "$value",
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

// Extension function to format double with specific decimal places
private fun Double.format(decimals: Int): String {
    return "%.${decimals}f".format(this)
}
