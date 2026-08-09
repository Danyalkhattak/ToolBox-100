package com.dannyk.toolbox.ui.screens.tools.developer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.LinkOff
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
import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.foundation.ScrollState

@Composable
fun URLDecoderScreen(navController: NavHostController) {
    var encodedInput by remember { mutableStateOf("") }
    var decodedOutput by remember { mutableStateOf("") }
    var decodeError by remember { mutableStateOf<String?>(null) }
    var plusAsSpace by remember { mutableStateOf(true) }
    var decodedSequences by remember { mutableStateOf<List<DecodedSequence>>(emptyList()) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "URL Decoder",
            subtitle = "Decode percent-encoded URL strings",
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
                    Text("Encoded URL Input", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = encodedInput,
                        onValueChange = { 
                            encodedInput = it
                            decodeError = null
                        },
                        label = { Text("Enter URL-encoded string") },
                        placeholder = { 
                            Text(
                                "hello%20world%3Fquery%3Dtest", 
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Options
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = plusAsSpace,
                            onCheckedChange = { plusAsSpace = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Decode + as space", style = MaterialTheme.typography.bodyMedium)
                            Text("For form data compatibility", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    PrimaryButton(
                        text = "Decode URL",
                        onClick = {
                            try {
                                val result = decodeUrl(encodedInput, plusAsSpace)
                                decodedOutput = result.first
                                decodedSequences = result.second
                                decodeError = null
                            } catch (e: Exception) {
                                decodedOutput = ""
                                decodedSequences = emptyList()
                                decodeError = e.message ?: "Failed to decode URL"
                            }
                        },
                        enabled = encodedInput.isNotBlank(),
                        icon = Icons.Default.LinkOff
                    )

                    SecondaryButton(
                        text = "Clear",
                        onClick = {
                            encodedInput = ""
                            decodedOutput = ""
                            decodedSequences = emptyList()
                            decodeError = null
                        }
                    )

                    // Quick examples
                    Text("Quick Examples:", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                    
                    listOf(
                        "%20" to "Space",
                        "%3C%3E" to "< and >",
                        "%26" to "Ampersand",
                        "hello+world" to "Plus as space"
                    ).forEach { (example, desc) ->
                        Surface(
                            shape = MaterialTheme.shapes.small,
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            onClick = { encodedInput = example }
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(example, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("($desc)", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
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
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1E1E1E),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = decodedOutput,
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
                            StatItem("Encoded", "${encodedInput.length} chars")
                            StatItem("Decoded", "${decodedOutput.length} chars")
                            StatItem("Sequences", "${decodedSequences.size}")
                        }
                    }
                }
            }

            // Decoded Sequences Detail
            if (decodedSequences.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text("Decoded Sequences (${decodedSequences.size})", style = MaterialTheme.typography.titleMedium)

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 200.dp)
                        ) {
                            items(decodedSequences) { seq ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        shape = MaterialTheme.shapes.small,
                                        color = MaterialTheme.colorScheme.primaryContainer
                                    ) {
                                        Text(
                                            seq.encoded,
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
                                        color = MaterialTheme.colorScheme.tertiaryContainer
                                    ) {
                                        Text(
                                            seq.decoded,
                                            fontFamily = FontFamily.Monospace,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                    
                                    Text(
                                        seq.description,
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

            // Percent Encoding Reference Table
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("Percent-Encoding Reference", style = MaterialTheme.typography.titleSmall)

                    Text(
                        "Common %XX sequences and their decoded characters:",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    val referenceTable = listOf(
                        "%20" to "Space",
                        "%21" to "!",
                        "%22" to "\"",
                        "%23" to "#",
                        "%24" to "$",
                        "%25" to "%",
                        "%26" to "&",
                        "%27" to "'",
                        "%28" to "(",
                        "%29" to ")",
                        "%2A" to "*",
                        "%2B" to "+",
                        "%2C" to ",",
                        "%2F" to "/",
                        "%3A" to ":",
                        "%3B" to ";",
                        "%3C" to "<",
                        "%3D" to "=",
                        "%3E" to ">",
                        "%3F" to "?",
                        "%40" to "@",
                        "%5B" to "[",
                        "%5C" to "\\",
                        "%5D" to "]",
                        "%5E" to "^",
                        "%60" to "`",
                        "%7B" to "{",
                        "%7C" to "|",
                        "%7D" to "}",
                        "%7E" to "~"
                    )

                    // Display in a compact grid format (no weight() to avoid crash in scrollable)
                    referenceTable.chunked(4).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            row.forEach { (encoded, char) ->
                                Surface(
                                    modifier = Modifier.fillMaxWidth(),
                                    shape = MaterialTheme.shapes.small,
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Column(
                                        modifier = Modifier.padding(5.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(char, style = MaterialTheme.typography.labelMedium)
                                        Text(encoded, fontFamily = FontFamily.Monospace, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
                                    }
                                }
                            }
                            // Fill remaining space if row is incomplete
                            if (row.size < 4) {
                                repeat(4 - row.size) {
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
                    Text("About URL Decoding", style = MaterialTheme.typography.titleSmall)
                    
                    Text(
                        "URL decoding reverses percent-encoding, converting %XX sequences back to their original characters.\n\n" +
                        "• Each %XX represents one byte in hexadecimal\n" +
                        "• UTF-8 characters may use multiple sequences\n" +
                        "• + is often used for spaces in query strings\n" +
                        "• Double-decoding can cause issues - only decode once!",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

data class DecodedSequence(
    val encoded: String,
    val decoded: String,
    val position: Int,
    val description: String
)

private fun decodeUrl(input: String, treatPlusAsSpace: Boolean): Pair<String, List<DecodedSequence>> {
    // First handle + as space if option is enabled
    val processedInput = if (treatPlusAsSpace) input.replace("+", " ") else input
    
    // Find all %XX sequences before decoding
    val sequences = mutableListOf<DecodedSequence>()
    val pattern = Regex("%[0-9A-Fa-f]{2}")
    
    pattern.findAll(processedInput).forEach { match ->
        val hexStr = match.value.substring(1)
        try {
            val byteValue = hexStr.toInt(16).toByte()
            val decodedChar = Char(byteValue.toInt() and 0xFF)
            
            val description = when (decodedChar) {
                ' ' -> "Space"
                '!' -> "Exclamation mark"
                '"' -> "Double quote"
                '#' -> "Hash/Pound sign"
                '$' -> "Dollar sign"
                '%' -> "Percent"
                '&' -> "Ampersand"
                '\'' -> "Single quote/Apostrophe"
                '(' -> "Left parenthesis"
                ')' -> "Right parenthesis"
                '*' -> "Asterisk"
                '+' -> "Plus sign"
                ',' -> "Comma"
                '/' -> "Slash/Fwd slash"
                ':' -> "Colon"
                ';' -> "Semicolon"
                '<' -> "Less than"
                '=' -> "Equals sign"
                '>' -> "Greater than"
                '?' -> "Question mark"
                '@' -> "At sign"
                '[' -> "Left bracket"
                '\\' -> "Backslash"
                ']' -> "Right bracket"
                '^' -> "Caret/Circumflex"
                '`' -> "Backtick/Grave accent"
                '{' -> "Left brace"
                '|' -> "Pipe/Vertical bar"
                '}' -> "Right brace"
                '~' -> "Tilde"
                '\n' -> "Newline"
                '\r' -> "Carriage return"
                '\t' -> "Tab"
                else -> if (decodedChar.isLetterOrDigit()) "$decodedChar" else "U+${String.format("%04X", decodedChar.code)}"
            }
            
            sequences.add(DecodedSequence(
                encoded = match.value,
                decoded = decodedChar.toString(),
                position = match.range.first,
                description = description
            ))
        } catch (e: Exception) {
            // Invalid hex sequence, skip
        }
    }
    
    // Perform actual decoding using Java's URLDecoder
    val decoded = try {
        URLDecoder.decode(processedInput, StandardCharsets.UTF_8.name())
    } catch (e: Exception) {
        // Fallback: manual decoding
        manualDecode(processedInput)
    }
    
    return Pair(decoded, sequences)
}

// Manual fallback decoder for edge cases
private fun manualDecode(input: String): String {
    val result = StringBuilder()
    var i = 0
    
    while (i < input.length) {
        when {
            input[i] == '%' && i + 2 < input.length -> {
                val hex = input.substring(i + 1, i + 3)
                try {
                    result.append(hex.toInt(16).toChar())
                    i += 3
                } catch (e: Exception) {
                    result.append(input[i])
                    i++
                }
            }
            else -> {
                result.append(input[i])
                i++
            }
        }
    }
    
    return result.toString()
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
