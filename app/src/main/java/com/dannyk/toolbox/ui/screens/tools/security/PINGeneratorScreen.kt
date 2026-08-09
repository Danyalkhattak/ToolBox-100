package com.dannyk.toolbox.ui.screens.tools.security

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import java.security.SecureRandom
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.foundation.clickable
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.filled.Check
import java.util.regex.Pattern
import androidx.compose.foundation.ScrollState
import kotlin.math.*
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PINGeneratorScreen(navController: NavHostController) {
    val context = LocalContext.current
    var pinLength by remember { mutableIntStateOf(4) }
    var avoidPatterns by remember { mutableStateOf(true) }
    var generateMultiple by remember { mutableStateOf(false) }
    var multipleCount by remember { mutableIntStateOf(5) }
    
    var generatedPins by remember { mutableStateOf(listOf<String>()) }
    var isGenerating by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf("") }

    val secureRandom = SecureRandom()

    fun isSequentialOrRepeating(pin: String): Boolean {
        if (pin.length < 2) return false
        
        // Check for repeating digits (e.g., "1111", "9999")
        if (pin.all { it == pin[0] }) return true
        
        // Check for sequential ascending (e.g., "1234", "5678")
        val sequentialAsc = pin.windowed(2).all { window ->
            val diff = window[1].code - window[0].code
            diff == 1 || diff == -9 // handles wrap-around like "8901"
        }
        if (sequentialAsc) return true
        
        // Check for sequential descending (e.g., "4321", "9876")
        val sequentialDesc = pin.windowed(2).all { window ->
            val diff = window[1].code - window[0].code
            diff == -1 || diff == 9 // handles wrap-around like "1098"
        }
        if (sequentialDesc) return true
        
        return false
    }

    fun generateSinglePIN(length: Int, avoidBadPatterns: Boolean): String {
        val maxAttempts = 100
        var attempts = 0
        
        do {
            val digits = (1..length).map { secureRandom.nextInt(10).toString() }.joinToString("")
            attempts++
            
            if (!avoidBadPatterns || !isSequentialOrRepeating(digits)) {
                return digits
            }
        } while (attempts < maxAttempts)
        
        // Fallback if we can't find a good PIN after many attempts
        return (1..length).map { secureRandom.nextInt(10).toString() }.joinToString("")
    }

    fun generatePINs() {
        if (!generateMultiple) {
            generatedPins = listOf(generateSinglePIN(pinLength, avoidPatterns))
        } else {
            generatedPins = (1..multipleCount).map { generateSinglePIN(pinLength, avoidPatterns) }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ToolTopBar(
            title = "PIN Generator",
            onBackClick = { navController.navigateUp() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(
                    Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "About PIN Codes",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate cryptographically secure random PIN codes using SecureRandom. Avoid common patterns like '1234' or '0000' for better security.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Options Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "PIN Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Length Selector
                Text(
                    text = "PIN Length: $pinLength digits",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = pinLength.toFloat(),
                    onValueChange = { pinLength = it.toInt() },
                    valueRange = 4f..12f,
                    steps = 7,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("4", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("6", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("8", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("10", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("12", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Quick length buttons
                Text(
                    text = "Quick Select:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(4, 6, 8).forEach { len ->
                        FilterChip(
                            selected = pinLength == len,
                            onClick = { pinLength = len },
                            label = { Text("$len-digit") },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Options
                Divider(modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(12.dp))

                // Avoid patterns option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { avoidPatterns = !avoidPatterns }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = avoidPatterns,
                        onCheckedChange = { avoidPatterns = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Avoid Weak Patterns", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Skip sequences (1234), repeats (1111)",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Generate multiple option
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { generateMultiple = !generateMultiple }
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = generateMultiple,
                        onCheckedChange = { generateMultiple = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Generate Multiple PINs", style = MaterialTheme.typography.bodyMedium)
                        if (generateMultiple) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Count: ", style = MaterialTheme.typography.bodySmall)
                                // Simple counter
                                OutlinedTextField(
                                    value = multipleCount.toString(),
                                    onValueChange = { 
                                        it.toIntOrNull()?.let { count -> 
                                            multipleCount = count.coerceIn(1, 20) 
                                        } 
                                    },
                                    modifier = Modifier.width(80.dp),
                                    singleLine = true,
                                    shape = RoundedCornerShape(8.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Generate Button
                Button(
                    onClick = {
                        isGenerating = true
                        generatePINs()
                        isGenerating = false
                    },
                    enabled = !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text(if (generateMultiple) "Generate $multipleCount PINs" else "Generate PIN")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Result Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = if (generateMultiple) "Generated PINs" else "Generated PIN",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (generatedPins.isNotEmpty()) {
                    if (!generateMultiple && generatedPins.size == 1) {
                        // Single PIN display - large and prominent
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(16.dp))
                                .padding(32.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = generatedPins.first(),
                                style = MaterialTheme.typography.displayMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 8.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Copy button for single PIN
                        OutlinedButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                clipboard?.setPrimaryClip(ClipData.newPlainText("pin", generatedPins.first()))
                                showCopiedMessage = generatedPins.first()
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showCopiedMessage == generatedPins.first()) "Copied!" else "Copy PIN")
                        }

                        LaunchedEffect(showCopiedMessage) {
                            if (showCopiedMessage.isNotEmpty()) {
                                kotlinx.coroutines.delay(2000)
                                showCopiedMessage = ""
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // PIN Security Info
                        Divider(modifier = Modifier.fillMaxWidth())
                        
                        Spacer(modifier = Modifier.height(16.dp))

                        val entropy = calculatePINEntropy(generatedPins.first().length)
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem(label = "Digits", value = "${generatedPins.first().length}")
                            StatItem(label="Combinations", value = formatCombinations(generatedPins.first().length))
                            StatItem(label="Entropy", value = "%.1f bits".format(entropy))
                        }
                    } else {
                        // Multiple PINs display in grid
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(2),
                            modifier = Modifier.height((generatedPins.size * 70).coerceAtMost(400).dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(generatedPins) { pin ->
                                Surface(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                            clipboard?.setPrimaryClip(ClipData.newPlainText("pin", pin))
                                            showCopiedMessage = pin
                                        },
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surface
                                ) {
                                    Row(
                                        modifier = Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = pin,
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontFamily = FontFamily.Monospace,
                                                fontWeight = FontWeight.Bold,
                                                letterSpacing = 2.sp
                                            )
                                        )
                                        Icon(
                                            Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }

                        if (showCopiedMessage.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Copied: $showCopiedMessage",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(2000)
                                showCopiedMessage = ""
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Summary info
                        Divider(modifier = Modifier.fillMaxWidth())
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(
                            text = "Generated ${generatedPins.size} PINs of ${pinLength} digits each",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Configure settings and click Generate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // PIN Security Tips Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "PIN Security Tips",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                SecurityTipItem(
                    tip = "Use at least 6 digits",
                    reason = "4-digit PINs have only 10,000 combinations"
                )
                SecurityTipItem(
                    tip = "Avoid birthdates & years",
                    reason = "Easily guessable from personal info"
                )
                SecurityTipItem(
                    tip = "Don't use repeating digits",
                    reason = "0000, 1111 are among most common PINs"
                )
                SecurityTipItem(
                    tip = "Avoid sequences",
                    reason = "1234, 9876 are easily guessed"
                )
                SecurityTipItem(
                    tip = "Use unique PINs",
                    reason = "Different PIN for each account"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Divider(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))

                // Entropy reference table
                Text(
                    text = "PIN Entropy Reference:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    EntropyRow(digits = 4, entropy = 13.3)
                    EntropyRow(digits = 6, entropy = 19.9)
                    EntropyRow(digits = 8, entropy = 26.6)
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

private fun calculatePINEntropy(length: Int): Double {
    // Each digit has 10 possibilities (0-9)
    return length * log2(10.0)
}

private fun formatCombinations(length: Int): String {
    val combinations = Math.pow(10.0, length.toDouble())
    return when {
        combinations >= 1_000_000_000_000.0 -> "${(combinations / 1_000_000_000_000.0).toInt()}T"
        combinations >= 1_000_000_000.0 -> "${(combinations / 1_000_000_000.0).toInt()}B"
        combinations >= 1_000_000.0 -> "${(combinations / 1_000_000.0).toInt()}M"
        combinations >= 1_000.0 -> "${(combinations / 1_000.0).toInt()}K"
        else -> combinations.toInt().toString()
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SecurityTipItem(tip: String, reason: String) {
    Row(
        modifier = Modifier.padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = null,
            tint = Color(0xFF4CAF50),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(
                text = tip,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = reason,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun EntropyRow(digits: Int, entropy: Double) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            Text(
                text = "$digits",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "%.1f bits".format(entropy),
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace
        )
    }
}
