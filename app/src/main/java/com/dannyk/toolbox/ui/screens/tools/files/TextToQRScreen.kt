package com.dannyk.toolbox.ui.screens.tools.files

import android.content.Context
import android.graphics.Bitmap
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolScreenLayout
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.Color
import android.content.Intent
import android.content.ClipboardManager
import androidx.compose.ui.draw.clip
import android.os.Environment
import androidx.core.content.FileProvider

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TextToQRScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    // Input text
    var inputText by remember { mutableStateOf("") }
    
    // QR Code options
    var qrSize by remember { mutableStateOf("512") }
    var errorCorrection by remember { mutableStateOf("M") }
    var foregroundColor by remember { mutableStateOf(Color.Black) }
    var backgroundColor by remember { mutableStateOf(Color.White) }
    
    // Generated QR code bitmap
    var qrBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isGenerating by remember { mutableStateOf(false) }

    ToolScreenLayout(
        title = "Text to QR Code",
        navController = navController
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Text Input Section
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Enter Text or URL",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        TextButton(
                            onClick = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                val clip = clipboard.primaryClip?.getItemAt(0)?.text?.toString() ?: ""
                                if (clip.isNotEmpty()) {
                                    inputText = clip
                                    Toast.makeText(context, "Text pasted!", Toast.LENGTH_SHORT).show()
                                }
                            }
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Paste")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { 
                            inputText = it
                            qrBitmap = null // Regenerate needed
                        },
                        placeholder = { 
                            Text("Enter text, URL, email, phone number...\n\nThe content you want to encode in the QR code.") 
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp, max = 200.dp),
                        maxLines = 8
                    )
                    
                    Spacer(modifier = Modifier.height(4.dp))
                    
                    Text(
                        text = "${inputText.length} characters | Max: ~2953 bytes (varies by content)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // QR Code Options
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "QR Code Options",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Size Selection
                    Text(
                        text = "Size (pixels)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Slider(
                            value = qrSize.toFloatOrNull()?.coerceIn(128f, 1024f) ?: 512f,
                            onValueChange = { qrSize = it.toInt().toString() },
                            valueRange = 128f..1024f,
                            steps = 7,
                            modifier = Modifier.weight(1f)
                        )
                        
                        Spacer(modifier = Modifier.width(12.dp))
                        
                        OutlinedTextField(
                            value = qrSize,
                            onValueChange = { newValue ->
                                if (newValue.isEmpty() || (newValue.all { it.isDigit() } && newValue.toIntOrNull()?.let { v -> v >= 64 && v <= 2048 } == true)) {
                                    qrSize = newValue
                                }
                            },
                            singleLine = true,
                            modifier = Modifier.width(80.dp)
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Error Correction Level
                    Text(
                        text = "Error Correction Level",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val errorLevels = listOf("L", "M", "Q", "H")
                    
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        errorLevels.forEachIndexed { index, level ->
                            SegmentedButton(
                                selected = errorCorrection == level,
                                onClick = { errorCorrection = level },
                                shape = SegmentedButtonDefaults.itemShape(index = index, count = errorLevels.size),
                                icon = {}
                            ) {
                                Text(level)
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = when (errorCorrection) {
                            "L" -> "L (Low): ~7% recovery capacity"
                            "M" -> "M (Medium): ~15% recovery capacity"
                            "Q" -> "Q (Quartile): ~25% recovery capacity"
                            "H" -> "H (High): ~30% recovery capacity"
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Generate Button
            Button(
                onClick = {
                    if (inputText.isBlank()) {
                        Toast.makeText(context, "Please enter some text first", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    
                    isGenerating = true
                    
                    scope.launch {
                        try {
                            val size = qrSize.toIntOrNull() ?: 512
                            val bitmap = generateQRCode(
                                content = inputText,
                                size = size,
                                errorCorrection = when (errorCorrection) {
                                    "L" -> ErrorCorrectionLevel.L
                                    "Q" -> ErrorCorrectionLevel.Q
                                    "H" -> ErrorCorrectionLevel.H
                                    else -> ErrorCorrectionLevel.M
                                },
                                foregroundColor = foregroundColor,
                                backgroundColor = backgroundColor
                            )
                            
                            withContext(Dispatchers.Main) {
                                qrBitmap = bitmap
                                isGenerating = false
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                isGenerating = false
                                Toast.makeText(context, "Error generating QR code: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                enabled = inputText.isNotBlank() && !isGenerating,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (isGenerating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Default.QrCode2, contentDescription = null)
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(if (isGenerating) "Generating..." else "Generate QR Code")
            }

            // QR Code Display
            qrBitmap?.let { bitmap ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Generated QR Code",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Generated QR Code",
                            modifier = Modifier
                                .size(256.dp)
                                .border(
                                    width = 2.dp,
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                    shape = RoundedCornerShape(8.dp)
                                )
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        Text(
                            text = "${bitmap.width} x ${bitmap.height} pixels",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    scope.launch {
                                        saveQRCodeImage(context, bitmap, "qrcode")
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "QR code saved!", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null, Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Save")
                            }
                            
                            OutlinedButton(
                                onClick = {
                                    shareQRCodeImage(context, bitmap)
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.Share, contentDescription = null, Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Share")
                            }
                        }
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
                        text = "About QR Codes",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• QR codes can store up to ~3000 characters of data\n" +
                               "• Higher error correction allows reading damaged codes\n" +
                               "• Scan with any QR reader app on your device\n" +
                               "• Supports URLs, text, emails, phone numbers & more\n" +
                               "• Larger sizes produce clearer images for printing",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private suspend fun generateQRCode(
    content: String,
    size: Int,
    errorCorrection: ErrorCorrectionLevel,
    foregroundColor: Color,
    backgroundColor: Color
): Bitmap = withContext(Dispatchers.IO) {
    val writer = QRCodeWriter()
    
    val hints = mapOf(
        EncodeHintType.ERROR_CORRECTION to errorCorrection,
        EncodeHintType.CHARACTER_SET to "UTF-8",
        EncodeHintType.MARGIN to 1
    )
    
    val bitMatrix = writer.encode(content, BarcodeFormat.QR_CODE, size, size, hints)
    
    val width = bitMatrix.width
    val height = bitMatrix.height
    val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    
    for (x in 0 until width) {
        for (y in 0 until height) {
            bitmap.setPixel(x, y, if (bitMatrix[x, y]) foregroundColor else backgroundColor)
        }
    }
    
    bitmap
}

private suspend fun saveQRCodeImage(
    context: Context,
    bitmap: Bitmap,
    prefix: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(
            android.os.Environment.DIRECTORY_DOWNLOADS
        )
        
        val file = File(downloadsDir, "${prefix}_${System.currentTimeMillis()}.png")
        
        FileOutputStream(file).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}

private fun shareQRCodeImage(context: Context, bitmap: Bitmap) {
    try {
        // Save to cache for sharing
        val cacheFile = File(context.cacheDir, "share_qr_${System.currentTimeMillis()}.png")
        FileOutputStream(cacheFile).use { outputStream ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
        }
        
        // Get URI using FileProvider
        val uri = androidx.core.content.FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            cacheFile
        )
        
        val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(android.content.Intent.EXTRA_STREAM, uri)
            addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        
        context.startActivity(android.content.Intent.createChooser(shareIntent, "Share QR Code"))
    } catch (e: Exception) {
        android.widget.Toast.makeText(context, "Error sharing: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
    }
}
