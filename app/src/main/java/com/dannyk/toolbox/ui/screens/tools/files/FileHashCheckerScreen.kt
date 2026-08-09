package com.dannyk.toolbox.ui.screens.tools.files

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.border
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolScreenLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileInputStream
import java.security.MessageDigest
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.ScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileHashCheckerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf<String?>(null) }
    var fileSize by remember { mutableStateOf<String?>(null) }
    
    // Hash algorithm selection
    var selectedAlgorithm by remember { mutableStateOf("SHA-256") }
    
    // Computed hash
    var computedHash by remember { mutableStateOf("") }
    
    // Hash to compare with
    var compareHash by remember { mutableStateOf("") }
    
    // Processing state
    var isProcessing by remember { mutableStateOf(false) }
    var progress by remember { mutableStateOf(0f) }
    
    // Match result
    var hashMatchResult by remember { mutableStateOf<HashMatchResult?>(null) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            
            // Get file name
            var name = "Unknown"
            var size = "Unknown"
            context.contentResolver.query(it, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
                    
                    if (nameIndex >= 0) name = cursor.getString(nameIndex) ?: "Unknown"
                    if (sizeIndex >= 0) {
                        val fileSizeLong = cursor.getLong(sizeIndex)
                        size = formatFileSize(fileSizeLong)
                    }
                }
            }
            
            fileName = name
            fileSize = size
            computedHash = ""
            hashMatchResult = null
        }
    }

    ToolScreenLayout(
        title = "File Hash Checker",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // File Selection Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select File",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { filePickerLauncher.launch("*/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.FolderOpen, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose File")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (selectedFileUri == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(80.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No file selected",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Text(
                                    text = fileName ?: "Selected File",
                                    style = MaterialTheme.typography.bodyMedium,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = fileSize ?: "",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Algorithm Selection
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Hash Algorithm",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val algorithms = listOf("MD5", "SHA-1", "SHA-256")
                    
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        algorithms.forEachIndexed { index, algorithm ->
                            SegmentedButton(
                                selected = selectedAlgorithm == algorithm,
                                onClick = { 
                                    selectedAlgorithm = algorithm
                                    computedHash = ""
                                    hashMatchResult = null
                                },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = algorithms.size),
                                icon = {}
                            ) {
                                Text(algorithm)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when (selectedAlgorithm) {
                            "MD5" -> "MD5 produces a 128-bit (32 hex characters) hash. Fast but not cryptographically secure."
                            "SHA-1" -> "SHA-1 produces a 160-bit (40 hex characters) hash. Deprecated for security use."
                            "SHA-256" -> "SHA-256 produces a 256-bit (64 hex characters) hash. Recommended for most uses."
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Calculate Hash Button
            Button(
                onClick = {
                    if (selectedFileUri == null) {
                        Toast.makeText(context, "Please select a file first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isProcessing = true
                    progress = 0f
                    computedHash = ""
                    hashMatchResult = null
                    
                    scope.launch {
                        try {
                            val hash = calculateFileHash(
                                context = context,
                                uri = selectedFileUri!!,
                                algorithm = selectedAlgorithm,
                                onProgress = { currentProgress ->
                                    progress = currentProgress
                                }
                            )
                            
                            withContext(Dispatchers.Main) {
                                computedHash = hash
                                isProcessing = false
                                
                                // Auto-compare if comparison hash exists
                                if (compareHash.isNotEmpty()) {
                                    hashMatchResult = compareHashes(hash, compareHash.trim())
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isProcessing = false
                                Toast.makeText(context, "Error calculating hash: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                enabled = selectedFileUri != null && !isProcessing,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Calculate Hash")
                }
            }

            // Progress Indicator
            if (isProcessing) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Calculating ${selectedAlgorithm} hash...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                        )
                        
                        Spacer(modifier = Modifier.height(4.dp))
                        
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            // Computed Hash Display
            if (computedHash.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedAlgorithm} Hash:",
                                style = MaterialTheme.typography.titleSmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            IconButton(
                                onClick = {
                                    val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                    val clip = android.content.ClipData.newPlainText("file hash", computedHash)
                                    clipboard.setPrimaryClip(clip)
                                    Toast.makeText(context, "Hash copied!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(
                                    Icons.Default.ContentCopy,
                                    contentDescription = "Copy hash",
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = computedHash,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                maxLines = 2,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            }

            // Compare Section
            if (computedHash.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Verify Hash",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        OutlinedTextField(
                            value = compareHash,
                            onValueChange = { newValue ->
                                compareHash = newValue
                                if (computedHash.isNotEmpty() && newValue.isNotEmpty()) {
                                    hashMatchResult = compareHashes(computedHash, newValue.trim())
                                } else {
                                    hashMatchResult = null
                                }
                            },
                            label = { Text("Enter expected hash to compare") },
                            placeholder = { Text("Paste the expected hash here...") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            textStyle = androidx.compose.ui.text.TextStyle(fontFamily = FontFamily.Monospace)
                        )
                        
                        // Match Result
                        hashMatchResult?.let { result ->
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = if (result.isMatch) 
                                        Color(0xFF4CAF50).copy(0.15f) 
                                    else 
                                        Color(0xFFF44336).copy(0.15f)
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        imageVector = if (result.isMatch) Icons.Default.CheckCircle else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (result.isMatch) Color(0xFF4CAF50) else Color(0xFFF44336)
                                    )
                                    
                                    Spacer(modifier = Modifier.width(12.dp))
                                    
                                    Column {
                                        Text(
                                            text = if (result.isMatch) "MATCH!" else "MISMATCH!",
                                            style = MaterialTheme.typography.titleMedium,
                                            color = if (result.isMatch) Color(0xFF4CAF50) else Color(0xFFF44336)
                                        )
                                        Text(
                                            text = result.message,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Info Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "About File Hashing",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "• A hash is a unique fingerprint of a file's contents\n" +
                               "• Use this tool to verify downloaded files\n" +
                               "• If hashes match, files are identical\n" +
                               "• MD5: Fast but vulnerable to collisions\n" +
                               "• SHA-1: Better than MD5 but still deprecated\n" +
                               "• SHA-256: Most secure, recommended option\n" +
                               "• Large files may take longer to process",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class HashMatchResult(val isMatch: Boolean, val message: String)

private fun compareHashes(computed: String, expected: String): HashMatchResult {
    val normalizedComputed = computed.lowercase().trim()
    val normalizedExpected = expected.lowercase().trim()
    
    return if (normalizedComputed == normalizedExpected) {
        HashMatchResult(true, "The computed hash matches the expected value.")
    } else {
        HashMatchResult(false, "The hashes do NOT match! The file may be corrupted or different.")
    }
}

private suspend fun calculateFileHash(
    context: Context,
    uri: Uri,
    algorithm: String,
    onProgress: (Float) -> Unit
): String = withContext(Dispatchers.IO) {
    val digest = when (algorithm.uppercase()) {
        "MD5" -> MessageDigest.getInstance("MD5")
        "SHA-1" -> MessageDigest.getInstance("SHA-1")
        "SHA-256" -> MessageDigest.getInstance("SHA-256")
        else -> MessageDigest.getInstance("SHA-256")
    }
    
    // Get file size for progress calculation
    var totalSize = 0L
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val sizeIndex = cursor.getColumnIndex(android.provider.OpenableColumns.SIZE)
            if (sizeIndex >= 0) {
                totalSize = cursor.getLong(sizeIndex)
            }
        }
    }
    
    val buffer = ByteArray(8192) // 8KB buffer
    var bytesRead: Int
    
    context.contentResolver.openInputStream(uri)?.use { inputStream ->
        do {
            bytesRead = inputStream.read(buffer)
            if (bytesRead > 0) {
                digest.update(buffer, 0, bytesRead)
                
                // Update progress
                if (totalSize > 0) {
                    val currentPosition = inputStream.available().coerceAtLeast(0)
                    val processed = totalSize - currentPosition
                    onProgress((processed.toFloat() / totalSize).coerceIn(0f, 1f))
                }
            }
        } while (bytesRead != -1)
    }
    
    // Finalize progress
    onProgress(1f)
    
    // Convert to hex string
    val hashBytes = digest.digest()
    val sb = StringBuilder()
    for (byte in hashBytes) {
        sb.append(String.format("%02x", byte))
    }
    
    sb.toString()
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${bytes / 1024} KB"
    if (bytes < 1024 * 1024 * 1024) return String.format("%.2f MB", bytes / (1024.0 * 1024))
    return String.format("%.2f GB", bytes / (1024.0 * 1024 * 1024))
}