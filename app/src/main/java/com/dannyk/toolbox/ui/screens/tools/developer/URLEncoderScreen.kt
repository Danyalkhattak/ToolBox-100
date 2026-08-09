package com.dannyk.toolbox.ui.screens.tools.developer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Link
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
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.ScrollState

@Composable
fun URLEncoderScreen(navController: NavHostController) {
    var plainText by remember { mutableStateOf("") }
    var encodedOutput by remember { mutableStateOf("") }
    var selectedMode by remember { mutableStateOf(UrlEncodeMode.QUERY_PARAM) }
    var encodedChars by remember { mutableStateOf<List<EncodedChar>>(emptyList()) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "URL Encoder",
            subtitle = "Encode text for safe use in URLs",
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
                    Text("Text to Encode", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = plainText,
                        onValueChange = { plainText = it },
                        label = { Text("Enter text or URL component") },
                        placeholder = { Text("hello world?query=test&foo=bar", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Encoding mode selection
                    Text("Encoding Mode", style = MaterialTheme.typography.bodyMedium)

                    UrlEncodeMode.values().forEach { mode ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            RadioButton(
                                selected = selectedMode == mode,
                                onClick = { selectedMode = mode }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(mode.displayName, style = MaterialTheme.typography.bodyMedium)
                                Text(mode.description, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }

                    PrimaryButton(
                        text = "Encode URL",
                        onClick = {
                            val result = encodeUrl(plainText, selectedMode)
                            encodedOutput = result.first
                            encodedChars = result.second
                        },
                        enabled = plainText.isNotEmpty(),
                        icon = Icons.Default.Link
                    )

                    SecondaryButton(
                        text = "Clear",
                        onClick = {
                            plainText = ""
                            encodedOutput = ""
                            encodedChars = emptyList()
                        }
                    )
                }
            }

            // Output Section
            if (encodedOutput.isNotEmpty()) {
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
                            Text("Encoded Output", style = MaterialTheme.typography.titleMedium)
                            
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(encodedOutput))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy to clipboard"
                                )
                            }
                        }

                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1E1E1E),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = encodedOutput,
                                fontFamily = FontFamily.Monospace,
                                fontSize = MaterialTheme.typography.bodySmall.fontSize,
                                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
                            )
                        }

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Original", "${plainText.length} chars")
                            StatItem("Encoded", "${encodedOutput.length} chars")
                            StatItem("Changed", "${encodedChars.size} chars")
                        }
                    }
                }
            }

            // Encoded Characters Detail
            if (encodedChars.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Encoded Characters (${encodedChars.size})", style = MaterialTheme.typography.titleMedium)

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(encodedChars) { char ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.errorContainer
                                    ) {
                                        Text(
                                            char.original,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Icon(
                                        imageVector = Icons.Default.ArrowForward,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            char.encoded,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Text(
                                        char.description,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        modifier = Modifier.fillMaxWidth()
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Character Reference Table
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Commonly Encoded Characters", style = MaterialTheme.typography.titleSmall)
                    
                    Text(
                        "These characters are always encoded in query parameter and form data modes:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val commonEncodings = listOf(
                        Triple("Space", " ", "%20"),
                        Triple("<", "<", "%3C"),
                        Triple(">", ">", "%3E"),
                        Triple("Hash", "#", "%23"),
                        Triple("Pct", "%", "%25"),
                        Triple("LBrace", "{", "%7B"),
                        Triple("RBrace", "}", "%7D"),
                        Triple("Pipe", "|", "%7C"),
                        Triple("BkSlsh", "\\", "%5C"),
                        Triple("Caret", "^", "%5E"),
                        Triple("LBrkt", "[", "%5B"),
                        Triple("RBrkt", "]", "%5D"),
                        Triple("Grave", "`", "%60"),
                        Triple("Quote", "\"", "%22"),
                        Triple("Amp", "&", "%26"),
                        Triple("Plus", "+", "%2B"),
                        Triple("Eqls", "=", "%3D"),
                        Triple("QMark", "?", "%3F"),
                        Triple("Slash", "/", "%2F")
                    )

                    // Display in a compact grid format using Columns (no weight() to avoid crash in scrollable)
                    commonEncodings.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { (name, char, encoded) ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(
                                        modifier = Modifier.padding(6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(char, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelMedium)
                                        Text(encoded, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                        Text(name, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 1)
                                    }
                                }
                            }
                            // Fill remaining space if row is incomplete
                            if (row.size < 3) {
                                repeat(3 - row.size) {
                                    Spacer(modifier = Modifier.fillMaxWidth())
                                }
                            }
                        }
                    }
                }
            }

            // Info card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("About URL Encoding", style = MaterialTheme.typography.titleSmall)
                    
                    Text(
                        "URL encoding (Percent-encoding) converts characters into a format that can be transmitted over the Internet.\n\n" +
                        "• Reserved characters have special meaning in URLs\n" +
                        "• Unsafe characters may be lost or misunderstood\n" +
                        "• Encoding uses % followed by two hexadecimal digits\n" +
                        "• Space can be encoded as + or %20 depending on context",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

enum class UrlEncodeMode(val displayName: String, val description: String) {
    QUERY_PARAM("Query Parameter", "Encodes all special chars including & and ="),
    FORM_DATA("Form Data", "Same as query param, space becomes +"),
    FULL_URI("Full URI Component", "Preserves : / ? # ; , etc.")
}

data class EncodedChar(
    val original: String,
    val encoded: String,
    val position: Int,
    val description: String
)

private fun encodeUrl(input: String, mode: UrlEncodeMode): Pair<String, List<EncodedChar>> {
    return when (mode) {
        UrlEncodeMode.QUERY_PARAM -> {
            val encoded = URLEncoder.encode(input, StandardCharsets.UTF_8.name())
            findEncodedChars(input, encoded)
        }
        UrlEncodeMode.FORM_DATA -> {
            // Form data encoding - space becomes +
            val encoded = URLEncoder.encode(input, StandardCharsets.UTF_8.name())
            findEncodedChars(input, encoded)
        }
        UrlEncodeMode.FULL_URI -> {
            // For full URI, we only encode unsafe characters
            val encoded = input.map { char ->
                when (char) {
                    ' ', '"', '<', '>', '\\', '^', '`', '{', '|', '}' -> 
                        "%" + String.format("%02X", char.code.toByte())
                    else -> char.toString()
                }
            }.joinToString("")
            findEncodedChars(input, encoded)
        }
    }
}

private fun findEncodedChars(original: String, encoded: String): Pair<String, List<EncodedChar>> {
    val encodedChars = mutableListOf<EncodedChar>()
    
    // Simple comparison to find which characters were encoded
    var origIdx = 0
    var encIdx = 0
    
    while (origIdx < original.length && encIdx < encoded.length) {
        if (original[origIdx].toString() == encoded[encIdx].toString()) {
            origIdx++
            encIdx++
        } else {
            // Find what was encoded
            val originalChar = original[origIdx]
            
            // Extract the encoded sequence (%XX)
            if (encIdx + 2 < encoded.length && encoded[encIdx] == '%') {
                val encodedSeq = encoded.substring(encIdx, minOf(encIdx + 3, encoded.length))
                
                val description = when (originalChar) {
                    ' ' -> "Space"
                    '<' -> "Less than"
                    '>' -> "Greater than"
                    '#' -> "Hash/fragment"
                    '%' -> "Percent"
                    '&' -> "Ampersand"
                    '+' -> "Plus"
                    '=' -> "Equals"
                    '?' -> "Question mark"
                    '/' -> "Slash"
                    ':' -> "Colon"
                    ';' -> "Semicolon"
                    '"' -> "Quote"
                    '\'' -> "Apostrophe"
                    else -> "U+${String.format("%04X", originalChar.code)}"
                }
                
                encodedChars.add(EncodedChar(
                    original = originalChar.toString(),
                    encoded = encodedSeq,
                    position = origIdx,
                    description = description
                ))
                
                encIdx += 3
            } else {
                encIdx++
            }
            origIdx++
        }
    }
    
    return Pair(encoded, encodedChars)
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
