package com.dannyk.toolbox.ui.screens.tools.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import java.security.SecureRandom
import java.util.Base64
import kotlin.math.log2
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TokenGeneratorScreen(navController: NavHostController) {
    var tokenLength by remember { mutableIntStateOf(32) }
    var selectedFormat by remember { mutableStateOf("hex") }
    var generatedToken by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    val secureRandom = SecureRandom()

    data class TokenFormat(
        val id: String,
        val name: String,
        val description: String,
        val charSetSize: Int
    )

    val formats = listOf(
        TokenFormat("hex", "Hexadecimal", "0-9, a-f", 16),
        TokenFormat("base64", "Base64", "Standard Base64", 64),
        TokenFormat("base64url", "Base64URL", "URL-safe Base64", 64),
        TokenFormat("alphanumeric", "Alphanumeric", "A-Z, a-z, 0-9", 62)
    )

    fun generateToken(length: Int, format: String): String {
        return try {
            when (format) {
                "hex" -> {
                    val bytes = ByteArray(length)
                    secureRandom.nextBytes(bytes)
                    bytes.joinToString("") { "%02x".format(it) }
                }
                "base64" -> {
                    // For base64, length refers to random bytes before encoding
                    val bytes = ByteArray(length)
                    secureRandom.nextBytes(bytes)
                    Base64.getEncoder().encodeToString(bytes).trimEnd('=')
                }
                "base64url" -> {
                    val bytes = ByteArray(length)
                    secureRandom.nextBytes(bytes)
                    Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
                }
                "alphanumeric" -> {
                    val chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
                    (1..length).map { chars[secureRandom.nextInt(chars.length)] }.joinToString("")
                }
                else -> ""
            }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    // Calculate entropy based on token and format
    fun calculateEntropy(token: String): Double {
        if (token.isEmpty()) return 0.0
        
        val charsetSize = when (selectedFormat) {
            "hex" -> 16.0
            "base64", "base64url" -> 64.0
            "alphanumeric" -> 62.0
            else -> token.toSet().size.toDouble()
        }
        
        return token.length * log2(charsetSize)
    }

    // Get entropy quality description
    fun getEntropyQuality(entropy: Double): Pair<String, Color> {
        return when {
            entropy < 64 -> Pair("Weak - Suitable for non-sensitive use only", Color.Red)
            entropy < 128 -> Pair("Fair - Basic protection", Color(0xFFFF9800))
            entropy < 192 -> Pair("Good - Standard security", Color(0xFF4CAF50))
            entropy < 256 -> Pair("Strong - High security applications", Color(0xFF2196F3))
            else -> Pair("Excellent - Cryptographic grade", Color(0xFF9C27B0))
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ToolTopBar(
            title = "Token Generator",
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
                        text = "About Random Tokens",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Generate cryptographically secure random tokens using SecureRandom. Ideal for API keys, session tokens, CSRF tokens, and nonce values.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.md))

        // Options Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Token Settings",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Length Slider
                Text(
                    text = "Token Length: $tokenLength bytes${if (selectedFormat == "hex") " (${tokenLength * 2} hex chars)" else ""}",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Slider(
                    value = tokenLength.toFloat(),
                    onValueChange = { tokenLength = it.toInt() },
                    valueRange = 16f..64f,
                    steps = 47,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("16 bytes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("64 bytes", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Format Selection
                Text(
                    text = "Output Format",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                formats.forEach { format ->
                    FilterChip(
                        selected = selectedFormat == format.id,
                        onClick = { selectedFormat = format.id },
                        label = { 
                            Column {
                                Text(format.name, fontWeight = if (selectedFormat == format.id) FontWeight.Bold else FontWeight.Normal)
                                Text(format.description, style = MaterialTheme.typography.labelSmall)
                            }
                        },
                        modifier = Modifier.padding(end = 8.dp, bottom = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Generate Button
                Button(
                    onClick = {
                        isGenerating = true
                        generatedToken = generateToken(tokenLength, selectedFormat)
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
                        Text("Generate Token")
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
                    text = "Generated Token",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (generatedToken.isNotEmpty()) {
                    SelectionContainer {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = generatedToken,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center,
                                maxLines = 4
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Copy button with message
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = {
                                android.content.ClipData.newPlainText("token", generatedToken).also { clipData ->
                                    android.app.ActivityManager.getActivity()?.let { activity ->
                                        (activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager)?.setPrimaryClip(clipData)
                                    }
                                }
                                showCopiedMessage = true
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showCopiedMessage) "Copied!" else "Copy Token")
                        }
                        
                        LaunchedEffect(showCopiedMessage) {
                            if (showCopiedMessage) {
                                kotlinx.coroutines.delay(2000)
                                showCopiedMessage = false
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Divider(modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    // Token Statistics
                    val entropy = calculateEntropy(generatedToken)
                    val (qualityDesc, qualityColor) = getEntropyQuality(entropy)

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Length", value = "${generatedToken.length}")
                        StatItem(label = "Bytes", value = "$tokenLength")
                        StatItem(label = "Format", value = selectedFormat.uppercase().take(5))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Entropy Display
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = qualityColor.copy(alpha = 0.1f)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "🔐 Entropy: %.1f bits".format(entropy),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = qualityColor
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = qualityDesc,
                                style = MaterialTheme.typography.bodySmall,
                                color = qualityColor,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(100.dp),
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

        // Format Reference Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Format Reference",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dm))

                // Hex info
                FormatInfoRow(
                    format = "Hexadecimal",
                    chars = "0-9, a-f",
                    example = "a1b2c3d4e5...",
                    useCase = "API keys, session IDs"
                )

                Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 8.dp))

                // Base64 info
                FormatInfoRow(
                    format = "Base64",
                    chars = "A-Z, a-z, 0-9, +, /",
                    example = "QWxhZGRpbjpvcGVuIHNlc2FtZQ==",
                    useCase = "Data encoding, auth headers"
                )

                Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 8.dp))

                // Base64URL info
                FormatInfoRow(
                    format = "Base64URL",
                    chars = "A-Z, a-z, 0-9, -, _",
                    example = "QWxhZGRpbjpvcGVuIHNlc2FtZQ",
                    useCase = "JWT tokens, URL parameters"
                )

                Divider(modifier = Modifier.fillMaxWidth(), modifier = Modifier.padding(vertical = 8.dp))

                // Alphanumeric info
                FormatInfoRow(
                    format = "Alphanumeric",
                    chars = "A-Z, a-z, 0-9",
                    example = "Ab3xK9mNpQ7...",
                    useCase = "Verification codes, passwords"
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
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
private fun FormatInfoRow(format: String, chars: String, example: String, useCase: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = format,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = useCase,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Text(
            text = "Chars: $chars",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = "Example: $example",
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        )
    }
}
