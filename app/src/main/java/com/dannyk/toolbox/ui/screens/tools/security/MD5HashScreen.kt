package com.dannyk.toolbox.ui.screens.tools.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Dangerous
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
import java.security.MessageDigest
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.filled.Warning

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MD5HashScreen(navController: NavHostController) {
    var inputText by remember { mutableStateOf("") }
    var hashResult by remember { mutableStateOf("") }
    var isHashing by remember { mutableStateOf(false) }
    var showCopiedMessage by remember { mutableStateOf(false) }

    fun computeMD5(text: String): String {
        return try {
            val digest = MessageDigest.getInstance("MD5")
            val bytes = text.toByteArray(Charsets.UTF_8)
            val hashBytes = digest.digest(bytes)
            hashBytes.joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            "Error: ${e.message}"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ToolTopBar(
            title = "MD5 Hash",
            onBackClick = { navController.navigateUp() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Critical Security Warning Card - MD5 Collision Vulnerability
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFEBEE)) // Red warning color
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        Icons.Default.Dangerous,
                        contentDescription = null,
                        tint = Color(0xFFD32F2F),
                        modifier = Modifier.size(28.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = "🚨 CRITICAL SECURITY WARNING",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "MD5 is CRYPTOGRAPHICALLY BROKEN and MUST NOT be used for any security purpose. Practical collision attacks exist since 2004. MD5 should only be used for non-security applications like checksums.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFB71C1C)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = Color(0xFFD32F2F)
                        ) {
                            Text(
                                text = "  DO NOT USE FOR: Passwords, Digital Signatures, SSL Certificates  ",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
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
                    text = "Input Text",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 120.dp),
                    placeholder = { Text("Enter text to hash...") },
                    shape = RoundedCornerShape(12.dp),
                    maxLines = 5
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${inputText.length} characters | ${inputText.toByteArray(Charsets.UTF_8).size} bytes",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    TextButton(onClick = { inputText = "" }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Hash Button with danger styling
                Button(
                    onClick = {
                        isHashing = true
                        hashResult = computeMD5(inputText)
                        isHashing = false
                    },
                    enabled = inputText.isNotEmpty() && !isHashing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFD32F2F), // Red to indicate dangerous
                        contentColor = Color.White
                    )
                ) {
                    if (isHashing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = Color.White
                        )
                    } else {
                        Text("Generate MD5 Hash (INSECURE)")
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
                    text = "MD5 Hash Result",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (hashResult.isNotEmpty()) {
                    SelectionContainer {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surface
                        ) {
                            Text(
                                text = hashResult,
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
                                android.content.ClipData.newPlainText("md5_hash", hashResult).also { clipData ->
                                    android.app.ActivityManager.getActivity()?.let { activity ->
                                        (activity.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager)?.setPrimaryClip(clipData)
                                    }
                                }
                                showCopiedMessage = true
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = Color(0xFFD32F2F)
                            )
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (showCopiedMessage) "Copied!" else "Copy Hash")
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

                    // Hash Statistics
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        StatItem(label = "Hex Length", value = "${hashResult.length}")
                        StatItem(label = "Bits", value = "${hashResult.length * 4}")
                        StatItem(label = "Bytes", value = "${hashResult.length / 2}")
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Security Status Indicator - CRITICAL
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFFFFCDD2) // Red tint for insecure
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "🚫 ",
                            )
                            Text(
                                text = "CRITICALLY INSECURE: Collisions trivial to compute",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFFB71C1C),
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Enter text and click Generate to see the hash",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Technical Details Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Technical Details",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                DetailRow(label = "Algorithm", value = "MD5")
                DetailRow(label = "Full Name", value = "Message Digest Algorithm 5")
                DetailRow(label = "Output Size", value = "128 bits (16 bytes)")
                DetailRow(label = "Hex Length", value = "32 characters")
                DetailRow(label = "Block Size", value = "512 bits (64 bytes)")
                DetailRow(label = "Designed", value = "1991 by Ronald Rivest")
                DetailRow(label = "Status", value = "BROKEN", valueColor = Color(0xFFD32F2F))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Divider(modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Collision attack timeline
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFFFFEBEE)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Collision Attack Timeline:",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFD32F2F)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        AttackTimelineItem(year = "1996", event = "First theoretical flaw discovered")
                        AttackTimelineItem(year = "2004", event = "Practical collisions demonstrated (Wang et al.)")
                        AttackTimelineItem(year = "2005", event = "Chosen-prefix collision attacks possible")
                        AttackTimelineItem(year = "2008", event = "SSL certificate collision attack (Rogue CA)")
                        AttackTimelineItem(year = "2012", event = "Flame malware used MD5 collision")
                        AttackTimelineItem(year = "Now", event = "Collisions computable in seconds on GPU")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.md))

        // Acceptable Use Cases Only
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF8E1))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "⚠️ Limited Acceptable Uses",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFE65100)
                )

                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "MD5 may ONLY be used for non-security purposes:",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFBF360C)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                AcceptableUseItem("File integrity checksums (non-adversarial)")
                AcceptableUseItem("Legacy system compatibility")
                AcceptableUseItem("Educational demonstrations of broken crypto")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Divider(modifier = Modifier.fillMaxWidth())
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = "✓ For security, use SHA-256 or better",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF2E7D32)
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
            color = Color(0xFFD32F2F) // Red for broken algorithm
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DetailRow(label: String, value: String, valueColor: Color? = null) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Medium,
                color = valueColor ?: MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

@Composable
private fun AttackTimelineItem(year: String, event: String) {
    Row(
        modifier = Modifier.padding(vertical = 3.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$year: ",
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFD32F2F),
            minWidth = 50.dp
        )
        Text(
            text = event,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFC62828)
        )
    }
}

@Composable
private fun AcceptableUseItem(text: String) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "✓ ",
            color = Color(0xFFE65100)
        )
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = Color(0xFFBF360C)
        )
    }
}
