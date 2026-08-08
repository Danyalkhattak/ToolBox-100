package com.dannyk.toolbox.ui.screens.tools.developer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FormatAlignLeft
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.*
import org.json.JSONArray
import org.json.JSONObject
import androidx.compose.ui.text.font.FontWeight
import android.content.ClipboardManager
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.pushStyle
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.toAnnotatedString
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.foundation.ScrollState
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign

@Composable
fun JSONFormatterScreen(navController: NavHostController) {
    var jsonInput by remember { mutableStateOf("") }
    var formattedOutput by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var indentSize by remember { mutableStateOf(2) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "JSON Formatter",
            subtitle = "Format, beautify, and minify JSON data",
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
                    Text("Input JSON", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = jsonInput,
                        onValueChange = { jsonInput = it },
                        label = { Text("Paste or type your JSON here") },
                        placeholder = { 
                            Text(
                                "{\"name\": \"John\", \"age\": 30}", 
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 120.dp, max = 200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Indent size selector
                    Text("Indent Size", style = MaterialTheme.typography.bodyMedium)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(2, 4).forEach { size ->
                            FilterChip(
                                selected = indentSize == size,
                                onClick = { indentSize = size },
                                label = { Text("$size spaces") }
                            )
                        }
                    }

                    // Action buttons
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PrimaryButton(
                            text = "Format",
                            onClick = {
                                try {
                                    formattedOutput = formatJson(jsonInput, indentSize)
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Invalid JSON"
                                    formattedOutput = ""
                                }
                            },
                            enabled = jsonInput.isNotBlank(),
                            icon = Icons.Default.AlignHorizontalLeft,
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryButton(
                            text = "Minify",
                            onClick = {
                                try {
                                    formattedOutput = minifyJson(jsonInput)
                                    errorMessage = null
                                } catch (e: Exception) {
                                    errorMessage = e.message ?: "Invalid JSON"
                                    formattedOutput = ""
                                }
                            },
                            enabled = jsonInput.isNotBlank(),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    SecondaryButton(
                        text = "Clear All",
                        onClick = {
                            jsonInput = ""
                            formattedOutput = ""
                            errorMessage = null
                        }
                    )
                }
            }

            // Error Message
            errorMessage?.let { error ->
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
                            Text("JSON Parse Error", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.error)
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(error, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }
            }

            // Output Section
            if (formattedOutput.isNotEmpty()) {
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
                            Text("Formatted Output", style = MaterialTheme.typography.titleMedium)
                            
                            IconButton(onClick = {
                                clipboardManager.setText(AnnotatedString(formattedOutput))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy to clipboard"
                                )
                            }
                        }

                        // Syntax highlighted output
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 150.dp, max = 400.dp),
                            color = Color(0xFF1E1E1E),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = highlightJsonSyntax(formattedOutput),
                                fontFamily = FontFamily.Monospace,
                                fontSize = MaterialTheme.typography.bodyMedium.fontSize,
                                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                                modifier = Modifier.padding(12.dp).verticalScroll(rememberScrollState())
                            )
                        }

                        // Stats
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatItem("Characters", "${formattedOutput.length}")
                            StatItem("Lines", "${formattedOutput.count { it == '\n' } + 1}")
                            StatItem("Original", "${jsonInput.length}")
                        }
                    }
                }
            }

            // Info card
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("About JSON Formatting", style = MaterialTheme.typography.titleSmall)
                    Text(
                        "• Format: Adds proper indentation and line breaks for readability\n" +
                        "• Minify: Removes whitespace to reduce file size\n" +
                        "• Supports nested objects and arrays\n" +
                        "• Validates JSON structure during formatting",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// Format JSON with specified indentation
private fun formatJson(json: String, indent: Int): String {
    val trimmed = json.trim()
    
    return when {
        trimmed.startsWith("{") -> JSONObject(trimmed).toString(indent)
        trimmed.startsWith("[") -> JSONArray(trimmed).toString(indent)
        else -> throw IllegalArgumentException("Input must be a valid JSON object or array")
    }
}

// Minify JSON (remove unnecessary whitespace)
private fun minifyJson(json: String): String {
    val trimmed = json.trim()
    
    return when {
        trimmed.startsWith("{") -> JSONObject(trimmed).toString()
        trimmed.startsWith("[") -> JSONArray(trimmed).toString()
        else -> throw IllegalArgumentException("Input must be a valid JSON object or array")
    }
}

// Simple syntax highlighting for JSON display
private fun highlightJsonSyntax(json: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        while (i < json.length) {
            when (json[i]) {
                '{', '}' -> {
                    pushStyle(SpanStyle(color = Color(0xFFCE9178)))
                    append(json[i])
                    pop()
                }
                '[', ']' -> {
                    pushStyle(SpanStyle(color = Color(0xFFFFD700)))
                    append(json[i])
                    pop()
                }
                ':' -> {
                    pushStyle(SpanStyle(color = Color(0xFFFFFFFF)))
                    append(json[i])
                    pop()
                }
                ',' -> {
                    pushStyle(SpanStyle(color = Color(0xFFFFFFFF)))
                    append(json[i])
                    pop()
                }
                '"' -> {
                    // Find the end of the string
                    pushStyle(SpanStyle(color = Color(0xFF6A9955)))
                    append('"')
                    i++
                    while (i < json.length && json[i] != '"') {
                        if (json[i] == '\\') {
                            append(json[i])
                            i++
                            if (i < json.length) {
                                append(json[i])
                            }
                        } else {
                            append(json[i])
                        }
                        i++
                    }
                    if (i < json.length) {
                        append('"')
                    }
                    pop()
                }
                in '0'..'9', '-' -> {
                    pushStyle(SpanStyle(color = Color(0xFFB5CEA8)))
                    while (i < json.length && (json[i].isDigit() || json[i] == '.' || json[i] == '-' || json[i] == '+' || json[i] == 'e' || json[i] == 'E')) {
                        append(json[i])
                        i++
                    }
                    pop()
                    continue
                }
                ' ', '\t', '\n', '\r' -> {
                    pushStyle(SpanStyle(color = Color(0xFF808080)))
                    append(json[i])
                    pop()
                }
                else -> {
                    append(json[i])
                }
            }
            i++
        }
    }
}
