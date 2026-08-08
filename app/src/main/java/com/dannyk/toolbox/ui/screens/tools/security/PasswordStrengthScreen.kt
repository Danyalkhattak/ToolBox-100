package com.dannyk.toolbox.ui.screens.tools.security

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.material.icons.filled.Warning
import androidx.compose.foundation.ScrollState
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PasswordStrengthScreen(navController: NavHostController) {
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Calculate password strength metrics
    val hasUppercase = password.any { it.isUpperCase() }
    val hasLowercase = password.any { it.isLowerCase() }
    val hasNumbers = password.any { it.isDigit() }
    var hasSpecialChars = password.any { !it.isLetterOrDigit() }
    
    val lengthScore = when {
        password.isEmpty() -> 0
        password.length >= 16 -> 4
        password.length >= 12 -> 3
        password.length >= 8 -> 2
        else -> 1
    }

    // Calculate character pool size for entropy calculation
    fun calculatePoolSize(): Double {
        var poolSize = 0.0
        if (hasUppercase) poolSize += 26
        if (hasLowercase) poolSize += 26
        if (hasNumbers) poolSize += 10
        if (hasSpecialChars) poolSize += 32 // Common special characters
        return poolSize.coerceAtLeast(1.0)
    }

    // Calculate entropy in bits: E = L * log2(R)
    // where L is length and R is pool size
    val entropyBits = if (password.isNotEmpty()) {
        password.length * log2(calculatePoolSize())
    } else 0.0

    // Calculate overall strength score (0-100)
    val strengthScore = remember(password, hasUppercase, hasLowercase, hasNumbers, hasSpecialChars) {
        var score = 0
        
        // Length contribution (max 40 points)
        score += minOf(password.length * 2, 40)
        
        // Character variety (max 40 points)
        if (hasUppercase) score += 10
        if (hasLowercase) score += 10
        if (hasNumbers) score += 10
        if (hasSpecialChars) score += 10
        
        // Bonus for length with variety (max 20 points)
        if (password.length >= 12 && hasUppercase && hasLowercase && hasNumbers && hasSpecialChars) {
            score += 20
        } else if (password.length >= 8 && listOf(hasUppercase, hasLowercase, hasNumbers, hasSpecialChars).count { it } >= 3) {
            score += 10
        }
        
        score.coerceIn(0, 100)
    }

    // Strength level based on score
    data class StrengthLevel(val label: String, val color: Color, val description: String)
    
    val strengthLevel: StrengthLevel = when {
        password.isEmpty() -> StrengthLevel("Enter Password", Color.Gray, "Type a password to check its strength")
        strengthScore < 25 -> StrengthLevel("Weak", Color.Red, "This password is easily guessable")
        strengthScore < 50 -> StrengthLevel("Fair", Color(0xFFFF9800), "This password could be improved")
        strengthScore < 75 -> StrengthLevel("Good", Color(0xFF4CAF50), "This password is reasonably secure")
        strengthScore < 90 -> StrengthLevel("Strong", Color(0xFF2196F3), "This password is very secure")
        else -> StrengthLevel("Very Strong", Color(0xFF9C27B0), "Excellent! This password is extremely secure")
    }

    // Animated color for progress bar
    val animatedColor by animateColorAsState(
        targetValue = strengthLevel.color,
        animationSpec = androidx.compose.animation.core.tween(300),
        label = "strengthColor"
    )

    // Estimate crack time based on entropy
    // Assuming 10 billion guesses per second (modern GPU)
    fun estimateCrackTime(): String {
        if (entropyBits <= 0) return "N/A"
        
        // 10 billion guesses per second
        val guessesPerSecond = 10_000_000_000.0
        val combinations = Math.pow(2.0, entropyBits)
        val secondsToCrack = combinations / (guessesPerSecond * 2) // Average case
        
        return when {
            secondsToCrack < 1 -> "Instant"
            secondsToCrack < 60 -> "${secondsToCrack.toInt()} second(s)"
            secondsToCrack < 3600 -> "${(secondsToCrack / 60).toInt()} minute(s)"
            secondsToCrack < 86400 -> "${(secondsToCrack / 3600).toInt()} hour(s)"
            secondsToCrack < 31536000 -> "${(secondsToCrack / 86400).toInt()} day(s)"
            secondsToCrack < 31536000 * 100 -> "${(secondsToCrack / 31536000).toInt()} year(s)"
            secondsToCrack < 31536000 * 1000000 -> String.format("%.1f million years", secondsToCrack / (31536000 * 1000000))
            else -> "Centuries+"
        }
    }

    // Generate suggestions
    fun getSuggestions(): List<String> {
        val suggestions = mutableListOf<String>()
        
        if (password.isEmpty()) return suggestions
        
        if (password.length < 8) {
            suggestions.add("Use at least 8 characters")
        } else if (password.length < 12) {
            suggestions.add("Consider using 12+ characters for better security")
        }
        
        if (!hasUppercase) {
            suggestions.add("Add uppercase letters (A-Z)")
        }
        
        if (!hasLowercase) {
            suggestions.add("Add lowercase letters (a-z)")
        }
        
        if (!hasNumbers) {
            suggestions.add("Add numbers (0-9)")
        }
        
        if (!hasSpecialChars) {
            suggestions.add("Add special characters (!@#\$%...)")
        }
        
        // Check for common patterns
        if (password.lowercase() in listOf("password", "123456", "qwerty", "admin", "letmein", "welcome")) {
            suggestions.add("Avoid common passwords")
        }
        
        // Check for sequential characters
        if (password.windowed(3).any { window ->
            val chars = window.map { it.code }
            chars.zipWithNext().all { (a, b) -> b - a == 1 || a - b == 1 }
        }) {
            suggestions.add("Avoid sequential characters like 'abc' or '123'")
        }
        
        // Check for repeating characters
        if (password.windowed(3).all { window -> window.distinct().size == 1 }) {
            suggestions.add("Avoid repeating characters like 'aaa'")
        }
        
        return suggestions
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ToolTopBar(
            title = "Password Strength",
            onBackClick = { navController.navigateUp() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = "Enter Password",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { 
                        if (it.length <= 128) password = it 
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    placeholder = { Text("Type your password here") },
                    trailingIcon = {
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = if (passwordVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = if (passwordVisible) "Hide password" else "Show password"
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = animatedColor,
                        cursorColor = animatedColor
                    )
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${password.length} / 128 characters",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Strength Meter Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Circular Strength Indicator
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(140.dp)
                ) {
                    CircularProgressIndicator(
                        progress = if (password.isNotEmpty()) strengthScore / 100f else 0f,
                        modifier = Modifier.size(140.dp),
                        color = animatedColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant,
                        strokeWidth = 12.dp
                    )
                    
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (password.isNotEmpty()) "$strengthScore" else "-",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = animatedColor
                        )
                        Text(
                            text = "/ 100",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Strength Label
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = animatedColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = strengthLevel.label.uppercase(),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = animatedColor,
                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = strengthLevel.description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Criteria Checklist
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Security Criteria",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Length Criteria
                CriterionRow(
                    label = "Length: ${password.length} characters",
                    sublabel = when {
                        password.isEmpty() -> ""
                        password.length >= 16 -> "Excellent (16+)"
                        password.length >= 12 -> "Good (12+)"
                        password.length >= 8 -> "Minimum (8+)"
                        else -> "Too short (< 8)"
                    },
                    isMet = password.length >= 8,
                    isExcellent = password.length >= 16
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Uppercase
                CriterionRow(
                    label = "Uppercase Letters",
                    sublabel = "A-Z",
                    isMet = hasUppercase
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Lowercase
                CriterionRow(
                    label = "Lowercase Letters",
                    sublabel = "a-z",
                    isMet = hasLowercase
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Numbers
                CriterionRow(
                    label = "Numbers",
                    sublabel = "0-9",
                    isMet = hasNumbers
                )

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                // Special Characters
                CriterionRow(
                    label = "Special Characters",
                    sublabel = "!@#\$%^&*...",
                    isMet = hasSpecialChars
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Entropy & Crack Time Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Security Analysis",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    AnalysisItem(
                        label = "Entropy",
                        value = if (entropyBits > 0) "%.1f bits".format(entropyBits) else "- bits",
                        icon = "🔐"
                    )
                    AnalysisItem(
                        label = "Est. Crack Time",
                        value = estimateCrackTime(),
                        icon = "⏱️"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Divider(modifier = Modifier.fillMaxWidth())

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Based on ~10 billion guesses/second (GPU attack)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Suggestions Card
        val suggestions = getSuggestions()
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (suggestions.isNotEmpty()) MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f) else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = if (suggestions.isNotEmpty()) Icons.Default.Warning else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (suggestions.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (suggestions.isNotEmpty()) "Suggestions" else "Great Password!",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (suggestions.isNotEmpty()) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary
                    )
                }

                if (suggestions.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    suggestions.forEachIndexed { index, suggestion ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(vertical = 4.dp)
                        ) {
                            Text(
                                text = "• ",
                                color = MaterialTheme.colorScheme.error
                            )
                            Text(
                                text = suggestion,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                } else if (password.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Your password meets all security criteria!",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun CriterionRow(
    label: String,
    sublabel: String,
    isMet: Boolean,
    isExcellent: Boolean = false
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isMet || isExcellent) FontWeight.Medium else FontWeight.Normal
            )
            if (sublabel.isNotEmpty()) {
                Text(
                    text = sublabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        val iconColor = when {
            isExcellent -> Color(0xFF9C27B0) // Purple for excellent
            isMet -> Color(0xFF4CAF50) // Green for met
            else -> Color.Gray // Gray for not met
        }
        
        val backgroundColor = when {
            isExcellent -> Color(0xFF9C27B0).copy(alpha = 0.15f)
            isMet -> Color(0xFF4CAF50).copy(alpha = 0.15f)
            else -> Color.Gray.copy(alpha = 0.1f)
        }
        
        Box(
            modifier = Modifier
                .size(32.dp)
                .background(backgroundColor, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isMet || isExcellent) Icons.Default.CheckCircle else Icons.Default.Close,
                contentDescription = if (isMet || isExcellent) "Met" else "Not met",
                tint = iconColor,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AnalysisItem(label: String, value: String, icon: String) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(text = icon, style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
