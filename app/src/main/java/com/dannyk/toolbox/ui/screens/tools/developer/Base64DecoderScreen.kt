package com.dannyk.toolbox.ui.screens.tools.developer

import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import androidx.compose.material3.Divider
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import java.util.regex.Pattern
import androidx.compose.foundation.ScrollState

@Composable
fun Base64DecoderScreen(navController: NavHostController) {
    var base64Input by remember { mutableStateOf("") }
    var decodedOutput by remember { mutableStateOf("") }
    var decodeError by remember { mutableStateOf<String?>(null) }
    var byteInfo by remember { mutableStateOf<ByteInfo?>(null) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Base64 Decoder",
            subtitle = "Decode Base64 encoded data back to plain text",
            onBack = { navController.navigateUp() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Input Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Base64 Input", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = base64Input,
                        onValueChange = { 
                            base64Input = it
                            decodeError = null
                        },
                        label = { Text("Enter Base64 string to decode") },
                        placeholder = { 
                            Text(
                                "SGVsbG8sIFdvcmxkIQ==", 
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    PrimaryButton(
                        text = "Decode Base64",
                        onClick = {
                            try {
                                val result = decodeFromBase64(base64Input)
                                decodedOutput = result.first
                                byteInfo = result.second
                                decodeError = null
                            } catch (e: Exception) {
                                decodedOutput = ""
                                byteInfo = null
                                decodeError = e.message ?: "Failed to decode Base64"
                            }
                        },
                        enabled = base64Input.isNotBlank(),
                        icon = Icons.Default.LockOpen
                    )

                    SecondaryButton(
                        text = "Clear",
                        onClick = {
                            base64Input = ""
                            decodedOutput = ""
                            decodeError = null
                            byteInfo = null
                        }
                    )
                }
            }

            // Error Display
            decodeError?.let { error ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text("Decoding Error", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                // Suggestions for common errors
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("Possible fixes:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                        
                        listOf(
                            "Remove any whitespace or newlines from input",
                            "Check for URL-safe characters (-, _ vs +, /)",
                            "Ensure proper padding (= at end)",
                            "Verify the input is valid Base64"
                        ).forEach { suggestion ->
                            Text("• $suggestion", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }

            // Output Section
            if (decodedOutput.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Decoded Output", style = MaterialTheme.typography.titleMedium)
                            
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(decodedOutput))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy to clipboard"
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp, max = 250.dp),
                            color = Color(0xFF1E1E1E),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = decodedOutput,
                                fontFamily = FontFamily.Monospace,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight,
                                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
                            )
                        }

                        // Stats row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Input", "${base64Input.replace("\\s".toRegex(), "").length} chars")
                            StatItem("Output", "${decodedOutput.length} chars")
                            StatItem("Ratio", "${if (base64Input.isNotEmpty()) String.format("%.1f", decodedOutput.length.toFloat() / base64Input.replace("\\s".toRegex(), "").length * 100) else "0"}%")
                        }
                    }
                }
            }

            // Byte Info Section
            byteInfo?.let { info ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Byte Information", style = MaterialTheme.typography.titleMedium)

                        InfoRow("Total Bytes", "${info.byteCount}")
                        InfoRow("Is Valid UTF-8", if (info.isUtf8Text) "Yes" else "No")
                        InfoRow("Encoding Detected", info.detectedEncoding)
                        
                        if (!info.isUtf8Text && info.nonPrintableCount > 0) {
                            InfoRow("Non-printable Chars", "${info.nonPrintableCount}")
                            InfoRow("Content Type", "Binary Data")
                            
                            Text(
                                "Note: This appears to be binary or non-text data. The displayed output shows the UTF-8 interpretation.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.tertiary
                            )
                        } else {
                            InfoRow("Content Type", "Text Data")
                        }

                        // Byte preview (hex dump)
                        if (info.hexPreview.isNotEmpty()) {
                            Text("Hex Preview:", style = MaterialTheme.typography.labelMedium)
                            Surface(
                                color = Color.Black.copy(alpha = 0.9f),
                                shape = MaterialTheme.shapes.small,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = info.hexPreview,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = MaterialTheme.typography.labelSmall.fontSize,
                                    color = Color.Green,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Info card about Base64 decoding
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("About Base64 Decoding", style = MaterialTheme.typography.titleSmall)

                    Text(
                        "Base64 decoding reverses the encoding process, converting ASCII-safe strings back to their original binary form.\n\n" +
                        "Common use cases:\n" +
                        "• Decoding API responses\n" +
                        "• Reading email attachments\n" +
                        "• Processing authentication tokens\n" +
                        "• Extracting embedded resources",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider()

                    Text("Supported Formats:", style = MaterialTheme.typography.labelMedium)

                    listOf(
                        "Standard Base64" to "A-Z, a-z, 0-9, +, / with = padding",
                        "URL-Safe Base64" to "Uses - and _ instead of + and /",
                        "With/Without padding" to "Handles both padded and unpadded input",
                        "With line breaks" to "Ignores whitespace automatically"
                    ).forEach { (format, desc) ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• $format: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

data class ByteInfo(
    val byteCount: Int,
    val isUtf8Text: Boolean,
    val detectedEncoding: String,
    val nonPrintableCount: Int,
    val hexPreview: String
)

private fun decodeFromBase64(input: String): Pair<String, ByteInfo> {
    // Clean up input - remove whitespace
    val cleanedInput = input.replace("\\s".toRegex(), "")
    
    if (cleanedInput.isEmpty()) {
        throw IllegalArgumentException("Empty input after removing whitespace")
    }

    // Validate Base64 characters
    val validBase64Pattern = Regex("^[A-Za-z0-9+/\\-_]*={0,2}$")
    if (!validBase64Pattern.matches(cleanedInput)) {
        throw IllegalArgumentException("Invalid Base64 characters detected in input")
    }

    try {
        // Try standard decoding first, then URL-safe
        val bytes = try {
            Base64.decode(cleanedInput, Base64.DEFAULT)
        } catch (e: IllegalArgumentException) {
            Base64.decode(cleanedInput, Base64.URL_SAFE)
        }

        // Analyze the decoded bytes
        val byteInfo = analyzeBytes(bytes)
        
        // Convert to string (may contain non-UTF-8 characters)
        val decodedString = try {
            String(bytes, Charsets.UTF_8)
        } catch (e: Exception) {
            // Fallback: show hex representation
            bytes.joinToString("") { "%02X".format(it) }
        }

        return Pair(decodedString, byteInfo)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Invalid Base64 format: ${e.message}")
    }
}

private fun analyzeBytes(bytes: ByteArray): ByteInfo {
    val byteCount = bytes.size
    
    // Check if all bytes are valid UTF-8 printable text
    var isUtf8Text = true
    var nonPrintableCount = 0
    
    for (byte in bytes) {
        val charValue = byte.toInt() and 0xFF
        // Check for control characters (except common whitespace)
        if (charValue < 32 && charValue !in listOf(9, 10, 13)) { // Tab, LF, CR are OK
            nonPrintableCount++
            isUtf8Text = false
        } else if (charValue > 126 && charValue < 160) {
            // Some extended ASCII that might not be valid UTF-8
            nonPrintableCount++
        }
    }

    // Detect content type/encoding
    val detectedEncoding = when {
        byteCount >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() -> "UTF-8 with BOM"
        byteCount >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() -> "UTF-16 LE"
        byteCount >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() -> "UTF-16 BE"
        isUtf8Text -> "UTF-8 Text"
        else -> "Binary/Unknown"
    }

    // Generate hex preview (first 32 bytes)
    val hexPreview = bytes.take(32).joinToString(" ") { "%02X".format(it) } + 
            if (bytes.size > 32) "..." else ""

    return ByteInfo(
        byteCount = byteCount,
        isUtf8Text = isUtf8Text,
        detectedEncoding = detectedEncoding,
        nonPrintableCount = nonPrintableCount,
        hexPreview = hexPreview
    )
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
    }
}
