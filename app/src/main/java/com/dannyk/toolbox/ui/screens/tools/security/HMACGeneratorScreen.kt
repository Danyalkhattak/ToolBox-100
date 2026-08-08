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
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HMACGeneratorScreen(navController: NavHostController) {
    var message by remember { mutableStateOf("") }
    var secretKey by remember { mutableStateOf("") }
    var selectedAlgorithm by remember { mutableStateOf("HmacSHA256") }
    var hmacResult by remember { mutableStateOf("") }
    var isGenerating by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf(false) }
    var showKeyVisibility by remember { mutableStateOf(false) }

    val algorithms = listOf(
        "HmacSHA256" to "SHA-256 (Recommended)",
        "HmacSHA1" to "SHA-1 (Deprecated)",
        "HmacMD5" to "MD5 (Insecure)"
    )

    fun computeHMAC(msg: String, key: String, algorithm: String): String {
        return try {
            val mac = Mac.getInstance(algorithm)
            val secretKeySpec = SecretKeySpec(key.toByteArray(Charsets.UTF_8), algorithm)
            mac.init(secretKeySpec)
            val hmacBytes = mac.doFinal(msg.toByteArray(Charsets.UTF_8))
            hmacBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    // Get output info based on algorithm
    fun getAlgorithmInfo(): Triple<Int, Int, Color> {
        return when (selectedAlgorithm) {
            "HmacSHA256" -> Triple(64, 256, Color(0xFF4CAF50)) // Green - secure
            "HmacSHA1" -> Triple(40, 160, Color(0xFFFF9800))   // Orange - deprecated
            "HmacMD5" -> Triple(32, 128, Color(0xFFFF5252))     // Red - insecure
            else -> Triple(0, 0, Color.Gray)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ToolTopBar(
            title = "HMAC Generator",
            onBackClick = { navController.navigateUp() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Info Card about HMAC
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
                        text = "About HMAC",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "HMAC (Hash-based Message Authentication Code) uses a cryptographic hash function and a secret key. It verifies both data integrity and authenticity of a message.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Input Section Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Message Input",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter message to authenticate...") },
                    shape = RoundedCornerShape(12.dp),
                    minLines = 3,
                    maxLines = 6
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "${message.length} characters | ${message.toByteArray(Charsets.UTF_8).size} bytes",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Secret Key Input
                Text(
                    text = "Secret Key",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = secretKey,
                    onValueChange = { secretKey = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Enter your secret key...") },
                    shape = RoundedCornerShape(12.dp),
                    visualTransformation = if (showKeyVisibility) 
                        androidx.compose.ui.text.input.VisualTransformation.None 
                    else 
                        androidx.compose.ui.text.input.PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showKeyVisibility = !showKeyVisibility }) {
                            Icon(
                                imageVector = if (showKeyVisibility) 
                                    Icons.Default.Visibility 
                                else 
                                    Icons.Default.VisibilityOff,
                                contentDescription = if (showKeyVisibility) "Hide key" else "Show key"
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${secretKey.length} characters | ${secretKey.toByteArray(Charsets.UTF_8).size} bytes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    TextButton(onClick = { secretKey = "" }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Algorithm Selector
                Text(
                    text = "Hash Algorithm",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Algorithm dropdown or chips
        algorithms.forEachIndexed { index, (algo, desc) ->
            val isSelected = selectedAlgorithm == algo
            val chipColor = when (algo) {
                "HmacSHA256" -> if (isSelected) Color(0xFF4CAF50) else Color(0xFF4CAF50).copy(alpha = 0.2f)
                "HmacSHA1" -> if (isSelected) Color(0xFFFF9800) else Color(0xFFFF9800).copy(alpha = 0.2f)
                "HmacMD5" -> if (isSelected) Color(0xFFFF5252) else Color(0xFFFF5252).copy(alpha = 0.2f)
                else -> MaterialTheme.colorScheme.primary
            }
            
            FilterChip(
                selected = isSelected,
                onClick = { selectedAlgorithm = algo },
                label = { 
                    Column {
                        Text(algo.removePrefix("Hmac"), fontWeight = FontWeight.Bold)
                        Text(desc, style = MaterialTheme.typography.labelSmall)
                    }
                },
                modifier = Modifier.padding(end = if (index < algorithms.lastIndex) 8.dp else 0.dp)
            )
            if (index < algorithms.lastIndex - 1) Spacer(modifier = Modifier.width(8.dp))
        }

                Spacer(modifier = Modifier.height(20.dp))

                // Generate Button
                Button(
                    onClick = {
                        isGenerating = true
                        hmacResult = computeHMAC(message, secretKey, selectedAlgorithm)
                        isGenerating = false
                    },
                    enabled = message.isNotEmpty() && secretKey.isNotEmpty() && !isGenerating,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = getAlgorithmInfo().third
                    )
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Generate HMAC")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Result Section Card
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
                    text = "HMAC Result (${selectedAlgorithm.removePrefix("Hmac")})",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (hmacResult.isNotEmpty() && !hmacResult.startsWith("Error")) {
                    SelectionContainer {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = hmacResult,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Medium
                                ),
                                modifier = Modifier.padding(16.dp),
                                textAlign = TextAlign.Center
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
                                android.content.ClipData.newPlainText("hmac_result", hmacResult).also { clipData ->
                                    android.app.ActivityManager.getActivity()?.let { activity ->
                                        (activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager)?.setPrimaryClip(clipData)
                                    }
                                }
                                showCopiedMessage = true
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showCopiedMessage) "Copied!" else "Copy HMAC")
                        }
                        
                        LaunchedEffect(showCopiedMessage) {
                            if (showCopiedMessage) {
                                kotlinx.coroutines.delay(2000)
                                showCopiedMessage = false
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Hash Info
                    Divider(modifier = Modifier.fillMaxWidth())

                    Spacer(modifier = Modifier.height(16.dp))

                    val (hexLen, bits, _) = getAlgorithmInfo()

                    // Hash Statistics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Hex Length", value = "$hexLen")
                        StatItem(label = "Bits", value = "$bits")
                        StatItem(label = "Bytes", value = "${hexLen / 2}")
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Security indicator based on algorithm
                    val securityStatus = when (selectedAlgorithm) {
                        "HmacSHA256" -> "✓ Secure" to Color(0xFF4CAF50)
                        "HmacSHA1" -> "⚠ Deprecated" to Color(0xFFFF9800)
                        "HmacMD5" -> "🚫 Insecure" to Color(0xFFFF5252)
                        else -> "" to Color.Gray
                    }
                    
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = securityStatus.second.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = securityStatus.first,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = securityStatus.second,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                } else if (hmacResult.startsWith("Error")) {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = Color(0xFFFFEBEE)
                    ) {
                        Text(
                            text = hmacResult,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFD32F2F),
                            modifier = Modifier.padding(16.dp),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Enter message and key, then click Generate",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.md))

        // Technical Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "How HMAC Works",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dm))

                // HMAC Formula
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Text(
                        text = "HMAC(K, m) = H((K' ⊕ opad) || H((K' ⊕ ipad) || m))",
                        style = MaterialTheme.typography.bodyMedium.copy(fontFamily = FontFamily.Monospace),
                        modifier = Modifier.padding(12.dp),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Where:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                HmacExplanationItem(symbol = "K", description = "Secret key")
                HmacExplanationItem(symbol = "m", description = "Message")
                HmacExplanationItem(symbol = "H", description = "Hash function")
                HmacExplanationItem(symbol = "K'", description = "Key padded/derived from K")
                HmacExplanationItem(symbol = "⊕", description = "XOR operation")
                HmacExplanationItem(symbol = "opad", description = "Outer padding (0x5c...)")
                HmacExplanationItem(symbol = "ipad", description = "Inner padding (0x36...)")
                HmacExplanationItem(symbol = "||", description = "Concatenation")

                Spacer(modifier = Modifier.height(12.dp))
                
                Divider(modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "Common Use Cases:",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                UseCaseItem("API authentication & request signing")
                UseCaseItem("JWT token validation")
                UseCaseItem("Data integrity verification")
                UseCaseItem("Digital signatures (with additional steps)")
                UseCaseItem("Message authentication in TLS")
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
private fun HmacExplanationItem(symbol: String, description: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = symbol,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            ),
            minWidth = 40.dp,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "= $description",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun UseCaseItem(useCase: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "• ",
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = useCase,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
