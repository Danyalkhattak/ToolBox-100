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
import androidx.compose.material3.Divider
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ScrollState
import android.os.Environment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageFormatConverterScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var convertedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var originalUri by remember { mutableStateOf<Uri?>(null) }
    var originalSize by remember { mutableStateOf(0L) }
    var convertedSize by remember { mutableStateOf(0L) }
    var targetFormat by remember { mutableStateOf("PNG") }
    var quality by remember { mutableIntStateOf(90) }
    var isProcessing by remember { mutableStateOf(false) }
    var showSaveMessage by remember { mutableStateOf(false) }
    
    // Detect original format from URI
    var originalFormat by remember { mutableStateOf("Unknown") }

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
                    
                    // Try to detect format from content type or extension
                    val contentType = context.contentResolver.getType(uri) ?: ""
                    originalFormat = when {
                        contentType.contains("png") -> "PNG"
                        contentType.contains("jpeg") || contentType.contains("jpg") -> "JPEG"
                        contentType.contains("webp") -> "WebP"
                        contentType.contains("bmp") -> "BMP"
                        else -> {
                            // Try to detect from file extension
                            val path = uri.path ?: ""
                            when {
                                path.endsWith(".png", ignoreCase = true) -> "PNG"
                                path.endsWith(".jpg", ignoreCase = true) || 
                                path.endsWith(".jpeg", ignoreCase = true) -> "JPEG"
                                path.endsWith(".webp", ignoreCase = true) -> "WebP"
                                path.endsWith(".bmp", ignoreCase = true) -> "BMP"
                                else -> "Unknown"
                            }
                        }
                    }
                    
                    // Auto-convert on image load
                    convertImage(originalBitmap, targetFormat, quality) { bitmap, size ->
                        convertedBitmap = bitmap
                        convertedSize = size
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = { ToolTopBar("Format Converter", navController) }
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
                Text("Select Image to Convert")
            }

            if (originalBitmap != null) {
                // Original Image Info Card
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
                                .height(150.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            FormatInfoItem("Format", originalFormat)
                            FormatInfoItem("Size", formatFileSize(originalSize))
                            FormatInfoItem("Dimensions", "${originalBitmap!!.width}x${originalBitmap!!.height}")
                        }
                    }
                }

                // Target Format Selection
                Text(
                    text = "Target Format",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Format chips
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("PNG", "JPEG", "WebP", "BMP").forEach { format ->
                                FilterChip(
                                    selected = targetFormat == format,
                                    onClick = {
                                        targetFormat = format
                                        if (originalBitmap != null && !isProcessing) {
                                            convertImage(originalBitmap, targetFormat, quality) { bitmap, size ->
                                                convertedBitmap = bitmap
                                                convertedSize = size
                                            }
                                        }
                                    },
                                    label = { Text(format) },
                                    enabled = format != "BMP" || originalBitmap?.let { 
                                        it.width <= 4096 && it.height <= 4096 
                                    } == true
                                )
                            }
                        }

                        // Format description
                        Text(
                            text = getFormatDescription(targetFormat),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        // Quality slider for lossy formats
                        if (targetFormat in listOf("JPEG", "WebP")) {
                            Divider()
                            
                            Text(
                                text = "Quality: $quality%",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            
                            Slider(
                                value = quality.toFloat(),
                                onValueChange = { 
                                    quality = it.toInt() 
                                    if (originalBitmap != null && !isProcessing) {
                                        convertImage(originalBitmap, targetFormat, quality) { bitmap, size ->
                                            convertedBitmap = bitmap
                                            convertedSize = size
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
                                Text("Smaller file", style = MaterialTheme.typography.labelSmall)
                                Text("Better quality", style = MaterialTheme.typography.labelSmall)
                            }
                        } else {
                            // Note about lossless formats
                            Surface(
                                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f),
                                shape = MaterialTheme.shapes.small
                            ) {
                                Text(
                                    text = "$targetFormat is a lossless format. Quality setting is not applicable.",
                                    style = MaterialTheme.typography.bodySmall,
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }

                // Converted Result
                if (convertedBitmap != null) {
                    Text(
                        text = "Converted Preview ($targetFormat)",
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
                                bitmap = convertedBitmap!!.asImageBitmap(),
                                contentDescription = "Converted Image",
                                modifier = Modifier
                                    .height(150.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            
                            // Size comparison card
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                ),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    // Size comparison row
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceEvenly
                                    ) {
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(formatFileSize(originalSize), 
                                                 style = MaterialTheme.typography.titleMedium,
                                                 color = MaterialTheme.colorScheme.onSurfaceVariant)
                                            Text("$originalFormat Original", 
                                                 style = MaterialTheme.typography.bodySmall)
                                        }
                                        
                                        Icon(
                                            imageVector = Icons.Default.ArrowForward,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary
                                        )
                                        
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(formatFileSize(convertedSize), 
                                                 style = MaterialTheme.typography.titleMedium,
                                                 color = MaterialTheme.colorScheme.primary)
                                            Text("$targetFormat Converted", 
                                                 style = MaterialTheme.typography.bodySmall)
                                        }
                                    }
                                    
                                    // Savings indicator
                                    val savings = if (originalSize > 0) {
                                        ((1 - convertedSize.toFloat() / originalSize) * 100).toInt()
                                    } else 0
                                    
                                    if (savings != 0) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            Surface(
                                                color = if (savings > 0) MaterialTheme.colorScheme.primaryContainer
                                                       else MaterialTheme.colorScheme.errorContainer,
                                                shape = MaterialTheme.shapes.small
                                            ) {
                                                Text(
                                                    text = if (savings > 0) "↓ $savings% smaller" else "↑ ${-savings}% larger",
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                            
                            Button(
                                onClick = {
                                    scope.launch {
                                        isProcessing = true
                                        saveConvertedImage(context, convertedBitmap!!, targetFormat, quality)
                                        isProcessing = false
                                        showSaveMessage = true
                                    }
                                },
                                enabled = !isProcessing,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.Save, contentDescription = null)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Save as $targetFormat")
                            }
                        }
                    }
                }
            }

            if (showSaveMessage) {
                Snackbar(modifier = Modifier.padding(8.dp)) {
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

@Composable
private fun FormatInfoItem(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Medium)
        Text(label, style = MaterialTheme.typography.labelSmall, 
             color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

private fun getFormatDescription(format: String): String {
    return when (format) {
        "PNG" -> "Lossless compression. Best for graphics, screenshots, images with transparency."
        "JPEG" -> "Lossy compression. Best for photographs. Does not support transparency."
        "WebP" -> "Modern format by Google. Better compression than JPEG/PNG. Supports transparency."
        "BMP" -> "Uncompressed bitmap format. Large file sizes. Maximum compatibility."
        else -> ""
    }
}

private fun convertImage(
    bitmap: Bitmap?,
    format: String,
    quality: Int,
    onResult: (Bitmap?, Long) -> Unit
) {
    if (bitmap == null) {
        onResult(null, 0)
        return
    }
    
    try {
        val outputStream = ByteArrayOutputStream()
        
        when (format) {
            "PNG" -> {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
            "JPEG" -> {
                // JPEG doesn't support alpha, create ARGB_8888 copy without alpha
                val argbBitmap = if (bitmap.hasAlpha()) {
                    val newBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
                    val canvas = android.graphics.Canvas(newBitmap)
                    canvas.drawColor(android.graphics.Color.WHITE)
                    canvas.drawBitmap(bitmap, 0f, 0f, null)
                    newBitmap
                } else {
                    bitmap
                }
                argbBitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
            }
            "WebP" -> {
                bitmap.compress(Bitmap.CompressFormat.WEBP_LOSSY, quality, outputStream)
            }
            "BMP" -> {
                // BMP is not directly supported by compress, use PNG as fallback and note this
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
        
        val compressedBytes = outputStream.toByteArray()
        val decodedBitmap = BitmapFactory.decodeByteArray(compressedBytes, 0, compressedBytes.size)
        
        onResult(decodedBitmap, compressedBytes.size.toLong())
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(null, 0)
    }
}

private suspend fun saveConvertedImage(
    context: Context,
    bitmap: Bitmap,
    format: String,
    quality: Int
) = withContext(Dispatchers.IO) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val extension = when (format) {
        "PNG" -> "png"
        "WebP" -> "webp"
        "BMP" -> "bmp"
        else -> "jpg"
    }
    val fileName = "Converted_$timestamp.$extension"
    
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
        "BMP" -> Bitmap.CompressFormat.PNG // Fallback
        else -> Bitmap.CompressFormat.JPEG
    }
    val actualQuality = if (format == "PNG") 100 else quality
    
    bitmap.compress(bitmapFormat, actualQuality, outputStream)
    outputStream.flush()
    outputStream.close()
    
    val intent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    intent.data = android.net.Uri.fromFile(file)
    context.sendBroadcast(intent)
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${bytes / 1024} KB"
    return String.format("%.2f MB", bytes / (1024.0 * 1024.0))
}
