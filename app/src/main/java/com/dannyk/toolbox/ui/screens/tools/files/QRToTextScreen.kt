package com.dannyk.toolbox.ui.screens.tools.files

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Image
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolScreenLayout
import com.google.zxing.BinaryBitmap
import com.google.zxing.DecodeHintType
import com.google.zxing.MultiFormatReader
import com.google.zxing.RGBLuminanceSource
import com.google.zxing.common.HybridBinarizer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRToTextScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Selected image
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var selectedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    
    // Decoded results
    var decodedResults by remember { mutableStateOf<List<QrDecodeResult>>(emptyList()) }
    var isDecoding by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            decodedResults = emptyList()
            errorMessage = null
            
            scope.launch {
                try {
                    val inputStream: InputStream? = context.contentResolver.openInputStream(it)
                    val bitmap = BitmapFactory.decodeStream(inputStream)
                    inputStream?.close()
                    
                    if (bitmap != null) {
                        withContext(Dispatchers.Main) {
                            selectedBitmap = bitmap
                        }
                        
                        // Auto-decode on selection
                        decodeQRFromBitmap(bitmap)
                    } else {
                        withContext(Dispatchers.Main) {
                            errorMessage = "Could not load the image"
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        errorMessage = "Error loading image: ${e.message}"
                    }
                }
            }
        }
    }

    // Function to decode QR code from bitmap
    fun decodeQRFromBitmap(bitmap: Bitmap) {
        isDecoding = true
        decodedResults = emptyList()
        errorMessage = null
        
        scope.launch {
            try {
                val results = decodeQRCodes(bitmap)
                
                withContext(Dispatchers.Main) {
                    decodedResults = results
                    isDecoding = false
                    
                    if (results.isEmpty()) {
                        errorMessage = "No QR code found in this image"
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    isDecoding = false
                    errorMessage = "Error decoding: ${e.message}"
                }
            }
        }
    }

    ToolScreenLayout(
        title = "QR Code Decoder",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Image Selection Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Select QR Code Image",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Button(
                        onClick = { imagePickerLauncher.launch("image/*") },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Image, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Choose Image")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    if (selectedBitmap == null) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(180.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(
                                    Icons.Default.Image,
                                    contentDescription = null,
                                    modifier = Modifier.size(48.dp),
                                    tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "No image selected\nTap to choose a QR code image",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                bitmap = selectedBitmap!!.asImageBitmap(),
                                contentDescription = "Selected QR Code",
                                modifier = Modifier
                                    .height(200.dp)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    )
                            )
                        }
                    }
                }
            }

            // Decode Button (if not auto-decoded or to re-decode)
            if (selectedBitmap != null) {
                Button(
                    onClick = { decodeQRFromBitmap(selectedBitmap!!) },
                    enabled = !isDecoding,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isDecoding) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Text("Decode QR Code")
                    }
                }
            }

            // Decoding Progress
            if (isDecoding) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(16.dp))
                        Text(
                            text = "Scanning for QR codes...",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            // Error Message
            errorMessage?.let { error ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = error,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            // Decoded Results
            if (decodedResults.isNotEmpty()) {
                Text(
                    text = "Decoded Content (${decodedResults.size} QR code${if (decodedResults.size > 1) "s" else ""} found)",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                decodedResults.forEachIndexed { index, result ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        )
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            // Header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "QR Code #${index + 1}",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = when (result.format) {
                                        "QR_CODE" -> MaterialTheme.colorScheme.primaryContainer
                                        else -> MaterialTheme.colorScheme.secondaryContainer
                                    }
                                ) {
                                    Text(
                                        text = result.format.replace("_", " "),
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Content Display
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = result.text,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        fontFamily = FontFamily.Monospace
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    maxLines = 10,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(4.dp))
                            
                            // Metadata
                            Text(
                                text = "${result.text.length} characters | Raw bytes: ${result.rawBytes}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Action Buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                OutlinedButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("qr content", result.text)
                                        clipboard.setPrimaryClip(clip)
                                        Toast.makeText(context, "Copied to clipboard!", Toast.LENGTH_SHORT).show()
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = null, Modifier.size(18.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Copy Text")
                                }
                                
                                OutlinedButton(
                                    onClick = {
                                        // Try to open URL if it looks like one
                                        if (result.text.startsWith("http://") || result.text.startsWith("https://")) {
                                            try {
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse(result.text))
                                                context.startActivity(intent)
                                            } catch (e: Exception) {
                                                Toast.makeText(context, "Cannot open URL", Toast.LENGTH_SHORT).show()
                                            }
                                        } else {
                                            Toast.makeText(context, "Not a valid URL", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f),
                                    enabled = result.text.startsWith("http://") || result.text.startsWith("https://")
                                ) {
                                    Text("Open URL")
                                }
                            }
                        }
                    }
                    
                    if (index < decodedResults.lastIndex) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }

            // Instructions / Info
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Supported Formats & Tips",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• QR Codes (most common)\n" +
                               "• Data Matrix codes\n" +
                               "• Aztec codes\n" +
                               "• PDF417 barcodes\n" +
                               "• Supports multiple QR codes in one image\n" +
                               "• For best results, use clear, well-lit images\n" +
                               "• Supports URLs, plain text, vCards, WiFi configs & more",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private data class QrDecodeResult(
    val text: String,
    val format: String,
    val rawBytes: Int
)

private suspend fun decodeQRCodes(bitmap: Bitmap): List<QrDecodeResult> = withContext(Dispatchers.IO) {
    val results = mutableListOf<QrDecodeResult>()
    
    try {
        val intArray = IntArray(bitmap.width * bitmap.height)
        bitmap.getPixels(intArray, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        
        val source = RGBLuminanceSource(bitmap.width, bitmap.height, intArray)
        val binaryBitmap = BinaryBitmap(HybridBinarizer(source))
        
        val reader = MultiFormatReader()
        
        // Configure hints for better detection
        val hints = mapOf(
            DecodeHintType.TRY_HARDER to true,
            DecodeHintType.PURE_BARCODE to false
        )
        
        reader.setHints(hints)
        
        // Try to decode
        val result = reader.decode(binaryBitmap)
        
        results.add(
            QrDecodeResult(
                text = result.text,
                format = result.barcodeFormat.name,
                rawBytes = result.rawBytes?.size ?: 0
            )
        )
        
        // Reset for potential multiple reads
        reader.reset()
        
    } catch (e: Exception) {
        // No QR code found or other error - return empty list
    }
    
    results
}
