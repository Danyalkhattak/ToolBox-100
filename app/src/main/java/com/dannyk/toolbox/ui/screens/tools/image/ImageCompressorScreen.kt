package com.dannyk.toolbox.ui.screens.tools.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import android.content.Intent
import androidx.compose.foundation.ScrollState
import android.os.Environment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCompressorScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var compressedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var originalUri by remember { mutableStateOf<Uri?>(null) }
    var originalSize by remember { mutableStateOf(0L) }
    var compressedSize by remember { mutableStateOf(0L) }
    var quality by remember { mutableIntStateOf(80) }
    var outputFormat by remember { mutableStateOf("JPEG") }
    var isProcessing by remember { mutableStateOf(false) }
    var showSaveMessage by remember { mutableStateOf(false) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            originalUri = it
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: byteArrayOf()
                    originalSize = bytes.size.toLong()
                    originalBitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    // Auto-compress when image is loaded
                    compressImage(originalBitmap, quality, outputFormat) { bitmap, size ->
                        compressedBitmap = bitmap
                        compressedSize = size
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = { ToolTopBar("Image Compressor") { navController.navigateUp() } }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Pick Image Button
            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Image from Gallery")
            }

            // Original Image Preview
            if (originalBitmap != null) {
                Text(
                    text = "Original Image",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            bitmap = originalBitmap!!.asImageBitmap(),
                            contentDescription = "Original Image",
                            modifier = Modifier
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Size: ${formatFileSize(originalSize)} | Dimensions: ${originalBitmap!!.width} x ${originalBitmap!!.height}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Quality Slider
            Text(
                text = "Compression Quality: $quality%",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Slider(
                value = quality.toFloat(),
                onValueChange = { 
                    quality = it.toInt()
                    if (originalBitmap != null && !isProcessing) {
                        compressImage(originalBitmap, quality, outputFormat) { bitmap, size ->
                            compressedBitmap = bitmap
                            compressedSize = size
                        }
                    }
                },
                valueRange = 1f..100f,
                steps = 98,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("1% (Smallest)", style = MaterialTheme.typography.bodySmall)
                Text("100% (Largest)", style = MaterialTheme.typography.bodySmall)
            }

            // Output Format Selection
            Text(
                text = "Output Format",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("JPEG", "PNG", "WebP").forEach { format ->
                    FilterChip(
                        selected = outputFormat == format,
                        onClick = {
                            outputFormat = format
                            if (originalBitmap != null && !isProcessing) {
                                compressImage(originalBitmap, quality, outputFormat) { bitmap, size ->
                                    compressedBitmap = bitmap
                                    compressedSize = size
                                }
                            }
                        },
                        label = { Text(format) }
                    )
                }
            }

            // Compressed Image Preview
            if (compressedBitmap != null) {
                Text(
                    text = "Compressed Preview",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            bitmap = compressedBitmap!!.asImageBitmap(),
                            contentDescription = "Compressed Image",
                            modifier = Modifier
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        
                        // Size Comparison
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatFileSize(originalSize),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = "Original",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val savings = if (originalSize > 0) {
                                        ((1 - compressedSize.toFloat() / originalSize) * 100).toInt()
                                    } else 0
                                    Text(
                                        text = "-$savings%",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = if (savings > 0) MaterialTheme.colorScheme.primary 
                                               else MaterialTheme.colorScheme.error
                                    )
                                    Text(
                                        text = "Saved",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = formatFileSize(compressedSize),
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Compressed ($outputFormat)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                        
                        // Save Button
                        if (!isProcessing) {
                            Button(
                                onClick = {
                                    scope.launch {
                                        isProcessing = true
                                        saveCompressedImage(context, compressedBitmap!!, outputFormat, quality)
                                        isProcessing = false
                                        showSaveMessage = true
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save Compressed Image")
                            }
                        } else {
                            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }

            if (showSaveMessage) {
                Snackbar(
                    modifier = Modifier.padding(8.dp)
                ) {
                    Text("Image saved successfully!")
                }
            }

            if (isProcessing) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}

private fun compressImage(
    bitmap: Bitmap?,
    quality: Int,
    format: String,
    onResult: (Bitmap?, Long) -> Unit
) {
    if (bitmap == null) {
        onResult(null, 0)
        return
    }
    
    val outputStream = ByteArrayOutputStream()
    val bitmapFormat = when (format) {
        "PNG" -> Bitmap.CompressFormat.PNG
        "WebP" -> Bitmap.CompressFormat.WEBP_LOSSY
        else -> Bitmap.CompressFormat.JPEG
    }
    
    // PNG doesn't use quality parameter
    val actualQuality = if (format == "PNG") 100 else quality
    
    bitmap.compress(bitmapFormat, actualQuality, outputStream)
    val compressedBytes = outputStream.toByteArray()
    val compressedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
    
    onResult(compressedBitmap, compressedBytes.size.toLong())
}

private suspend fun saveCompressedImage(
    context: Context,
    bitmap: Bitmap,
    format: String,
    quality: Int
) = withContext(Dispatchers.IO) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val extension = when (format) {
        "PNG" -> "png"
        "WebP" -> "webp"
        else -> "jpg"
    }
    val fileName = "Compressed_$timestamp.$extension"
    
    val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(
        android.os.Environment.DIRECTORY_PICTURES
    )
    val appDir = File(picturesDir, "ToolBox")
    if (!appDir.exists()) appDir.mkdirs()
    
    val file = File(appDir, fileName)
    val outputStream = FileOutputStream(file)
    
    val bitmapFormat = when (format) {
        "PNG" -> Bitmap.CompressFormat.PNG
        "WebP" -> Bitmap.CompressFormat.WEBP_LOSSY
        else -> Bitmap.CompressFormat.JPEG
    }
    val actualQuality = if (format == "PNG") 100 else quality
    
    bitmap.compress(bitmapFormat, actualQuality, outputStream)
    outputStream.flush()
    outputStream.close()
    
    // Notify media scanner
    val intent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    intent.data = android.net.Uri.fromFile(file)
    context.sendBroadcast(intent)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${bytes / 1024} KB"
    return String.format("%.2f MB", bytes / (1024.0 * 1024.0))
}
