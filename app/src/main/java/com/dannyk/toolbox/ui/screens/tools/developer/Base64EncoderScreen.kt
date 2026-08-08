package com.dannyk.toolbox.ui.screens.tools.developer

import android.util.Base64
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Check
import androidx.compose.foundation.ScrollState

@Composable
fun Base64EncoderScreen(navController: NavHostController) {
    var plainText by remember { mutableStateOf("") }
    var encodedOutput by remember { mutableStateOf("") }
    var selectedEncoding by remember { mutableStateOf(Base64Encoding.DEFAULT) }
    var showLineBreaks by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "Base64 Encoder",
            subtitle = "Encode text to Base64 format",
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
                    Text("Plain Text Input", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = plainText,
                        onValueChange = { plainText = it },
                        label = { Text("Enter text to encode") },
                        placeholder = { Text("Hello, World!", color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Encoding options
                    Text("Encoding Options", style = MaterialTheme.typography.bodyMedium)

                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Base64Encoding.values().forEach { encoding ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                RadioButton(
                                    selected = selectedEncoding == encoding,
                                    onClick = { selectedEncoding = encoding }
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(encoding.displayName, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }

                    // Line breaks option
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = showLineBreaks,
                            onCheckedChange = { showLineBreaks = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text("Add line breaks (76 chars)", style = MaterialTheme.typography.bodyMedium)
                            Text("For email/MIME compatibility", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    PrimaryButton(
                        text = "Encode to Base64",
                        onClick = {
                            encodedOutput = encodeToBase64(plainText, selectedEncoding, showLineBreaks)
                        },
                        enabled = plainText.isNotEmpty(),
                        icon = Icons.Default.Lock
                    )

                    SecondaryButton(
                        text = "Clear",
                        onClick = {
                            plainText = ""
                            encodedOutput = ""
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .heightIn(min = 80.dp, max = 250.dp),
                            color = Color(0xFF1E1E1E),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Text(
                                text = encodedOutput,
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
                            StatItem("Input", "${plainText.length} chars")
                            StatItem("Output", "${encodedOutput.length} chars")
                            StatItem("Ratio", "${if (plainText.isNotEmpty()) String.format("%.1f", encodedOutput.length.toFloat() / plainText.length * 100) else "0"}%")
                        }
                    }
                }
            }

            // Info card about Base64
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("About Base64 Encoding", style = MaterialTheme.typography.titleSmall)

                    Text(
                        "Base64 is a binary-to-text encoding scheme that represents binary data in an ASCII string format. " +
                        "It's commonly used for:\n\n" +
                        "• Email attachments (MIME)\n" +
                        "• Data URLs in HTML/CSS\n" +
                        "• Storing complex data in JSON/XML\n" +
                        "• Basic authentication headers\n" +
                        "• Embedding images in code",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider()

                    Text("Encoding Types:", style = MaterialTheme.typography.labelMedium)
                    
                    listOf(
                        "Basic" to "Standard A-Z, a-z, 0-9, +, / characters",
                        "URL-Safe" to "Uses - and _ instead of + and / for URL compatibility",
                        "MIME" to "Adds line breaks every 76 characters"
                    ).forEach { (type, desc) ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• $type: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text(desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

enum class Base64Encoding(val displayName: String, val flags: Int) {
    DEFAULT("Default (Standard)", Base64.DEFAULT),
    NO_PADDING("No Padding", Base64.NO_PADDING),
    NO_WRAP("No Wrap (Single line)", Base64.NO_WRAP),
    URL_SAFE("URL Safe", Base64.URL_SAFE or Base64.NO_WRAP),
    MIME("MIME (76 char lines)", Base64.CRLF)
}

private fun encodeToBase64(input: String, encoding: Base64Encoding, addLineBreaks: Boolean): String {
    val bytes = input.toByteArray(Charsets.UTF_8)
    
    return if (addLineBreaks && encoding != Base64Encoding.MIME) {
        // Manual line break implementation
        val encoded = Base64.encodeToString(bytes, encoding.flags or Base64.NO_WRAP)
        encoded.chunked(76).joinToString("\n")
    } else {
        Base64.encodeToString(bytes, encoding.flags)
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold)
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
