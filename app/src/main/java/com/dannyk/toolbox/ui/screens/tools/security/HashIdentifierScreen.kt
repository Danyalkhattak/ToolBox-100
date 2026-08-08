package com.dannyk.toolbox.ui.screens.tools.security

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
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
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HashIdentifierScreen(navController: NavHostController) {
    var hashInput by remember { mutableStateOf("") }
    var identificationResults by remember { mutableStateOf(listOf<HashMatch>()) }
    var hasIdentified by remember { mutableStateOf(false) }

    data class HashType(
        val name: String,
        val hexLength: Int,
        val bits: Int,
        val description: String,
        val securityStatus: String,
        val statusColor: Color
    )

    data class HashMatch(
        val hashType: HashType,
        val confidence: Float, // 0.0 to 1.0
        val reason: String
    )

    // Known hash types with their characteristics
    val knownHashTypes = listOf(
        HashType("MD5", 32, 128, "Message Digest Algorithm 5", "BROKEN - Collision attacks exist", Color(0xFFFF5252)),
        HashType("MD4", 32, 128, "Message Digest Algorithm 4", "BROKEN - Completely broken", Color(0xFFFF5252)),
        HashType("NTLM", 32, 128, "NT LAN Manager hash", "WEAK - Based on MD4", Color(0xFFFF9800)),
        HashType("SHA-1", 40, 160, "Secure Hash Algorithm 1", "DEPRECATED - Collisions practical", Color(0xFFFF9800)),
        HashType("MySQL (old)", 40, 160, "Old MySQL password hash", "WEAK - Deprecated", Color(0xFFFF9800)),
        HashType("RIPEMD-160", 40, 160, "RACE Integrity Primitives Evaluation MD-160", "Weak - Not recommended", Color(0xFFFF9800)),
        HashType("SHA-224", 56, 224, "Secure Hash Algorithm 224-bit", "Secure - Less common", Color(0xFF4CAF50)),
        HashType("SHA-256", 64, 256, "Secure Hash Algorithm 256-bit", "SECURE - Industry standard", Color(0xFF4CAF50)),
        HashType("SHA3-224", 56, 224, "SHA-3 (Keccak) 224-bit", "SECURE - Future-proof", Color(0xFF4CAF50)),
        HashType("SHA3-256", 64, 256, "SHA-3 (Keccak) 256-bit", "SECURE - Future-proof", Color(0xFF4CAF50)),
        HashType("SHA-384", 96, 384, "Secure Hash Algorithm 384-bit", "SECURE - High security", Color(0xFF4CAF50)),
        HashType("SHA3-384", 96, 384, "SHA-3 (Keccak) 384-bit", "SECURE - High security", Color(0xFF4CAF50)),
        HashType("SHA-512", 128, 512, "Secure Hash Algorithm 512-bit", "VERY SECURE - Maximum security", Color(0xFF2196F3)),
        HashType("SHA3-512", 128, 512, "SHA-3 (Keccak) 512-bit", "VERY SECURE - Maximum security", Color(0xFF2196F3)),
        HashType("Whirlpool", 128, 512, "Whirlpool hash function", "SECURE - Less common", Color(0xFF4CAF50))
    )

    fun isHexOnly(input: String): Boolean {
        return input.all { it in '0'..'9' || it in 'a'..'f' || it in 'A'..'F' }
    }

    fun isBase64(input: String): Boolean {
        // Base64 can contain A-Z, a-z, 0-9, +, /, and = for padding
        val base64Regex = Regex("^[A-Za-z0-9+/]*={0,2}$")
        return base64Regex.matches(input) && input.length % 4 == 0 && input.length >= 4
    }

    fun identifyHash(hash: String): List<HashMatch> {
        if (hash.isEmpty()) return emptyList()
        
        val results = mutableListOf<HashMatch>()
        val trimmedHash = hash.trim()
        val length = trimmedHash.length
        
        // Check if it's valid hex
        val isValidHex = isHexOnly(trimmedHash)
        
        // Check if it might be base64
        val isLikelyBase64 = !isValidHex && isBase64(trimmedHash)
        
        // Match based on length (for hex hashes)
        if (isValidHex) {
            knownHashTypes.forEach { hashType ->
                if (length == hashType.hexLength) {
                    var confidence = 0.8f
                    var reason = "Length matches ${hashType.hexLength} hex characters"
                    
                    // Boost confidence for unique lengths
                    when (length) {
                        32 -> { /* MD5/MD4/NTLM share this length */ }
                        40 -> { /* SHA-1 shares this length */ }
                        56 -> { /* SHA-224 and SHA3-224 share this length */ }
                        64 -> { 
                            // SHA-256 is very common at 64 chars
                            confidence = 0.85f
                            reason += " (most common SHA-256 format)"
                        }
                        96 -> { /* SHA-384 and SHA3-384 share this length */ }
                        128 -> {
                            // SHA-512 is very common at 128 chars
                            confidence = 0.90f
                            reason += " (strong indicator of SHA-512 family)"
                        }
                    }
                    
                    // Check character distribution for additional hints
                    val hasUppercase = trimmedHash.any { it in 'A'..'F' }
                    val hasLowercase = trimmedHash.any { it in 'a'..'f' }
                    
                    if (!hasLowercase && hasUppercase) {
                        // All uppercase hex - slightly less common but valid
                    } else if (hasLowercase && !hasUppercase) {
                        // All lowercase hex - most common format
                        confidence += 0.05f
                    }
                    
                    results.add(HashMatch(hashType, confidence.coerceIn(0f, 1f), reason))
                }
            }
            
            // Check for common prefixes that indicate specific formats
            if (trimmedHash.startsWith("\$apr1\$")) {
                results.add(HashMatch(
                    HashType("Apache MD5-crypt", 0, 0, "Apache MD5 crypt password hash", "Moderate - Salted MD5 variant", Color(0xFFFF9800)),
                    0.95f,
                    "Detected Apache MD5-crypt prefix (\$apr1$)"
                ))
            }
            if (trimmedHash.startsWith("\$2y\$") || trimmedHash.startsWith("\$2a\$") || trimmedHash.startsWith("\$2b\$")) {
                results.add(HashMatch(
                    HashType("bcrypt", 60, 0, "bcrypt password hash", "SECURE - Recommended for passwords", Color(0xFF4CAF50)),
                    0.98f,
                    "Detected bcrypt prefix"
                ))
            }
            if (trimmedHash.startsWith("\$6\$")) {
                results.add(HashMatch(
                    HashType("SHA-512 crypt", 0, 0, "Unix SHA-512 crypt password hash", "SECURE - Salted SHA-512", Color(0xFF4CAF50)),
                    0.95f,
                    "Detected SHA-512 crypt prefix (\$6$)"
                ))
            }
            if (trimmedHash.startsWith("\$5\$")) {
                results.add(HashMatch(
                    HashType("SHA-256 crypt", 0, 0, "Unix SHA-256 crypt password hash", "SECURE - Salted SHA-256", Color(0xFF4CAF50)),
                    0.95f,
                    "Detected SHA-256 crypt prefix (\$5$)"
                ))
            }
            if (trimmedHash.startsWith("\$1\$")) {
                results.add(HashMatch(
                    HashType("MD5 crypt", 0, 0, "Unix MD5 crypt password hash", "WEAK - Salted MD5", Color(0xFFFF9800)),
                    0.92f,
                    "Detected MD5 crypt prefix (\$1$)"
                ))
            }
        }
        
        // Base64 encoded hashes
        if (isLikelyBase64) {
            // Try to estimate original size from base64 length
            val estimatedOriginalSize = (length * 3) / 4
            
            when {
                estimatedOriginalSize == 16 -> results.add(HashMatch(
                    HashType("Base64(MD5)", 32, 128, "MD5 hash in Base64 encoding", "BROKEN - Same as MD5", Color(0xFFFF5252)),
                    0.7f,
                    "Base64 length suggests 16-byte hash (MD5 size)"
                ))
                estimatedOriginalSize == 20 -> results.add(HashMatch(
                    HashType("Base64(SHA-1)", 40, 160, "SHA-1 hash in Base64 encoding", "DEPRECATED - Same as SHA-1", Color(0xFFFF9800)),
                    0.7f,
                    "Base64 length suggests 20-byte hash (SHA-1 size)"
                ))
                estimatedOriginalSize == 28 -> results.add(HashMatch(
                    HashType("Base64(SHA-224)", 56, 224, "SHA-224 hash in Base64 encoding", "SECURE", Color(0xFF4CAF50)),
                    0.65f,
                    "Base64 length suggests 28-byte hash (SHA-224 size)"
                ))
                estimatedOriginalSize == 32 -> results.add(HashMatch(
                    HashType("Base64(SHA-256)", 64, 256, "SHA-256 hash in Base64 encoding", "SECURE", Color(0xFF4CAF50)),
                    0.75f,
                    "Base64 length suggests 32-byte hash (SHA-256 size)"
                ))
                estimatedOriginalSize == 48 -> results.add(HashMatch(
                    HashType("Base64(SHA-384)", 96, 384, "SHA-384 hash in Base64 encoding", "SECURE", Color(0xFF4CAF50)),
                    0.65f,
                    "Base64 length suggests 48-byte hash (SHA-384 size)"
                ))
                estimatedOriginalSize == 64 -> results.add(HashMatch(
                    HashType("Base64(SHA-512)", 128, 512, "SHA-512 hash in Base64 encoding", "VERY SECURE", Color(0xFF2196F3)),
                    0.70f,
                    "Base64 length suggests 64-byte hash (SHA-512 size)"
                ))
            }
        }
        
        // If no matches found
        if (results.isEmpty()) {
            if (isValidHex) {
                results.add(HashMatch(
                    HashType("Unknown Hex Hash", 0, 0, "Unrecognized hex hash format", "Unknown", Color.Gray),
                    0.1f,
                    "Valid hex but no matching known hash length ($length chars)"
                ))
            } else if (isLikelyBase64) {
                results.add(HashMatch(
                    HashType("Unknown Base64 Data", 0, 0, "Unrecognized base64 data", "Unknown", Color.Gray),
                    0.1f,
                    "Valid base64 but unknown content type"
                ))
            } else {
                results.add(HashMatch(
                    HashType("Not a Recognized Hash", 0, 0, "Input does not match any known hash format", "N/A", Color.Gray),
                    0.0f,
                    "Does not appear to be a standard hash format"
                ))
            }
        }
        
        // Sort by confidence descending
        return results.sortedByDescending { it.confidence }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        ToolTopBar(
            title = "Hash Identifier",
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
                        text = "About Hash Identification",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "This tool identifies hash types based on length, character set, and common prefixes. Note: Multiple hash types may share the same output length.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Input Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Paste Hash",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = hashInput,
                    onValueChange = { 
                        if (it.length <= 256) hashInput = it 
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 100.dp),
                    placeholder = { Text("Paste your hash here...") },
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
                        text = "${hashInput.length} characters",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Show format hint
                    if (hashInput.isNotEmpty()) {
                        val formatHint = when {
                            isHexOnly(hashInput) -> "Detected: Hexadecimal"
                            isBase64(hashInput) -> "Detected: Base64"
                            else -> "Format: Unknown"
                        }
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = formatHint,
                                style = MaterialTheme.typography.labelSmall,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    
                    TextButton(onClick = { 
                        hashInput = ""
                        identificationResults = emptyList()
                        hasIdentified = false
                    }) {
                        Text("Clear", color = MaterialTheme.colorScheme.error)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Identify Button
                Button(
                    onClick = {
                        identificationResults = identifyHash(hashInput)
                        hasIdentified = true
                    },
                    enabled = hashInput.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Identify Hash Type")
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Results Card
        if (hasIdentified) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Identification Results",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (identificationResults.isNotEmpty()) {
                        identificationResults.forEachIndexed { index, match ->
                            ResultCard(match = match, rank = index + 1)
                            
                            if (index < identificationResults.lastIndex) {
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(60.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "Unable to identify hash",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Reference Table Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📋 Common Hash Format Reference",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Table header
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Algorithm", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                        Text("Hex Len", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("Bits", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                        Text("Status", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Hash entries
        knownHashTypes.filter { it.hexLength > 0 }.forEach { hashType ->
                    HashTableRow(hashType = hashType)
                }

                Spacer(modifier = Modifier.height(12.dp))
                
                HorizontalDivider()
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Password hash formats
                Text(
                    text = "Password Hash Formats (with prefix):",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Medium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                PasswordHashRow(prefix = "\$1\$", name = "MD5 crypt", status = "Weak")
                PasswordHashRow(prefix = "\$5\$", name = "SHA-256 crypt", status = "Secure")
                PasswordHashRow(prefix = "\$6\$", name = "SHA-512 crypt", status = "Secure")
                PasswordHashRow(prefix = "\$2y\$ / \$2a\$", name = "bcrypt", status = "Secure")
                PasswordHashRow(prefix = "\$apr1\$", name = "Apache MD5", status = "Weak")
                PasswordHashRow(prefix = "argon2", name = "Argon2", status = "Very Secure")
            }
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun ResultCard(match: HashMatch, rank: Int) {
    val confidenceColor = when {
        match.confidence >= 0.9f -> Color(0xFF4CAF50) // Green - high confidence
        match.confidence >= 0.7f -> Color(0xFFFF9800)   // Orange - medium confidence
        match.confidence >= 0.3f -> Color(0xFFFFC107)   // Yellow - low confidence
        else -> Color.Gray                              // Gray - very uncertain
    }
    
    val confidenceLabel = when {
        match.confidence >= 0.9f -> "High Confidence"
        match.confidence >= 0.7f -> "Likely"
        match.confidence >= 0.3f -> "Possible"
        else -> "Uncertain"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = match.hashType.statusColor.copy(alpha = 0.08f)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Rank badge
                    Surface(
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.primary
                    ) {
                        Text(
                            text = "$rank",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    Column {
                        Text(
                            text = match.hashType.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        if (match.hashType.bits > 0) {
                            Text(
                                text = "${match.hashType.bits}-bit${if (match.hashType.hexLength > 0) " (${match.hashType.hexLength} hex)" else ""}",
                                style = MaterialTheme.typography.bodySmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
                
                // Confidence badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = confidenceColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "%.0f%%".format(match.confidence * 100),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = confidenceColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Description
            Text(
                text = match.hashType.description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(4.dp))
            
            // Reason
            Text(
                text = "💡 ${match.reason}",
                style = MaterialTheme.typography.bodySmall,
                fontStyle = androidx.compose.ui.text.font.FontStyle.Italic,
                color = MaterialTheme.colorScheme.primary
            )
            
            // Security status
            Spacer(modifier = Modifier.height(8.dp))
            
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = match.hashType.statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = "Security: ${match.hashType.securityStatus}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    color = match.hashType.statusColor,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun HashTableRow(hashType: HashType) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = hashType.name,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${hashType.hexLength}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(40.dp)
            )
            Text(
                text = "${hashType.bits}",
                style = MaterialTheme.typography.bodySmall,
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.width(35.dp)
            )
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = hashType.statusColor.copy(alpha = 0.15f)
            ) {
                Text(
                    text = when {
                        hashType.statusColor == Color(0xFFFF5252) -> "Broken"
                        hashType.statusColor == Color(0xFFFF9800) -> "Weak"
                        hashType.statusColor == Color(0xFF4CAF50) -> "Secure"
                        else -> "Strong"
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = hashType.statusColor,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun PasswordHashRow(prefix: String, name: String, status: String) {
    val statusColor = when (status) {
        "Very Secure" -> Color(0xFF2196F3)
        "Secure" -> Color(0xFF4CAF50)
        "Weak" -> Color(0xFFFF9800)
        else -> Color.Gray
    }
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = prefix,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            modifier = Modifier.width(120.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = name,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.weight(1f)
        )
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = statusColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = status,
                style = MaterialTheme.typography.labelSmall,
                color = statusColor,
                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
            )
        }
    }
}
