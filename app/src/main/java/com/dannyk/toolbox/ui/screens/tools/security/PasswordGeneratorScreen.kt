package com.dannyk.toolbox.ui.screens.tools.security

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
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
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import java.security.SecureRandom
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordGeneratorScreen(navController: NavHostController) {
    val context = LocalContext.current
    var passwordLength by remember { mutableIntStateOf(16) }
    var includeUppercase by remember { mutableStateOf(true) }
    var includeLowercase by remember { mutableStateOf(true) }
    var includeNumbers by remember { mutableStateOf(true) }
    var includeSpecialChars by remember { mutableStateOf(true) }
    var excludeAmbiguous by remember { mutableStateOf(false) }
    
    var generatedPassword by remember { mutableStateOf("") }
    var passwordHistory by remember { mutableStateOf(listOf<String>()) }
    var showHistory by remember { mutableStateOf(false) }
    var isGenerating by remember { mutableStateOf(false) }
    var copiedMessage by remember { mutableStateOf(false) }

    val secureRandom = SecureRandom()

    // Animation for generate button
    val rotationAngle by animateFloatAsState(
        targetValue = if (isGenerating) 360f else 0f,
        animationSpec = tween(durationMillis = 500),
        label = "rotation"
    )

    fun generatePassword(): String {
        val uppercaseChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
        val lowercaseChars = "abcdefghijklmnopqrstuvwxyz"
        val numberChars = "0123456789"
        val specialChars = "!@#\$%^&*()_+-=[]{}|;:,.<>?"
        val ambiguousChars = setOf('0', 'O', 'o', '1', 'l', 'I')

        var charPool = StringBuilder()
        
        if (includeUppercase) {
            charPool.append(if (excludeAmbiguous) uppercaseChars.filter { it !in ambiguousChars } else uppercaseChars)
        }
        if (includeLowercase) {
            charPool.append(if (excludeAmbiguous) lowercaseChars.filter { it !in ambiguousChars } else lowercaseChars)
        }
        if (includeNumbers) {
            charPool.append(if (excludeAmbiguous) numberChars.filter { it !in ambiguousChars } else numberChars)
        }
        if (includeSpecialChars) {
            charPool.append(if (excludeAmbiguous) specialChars.filter { it !in ambiguousChars } else specialChars)
        }

        if (charPool.isEmpty()) {
            return "Select at least one character type"
        }

        val pool = charPool.toString()
        val password = StringBuilder(passwordLength)

        // Ensure at least one character from each selected type
        val guaranteedChars = mutableListOf<Char>()
        if (includeUppercase) {
            val availableUpper = if (excludeAmbiguous) uppercaseChars.filter { it !in ambiguousChars } else uppercaseChars
            guaranteedChars.add(availableUpper[secureRandom.nextInt(availableUpper.length)])
        }
        if (includeLowercase) {
            val availableLower = if (excludeAmbiguous) lowercaseChars.filter { it !in ambiguousChars } else lowercaseChars
            guaranteedChars.add(availableLower[secureRandom.nextInt(availableLower.length)])
        }
        if (includeNumbers) {
            val availableNums = if (excludeAmbiguous) numberChars.filter { it !in ambiguousChars } else numberChars
            guaranteedChars.add(availableNums[secureRandom.nextInt(availableNums.length)])
        }
        if (includeSpecialChars) {
            val availableSpecial = if (excludeAmbiguous) specialChars.filter { it !in ambiguousChars } else specialChars
            guaranteedChars.add(availableSpecial[secureRandom.nextInt(availableSpecial.length)])
        }

        // Add guaranteed characters
        guaranteedChars.shuffle(secureRandom)
        for (i in guaranteedChars.indices.take(minOf(guaranteedChars.size, passwordLength))) {
            password[i] = guaranteedChars[i]
        }

        // Fill remaining positions
        for (i in guaranteedChars.size until passwordLength) {
            password.append(pool[secureRandom.nextInt(pool.length)])
        }

        // Shuffle the result
        val shuffled = password.toList().shuffled(secureRandom).joinToString("")
        return shuffled
    }

    fun calculateStrength(pwd: String): Triple<String, Color, Float> {
        if (pwd.isEmpty()) return Triple("None", Color(0xFF9E9E9E), 0f)
        
        var score = 0f
        
        // Length scoring
        when {
            pwd.length >= 16 -> score += 3f
            pwd.length >= 12 -> score += 2.5f
            pwd.length >= 8 -> score += 2f
            pwd.length >= 6 -> score += 1f
            else -> score += 0.5f
        }
        
        // Character variety
        if (pwd.any { it.isUpperCase() }) score += 1.5f
        if (pwd.any { it.isLowerCase() }) score += 1.5f
        if (pwd.any { it.isDigit() }) score += 1.5f
        if (pwd.any { !it.isLetterOrDigit() }) score += 2f
        
        // Extra variety bonus
        val uniqueChars = pwd.toSet().size.toFloat()
        score += (uniqueChars / pwd.length.coerceAtLeast(1)) * 1.5f

        val maxScore = 13f
        val percentage = (score / maxScore).coerceIn(0f, 1f)
        
        return when {
            percentage < 0.25f -> Triple("Weak", Color(0xFFF44336), percentage)
            percentage < 0.5f -> Triple("Fair", Color(0xFFFF9800), percentage)
            percentage < 0.7f -> Triple("Good", Color(0xFF4CAF50), percentage)
            percentage < 0.9f -> Triple("Strong", Color(0xFF2196F3), percentage)
            else -> Triple("Very Strong", Color(0xFF9C27B0), percentage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ToolTopBar(
            title = "Password Generator",
            onBackClick = { navController.navigateUp() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Generated Password Display Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Generated Password",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = if (generatedPassword.isEmpty()) "••••••••••••" else generatedPassword,
                        style = MaterialTheme.typography.headlineSmall.copy(
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Medium
                        ),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f),
                        maxLines = 2
                    )
                    
                    if (generatedPassword.isNotEmpty()) {
                        IconButton(onClick = {
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("password", generatedPassword)
                            clipboard.setPrimaryClip(clip)
                            copiedMessage = true
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                        }
                    }
                }

                if (copiedMessage) {
                    Text(
                        text = "Copied to clipboard!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        copiedMessage = false
                    }
                }

                // Strength Indicator
                if (generatedPassword.isNotEmpty()) {
                    val (strengthLabel, strengthColor, strengthValue) = calculateStrength(generatedPassword)
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    LinearProgressIndicator(
                        progress = strengthValue,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = strengthColor,
                        trackColor = MaterialTheme.colorScheme.surface
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "Strength: $strengthLabel",
                        style = MaterialTheme.typography.labelLarge,
                        color = strengthColor,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Length: ${generatedPassword.length} characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Generate Button
                Button(
                    onClick = {
                        isGenerating = true
                        generatedPassword = generatePassword()
                        passwordHistory = listOf(generatedPassword) + passwordHistory.take(9)
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    )
                ) {
                    Icon(
                        Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.graphicsLayer { 
                            rotationZ = rotationAngle 
                        }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Generate Password", style = MaterialTheme.typography.titleMedium)
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Options Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Length Slider
                Text(
                    text = "Password Length: $passwordLength",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = passwordLength.toFloat(),
                    onValueChange = { passwordLength = it.toInt() },
                    valueRange = 8f..128f,
                    steps = 119,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("8", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("128", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Character Type Checkboxes
                Text(
                    text = "Character Types",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    FilterChip(
                        selected = includeUppercase,
                        onClick = { includeUppercase = !includeUppercase },
                        label = { Text("ABC") },
                        leadingIcon = if (includeUppercase) {{ Icon(Icons.Default.Check, contentDescription = null) }} else null
                    )
                    FilterChip(
                        selected = includeLowercase,
                        onClick = { includeLowercase = !includeLowercase },
                        label = { Text("abc") },
                        leadingIcon = if (includeLowercase) {{ Icon(Icons.Default.Check, contentDescription = null) }} else null
                    )
                    FilterChip(
                        selected = includeNumbers,
                        onClick = { includeNumbers = !includeNumbers },
                        label = { Text("123") },
                        leadingIcon = if (includeNumbers) {{ Icon(Icons.Default.Check, contentDescription = null) }} else null
                    )
                    FilterChip(
                        selected = includeSpecialChars,
                        onClick = { includeSpecialChars = !includeSpecialChars },
                        label = { Text("@#\$") },
                        leadingIcon = if (includeSpecialChars) {{ Icon(Icons.Default.Check, contentDescription = null) }} else null
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Exclude Ambiguous Characters
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { excludeAmbiguous = !excludeAmbiguous }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = excludeAmbiguous,
                        onCheckedChange = { excludeAmbiguous = it }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(text = "Exclude Ambiguous Characters", style = MaterialTheme.typography.bodyMedium)
                        Text(
                            text = "Removes: 0, O, o, 1, l, I",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // History Section
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showHistory = !showHistory },
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Recent Passwords (${passwordHistory.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (showHistory) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (showHistory) "Collapse" else "Expand"
                    )
                }

                if (showHistory && passwordHistory.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    passwordHistory.forEachIndexed { index, pwd ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .clickable {
                                    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                    val clip = ClipData.newPlainText("password", pwd)
                                    clipboard.setPrimaryClip(clip)
                                    copiedMessage = true
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. ${pwd.take(24)}${if (pwd.length > 24) "..." else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontFamily = FontFamily.Monospace,
                                modifier = Modifier.weight(1f)
                            )
                            Icon(
                                Icons.Default.ContentCopy,
                                contentDescription = "Copy",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        if (index < passwordHistory.lastIndex) {
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    TextButton(
                        onClick = { passwordHistory = emptyList() },
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Text("Clear History", color = MaterialTheme.colorScheme.error)
                    }
                } else if (showHistory && passwordHistory.isEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No passwords generated yet",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                }
                
                Divider(modifier = Modifier.padding(vertical = 8.dp))
                
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Passwords stored in memory only. Cleared when app closes.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}
