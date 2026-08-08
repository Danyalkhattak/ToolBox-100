package com.dannyk.toolbox.ui.screens.tools.developer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Fingerprint
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
import java.util.UUID
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import androidx.compose.material3.Divider

@Composable
fun UUIDGeneratorScreen(navController: NavHostController) {
    var singleUuid by remember { mutableStateOf("") }
    var bulkUuids by remember { mutableStateOf<List<String>>(emptyList()) }
    var bulkCount by remember { mutableStateOf("5") }
    var useUppercase by remember { mutableStateOf(false) }
    var removeDashes by remember { mutableStateOf(false) }
    val clipboardManager = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        ToolHeader(
            title = "UUID Generator",
            subtitle = "Generate unique identifiers (UUID v4)",
            onBack = { navController.navigateUp() }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Single UUID Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Generate Single UUID", style = MaterialTheme.typography.titleMedium)

                    if (singleUuid.isNotEmpty()) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            color = Color(0xFF1E1E1E),
                            shape = MaterialTheme.shapes.medium
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = formatUuid(singleUuid, useUppercase, removeDashes),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize,
                                    modifier = Modifier.weight(1f).horizontalScroll(rememberScrollState())
                                )
                                
                                IconButton(onClick = {
                                    clipboardManager.setText(AnnotatedString(formatUuid(singleUuid, useUppercase, removeDashes)))
                                }) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy to clipboard"
                                    )
                                }
                            }
                        }

                        // UUID info
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            InfoChip("Version", "4")
                            InfoChip("Variant", "RFC 4122")
                            InfoChip("Length", "${formatUuid(singleUuid, useUppercase, removeDashes).length}")
                        }
                    }

                    PrimaryButton(
                        text = "Generate UUID",
                        onClick = {
                            singleUuid = UUID.randomUUID().toString()
                        },
                        icon = Icons.Default.Fingerprint
                    )
                }
            }

            // Bulk Generation Section
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Bulk Generation", style = MaterialTheme.typography.titleMedium)

                    OutlinedTextField(
                        value = bulkCount,
                        onValueChange = { 
                            if (it.isEmpty() || (it.matches(Regex("\\d*")) && it.toIntOrNull() in 1..100)) {
                                bulkCount = it 
                            }
                        },
                        label = { Text("Number of UUIDs (1-100)") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                        )
                    )

                    // Format options
                    Text("Format Options", style = MaterialTheme.typography.bodyMedium)

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = useUppercase,
                            onCheckedChange = { useUppercase = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Uppercase", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Switch(
                            checked = removeDashes,
                            onCheckedChange = { removeDashes = it }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Remove dashes (32 chars)", style = MaterialTheme.typography.bodyMedium)
                    }

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        PrimaryButton(
                            text = "Generate Bulk",
                            onClick = {
                                val count = bulkCount.toIntOrNull() ?: 5
                                bulkUuids = (1..count.coerceIn(1, 100)).map { 
                                    UUID.randomUUID().toString() 
                                }
                            },
                            icon = Icons.Default.Fingerprint,
                            modifier = Modifier.weight(1f)
                        )

                        SecondaryButton(
                            text = "Copy All",
                            onClick = {
                                val allUuids = bulkUuids.joinToString("\n") { formatUuid(it, useUppercase, removeDashes) }
                                clipboardManager.setText(AnnotatedString(allUuids))
                            },
                            enabled = bulkUuids.isNotEmpty(),
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Bulk Results
            if (bulkUuids.isNotEmpty()) {
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
                            Text("Generated UUIDs (${bulkUuids.size})", style = MaterialTheme.typography.titleMedium)
                            
                            TextButton(onClick = {
                                val allUuids = bulkUuids.joinToString("\n") { formatUuid(it, useUppercase, removeDashes) }
                                clipboardManager.setText(AnnotatedString(allUuids))
                            }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy All")
                            }
                        }

                        LazyColumn(
                            modifier = Modifier.heightIn(max = 300.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            items(bulkUuids) { uuid ->
                                Surface(
                                    shape = MaterialTheme.shapes.small,
                                    color = Color.Black.copy(alpha = 0.9f),
                                    onClick = {
                                        clipboardManager.setText(AnnotatedString(formatUuid(uuid, useUppercase, removeDashes)))
                                    }
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(horizontal = 12.dp, vertical = 8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = formatUuid(uuid, useUppercase, removeDashes),
                                            fontFamily = FontFamily.Monospace,
                                            color = Color.White,
                                            modifier = Modifier.weight(1f)
                                        )
                                        
                                        Icon(
                                            imageVector = Icons.Default.ContentCopy,
                                            contentDescription = "Copy",
                                            tint = Color.Gray.copy(alpha = 0.7f),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // UUID Version Info
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("About UUID v4", style = MaterialTheme.typography.titleSmall)

                    Text(
                        "Universally Unique Identifier version 4 is a randomly generated identifier.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Divider()

                    listOf(
                        "Format" to "xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx (36 chars)",
                        "Version" to "4 (random)",
                        "Variant" to "RFC 4122 (Leach-Salz)",
                        "Uniqueness" to "Extremely high probability of uniqueness",
                        "Bits" to "128 bits total (122 random + 6 fixed)"
                    ).forEach { (label, value) ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(label, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text(value, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, fontFamily = FontFamily.Monospace)
                        }
                    }
                }
            }

            // UUID Structure Visualization
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("UUID Structure", style = MaterialTheme.typography.titleSmall)

                    if (singleUuid.isNotEmpty()) {
                        val uuid = formatUuid(singleUuid, false, false)
                        
                        // Visual breakdown
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(2.dp)
                        ) {
                            // time_low (8 hex)
                            UuidSegment(uuid.substring(0, 8), "time_low", MaterialTheme.colorScheme.primaryContainer)
                            Text("-", style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
                            // time_mid (4 hex)
                            UuidSegment(uuid.substring(9, 13), "time_mid", MaterialTheme.colorScheme.secondaryContainer)
                            Text("-", style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
                            // time_hi_and_version (4 hex)
                            UuidSegment(uuid.substring(14, 18), "ver", MaterialTheme.colorScheme.tertiaryContainer)
                            Text("-", style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
                            // clock_seq_hi_and_res clock_seq_low (4 hex)
                            UuidSegment(uuid.substring(19, 23), "variant", MaterialTheme.colorScheme.errorContainer)
                            Text("-", style = MaterialTheme.typography.bodyLarge, fontFamily = FontFamily.Monospace)
                            // node (12 hex)
                            UuidSegment(uuid.substring(24, 36), "node", MaterialTheme.colorScheme.surfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        // Legend
                        listOf(
                            Pair("time_low", "32-bit timestamp (random in v4)", MaterialTheme.colorScheme.primaryContainer),
                            Pair("time_mid", "16-bit timestamp (random in v4)", MaterialTheme.colorScheme.secondaryContainer),
                            Pair("ver", "4-bit version (always 4)", MaterialTheme.colorScheme.tertiaryContainer),
                            Pair("variant", "1-3 bit variant + clock seq", MaterialTheme.colorScheme.errorContainer),
                            Pair("node", "48-bit node ID (random)", MaterialTheme.colorScheme.surfaceVariant)
                        ).forEach { (name, desc, color) ->
                            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                Surface(shape = MaterialTheme.shapes.extraSmall, color = color) {
                                    Text(name, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(desc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    } else {
                        Text("Generate a UUID to see its structure breakdown", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            // Use Cases
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Common Use Cases", style = MaterialTheme.typography.titleSmall)
                    
                    listOf(
                        "Database primary keys" to "Unique row identifiers",
                        "Session tokens" to "User session management",
                        "Request tracking" to "Correlate API requests",
                        "File naming" to "Avoid collisions in uploads",
                        "Distributed systems" to "Generate IDs without coordination",
                        "API resource IDs" to "Public-facing unique identifiers"
                    ).forEach { (useCase, description) ->
                        Row(modifier = Modifier.padding(vertical = 2.dp)) {
                            Text("• $useCase: ", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
                            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UuidSegment(value: String, label: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(color = color, shape = MaterialTheme.shapes.small) {
            Text(
                value,
                fontFamily = FontFamily.Monospace,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
            )
        }
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun InfoChip(label: String, value: String) {
    Surface(
        shape = MaterialTheme.shapes.small,
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
    }
}

// Format UUID based on options
private fun formatUuid(uuid: String, uppercase: Boolean, noDashes: String): String {
    var formatted = uuid
    
    if (uppercase) {
        formatted = formatted.uppercase()
    }
    
    if (noDashes) {
        formatted = formatted.replace("-", "")
    }
    
    return formatted
}
