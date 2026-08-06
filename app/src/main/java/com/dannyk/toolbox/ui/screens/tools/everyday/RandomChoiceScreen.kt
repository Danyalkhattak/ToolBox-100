package com.dannyk.toolbox.ui.screens.tools.everyday

import androidx.compose.animation.core.*
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolHeader
import kotlinx.coroutines.delay

data class ChoiceItem(
    val id: Int,
    val text: String,
    var isEliminated: Boolean = false,
    var wasPicked: Boolean = false
)

data class PickHistory(
    val id: Int,
    val winner: String,
    val allChoices: List<String>,
    val timestamp: Long = System.currentTimeMillis()
)

@Composable
fun RandomChoiceScreen(navHostController: NavHostController) {
    var choices by remember { mutableStateOf(listOf<ChoiceItem>()) }
    var inputText by remember { mutableStateOf("") }
    var currentWinner by remember { mutableStateOf<String?>(null) }
    var isPicking by remember { mutableStateOf(false) }
    var displayedChoice by remember { mutableStateOf("") }
    
    // Elimination mode
    var eliminationMode by remember { mutableStateOf(false) }
    var remainingChoices by remember { mutableStateOf(listOf<ChoiceItem>()) }
    
    // History
    var pickHistory by remember { mutableStateOf(listOf<PickHistory>()) }
    
    // Animation for selection process
    val animatedScale by animateFloatAsState(
        targetValue = if (isPicking && currentWinner != null) 1.1f else 1f,
        animationSpec = repeatable(
            iterations = if (isPicking) Infinite else 1,
            animation = keyframes {
                durationMillis = 100
                1f at 0
                1.1f at 50
                1f at 100
            }
        ),
        label = "scale"
    )
    
    val animatedColor by animateColorAsState(
        targetValue = if (currentWinner != null && !isPicking) 
            MaterialTheme.colorScheme.primary 
        else MaterialTheme.colorScheme.onSurface,
        animationSpec = tween(300),
        label = "color"
    )
    
    // Selection coroutine
    LaunchedEffect(isPicking) {
        if (isPicking) {
            val activeChoices = if (eliminationMode) {
                remainingChoices.filter { !it.isEliminated }
            } else {
                choices.filter { !it.isEliminated }
            }.map { it.text }
            
            if (activeChoices.isNotEmpty()) {
                // Animate through choices
                val iterations = 20 + Random.nextInt(10)
                for (i in iterations downTo 0) {
                    displayedChoice = activeChoices[Random.nextInt(activeChoices.size)]
                    delay(50L + (iterations - i) * 10L) // Slow down over time
                }
                
                // Final selection
                val winnerIndex = Random.nextInt(activeChoices.size)
                currentWinner = activeChoices[winnerIndex]
                displayedChoice = currentWinner!!
                
                // In elimination mode, mark as picked
                if (eliminationMode) {
                    val winnerId = remainingChoices.find { it.text == currentWinner }?.id
                    remainingChoices = remainingChoices.map {
                        if (it.id == winnerId) it.copy(wasPicked = true, isEliminated = true) else it
                    }
                }
                
                // Add to history
                pickHistory = pickHistory + PickHistory(
                    id = pickHistory.size + 1,
                    winner = currentWinner!!,
                    allChoices = activeChoices
                )
            }
            
            isPicking = false
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
    ) {
        ToolHeader(
            title = "Random Choice",
            subtitle = "Let fate decide from your options",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(16.dp))
        
        // Winner display area
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .height(180.dp),
            colors = CardDefaults.cardColors(
                containerColor = if (currentWinner != null && !isPicking) 
                    MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f) 
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                if (currentWinner == null && !isPicking) {
                    // Empty state
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Shuffle,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Add choices and pick!",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    // Display result
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.graphicsLayer {
                            scaleX = animatedScale
                            scaleY = animatedScale
                        }
                    ) {
                        if (isPicking) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(40.dp),
                                color = MaterialTheme.colorScheme.primary,
                                strokeWidth = 3.dp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                        } else {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFFFFB300),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        Text(
                            text = displayedChoice.ifEmpty { "?" },
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = animatedColor,
                            textAlign = TextAlign.Center,
                            maxLines = 2,
                            modifier = Modifier.padding(horizontal = 32.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = when {
                                isPicking -> "Selecting..."
                                eliminationMode -> "Winner selected!"
                                else -> "The winner is..."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(20.dp))
        
        // Input area
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
                    text = "Add Choices",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = { 
                        Text("Enter choice (one per line or comma-separated)") 
                    },
                    leadingIcon = { Icon(Icons.Default.EditNote, null) },
                    trailingIcon = {
                        if (inputText.isNotEmpty()) {
                            IconButton(onClick = { inputText = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = false,
                    minLines = 2,
                    maxLines = 4,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            // Parse input - support both comma-separated and newlines
                            val newChoices = inputText
                                .split(",", "\n")
                                .map { it.trim() }
                                .filter { it.isNotEmpty() }
                            
                            if (newChoices.isNotEmpty()) {
                                val nextId = (choices.maxOfOrNull { it.id } ?: 0) + 1
                                choices = choices + newChoices.mapIndexed { index, text ->
                                    ChoiceItem(id = nextId + index, text = text)
                                }
                                remainingChoices = choices.toList() // Reset for elimination mode
                                inputText = ""
                            }
                        },
                        enabled = inputText.isNotBlank(),
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Add")
                    }
                    
                    OutlinedButton(
                        onClick = {
                            // Add some example choices
                            val examples = listOf("Option A", "Option B", "Option C")
                            val nextId = (choices.maxOfOrNull { it.id } ?: 0) + 1
                            choices = choices + examples.mapIndexed { index, text ->
                                ChoiceItem(id = nextId + index, text = text)
                            }
                            remainingChoices = choices.toList()
                        },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Lightbulb, null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Examples")
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Mode toggle and pick button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Mode card
            Card(
                modifier = Modifier.weight(1f),
                onClick = { eliminationMode = !eliminationMode },
                colors = CardDefaults.cardColors(
                    containerColor = if (eliminationMode) 
                        MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f) else 
                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (eliminationMode) Icons.Default.Eliminate else Icons.Default.Shuffle,
                        contentDescription = null,
                        tint = if (eliminationMode) MaterialTheme.colorScheme.primary 
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (eliminationMode) "Elimination" else "Standard",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (eliminationMode) "Pick without replacement" else "Random each time",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            
            // Pick button
            Button(
                onClick = {
                    if (!isPicking) {
                        val activeChoices = if (eliminationMode) {
                            remainingChoices.filter { !it.isEliminated }
                        } else {
                            choices.filter { !it.isEliminated }
                        }
                        
                        if (activeChoices.isNotEmpty()) {
                            currentWinner = null
                            isPicking = true
                        }
                    }
                },
                enabled = !isPicking && choices.any { !it.isEliminated } &&
                         (!eliminationMode || remainingChoices.any { !it.isEliminated }),
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Casino,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("PICK!", fontWeight = FontWeight.Bold)
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Choices list
        if (choices.isNotEmpty()) {
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
                            text = "Choices (${choices.count { !it.isEliminated }}/${choices.size})",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        
                        TextButton(onClick = {
                            choices = emptyList()
                            remainingChoices = emptyList()
                            currentWinner = null
                        }) {
                            Text("Clear All", color = MaterialTheme.colorScheme.error)
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        items(choices, key = { it.id }) { item ->
                            val isWinner = item.text == currentWinner && !item.isEliminated
                            
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        when {
                                            item.isEliminated -> MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                                            isWinner -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                                            else -> Color.Transparent
                                        }
                                    )
                                    .padding(vertical = 8.dp, horizontal = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Status indicator
                                Box(
                                    modifier = Modifier
                                        .size(28.dp)
                                        .clip(CircleShape)
                                        .background(
                                            when {
                                                item.isEliminated -> MaterialTheme.colorScheme.errorContainer
                                                item.wasPicked -> MaterialTheme.colorScheme.tertiaryContainer
                                                isWinner -> MaterialTheme.colorScheme.primary
                                                else -> MaterialTheme.colorScheme.surfaceContainerHighest
                                            }
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    when {
                                        item.isEliminated -> Icon(
                                            Icons.Default.Close,
                                            null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                        item.wasPicked -> Icon(
                                            Icons.Default.Check,
                                            null,
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.tertiary
                                        )
                                        isWinner -> Icon(
                                            Icons.Default.Star,
                                            null,
                                            modifier = Modifier.size(14.dp),
                                            tint = Color.White
                                        )
                                        else -> Text(
                                            "${choices.indexOf(item) + 1}",
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                Text(
                                    text = item.text,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = if (isWinner) FontWeight.Bold else FontWeight.Normal,
                                    color = if (item.isEliminated) 
                                        MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f) 
                                    else 
                                        MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                IconButton(
                                    onClick = {
                                        choices = choices.filter { it.id != item.id }
                                        remainingChoices = remainingChoices.filter { it.id != item.id }
                                        if (currentWinner == item.text) currentWinner = null
                                    }
                                ) {
                                    Icon(
                                        Icons.Default.DeleteOutline,
                                        contentDescription = "Remove",
                                        tint = MaterialTheme.colorScheme.error.copy(alpha = 0.7f),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Reset eliminated in elimination mode
                    if (eliminationMode && remainingChoices.any { it.isEliminated }) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = {
                                remainingChoices = remainingChoices.map { 
                                    it.copy(isEliminated = false, wasPicked = false) 
                                }
                                choices = choices.map { 
                                    it.copy(isEliminated = false, wasPicked = false) 
                                }
                                currentWinner = null
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.Refresh, null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset Eliminations")
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // History
        if (pickHistory.isNotEmpty()) {
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
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Pick History",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        TextButton(onClick = { pickHistory = emptyList() }) {
                            Text("Clear")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 150.dp)
                    ) {
                        items(pickHistory.reversed().take(10).reversed()) { entry ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Surface(
                                    shape = CircleShape,
                                    color = MaterialTheme.colorScheme.primaryContainer
                                ) {
                                    Text(
                                        text = "#${entry.id}",
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(8.dp))
                                
                                Icon(
                                    Icons.Default.EmojiEvents,
                                    contentDescription = null,
                                    tint = Color(0xFFFFB300),
                                    modifier = Modifier.size(16.dp)
                                )
                                
                                Spacer(modifier = Modifier.width(4.dp))
                                
                                Text(
                                    text = entry.winner,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                Text(
                                    text = "of ${entry.allChoices.size}",
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
