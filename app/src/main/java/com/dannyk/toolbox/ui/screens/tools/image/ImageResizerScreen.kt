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
import androidx.compose.foundation.text.KeyboardOptions
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolTopBar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.ui.graphics.Color
import android.content.Intent
import androidx.compose.material.icons.filled.Check

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageResizerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var resizedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var targetWidth by remember { mutableStateOf("") }
    var targetHeight by remember { mutableStateOf("") }
    var maintainAspectRatio by remember { mutableStateOf(true) }
    var usePercentage by remember { mutableStateOf(false) }
    var percentage by remember { mutableStateOf("50") }
    var isProcessing by remember { mutableStateOf(false) }
    var showSaveMessage by remember { mutableStateOf(false) }
    
    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes() ?: byteArrayOf()
                    originalBitmap = withContext(Dispatchers.IO) {
                        BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                    }
                    // Set default values based on original size
                    originalBitmap?.let { bmp ->
                        targetWidth = bmp.width.toString()
                        targetHeight = bmp.height.toString()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = { ToolTopBar("Image Resizer", navController) }
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
                                .height(180.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Original: ${originalBitmap!!.width} x ${originalBitmap!!.height} px",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Resize Options
                Text(
                    text = "Resize Options",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // Percentage vs Pixels toggle
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Resize Mode:", modifier = Modifier.weight(1f))
                            
                            FilterChip(
                                selected = usePercentage,
                                onClick = { usePercentage = true },
                                label = { Text("Percentage") }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            FilterChip(
                                selected = !usePercentage,
                                onClick = { usePercentage = false },
                                label = { Text("Pixels") }
                            )
                        }

                        if (usePercentage) {
                            // Percentage Input
                            OutlinedTextField(
                                value = percentage,
                                onValueChange = { newValue ->
                                    if (newValue.isEmpty() || (newValue.toIntOrNull() != null && 
                                        newValue.toInt() in 1..500)) {
                                        percentage = newValue
                                    }
                                },
                                label = { Text("Scale (%)") },
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                supportingText = { 
                                    Text("Enter 1-500. Values >100 upscale, <100 downscale") 
                                }
                            )
                            
                            // Show calculated dimensions
                            originalBitmap?.let { bmp ->
                                val pct = percentage.toFloatOrNull()?.div(100f) ?: 1f
                                val newW = (bmp.width * pct).toInt()
                                val newH = (bmp.height * pct).toInt()
                                Text(
                                    text = "Output: $newW x $newH px",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        } else {
                            // Width and Height Inputs
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                OutlinedTextField(
                                    value = targetWidth,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                                            targetWidth = newValue
                                            if (maintainAspectRatio && originalBitmap != null && 
                                                newValue.isNotEmpty()) {
                                                val w = newValue.toIntOrNull() ?: return@OutlinedTextField
                                                val ratio = originalBitmap!!.height.toFloat() / 
                                                           originalBitmap!!.width.toFloat()
                                                targetHeight = (w * ratio).toInt().toString()
                                            }
                                        }
                                    },
                                    label = { Text("Width (px)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                                
                                OutlinedTextField(
                                    value = targetHeight,
                                    onValueChange = { newValue ->
                                        if (newValue.isEmpty() || newValue.toIntOrNull() != null) {
                                            targetHeight = newValue
                                            if (maintainAspectRatio && originalBitmap != null && 
                                                newValue.isNotEmpty()) {
                                                val h = newValue.toIntOrNull() ?: return@OutlinedTextField
                                                val ratio = originalBitmap!!.width.toFloat() / 
                                                           originalBitmap!!.height.toFloat()
                                                targetWidth = (h * ratio).toInt().toString()
                                            }
                                        }
                                    },
                                    label = { Text("Height (px)") },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    singleLine = true,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        // Maintain Aspect Ratio Toggle
                        Row(
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Switch(
                                checked = maintainAspectRatio,
                                onCheckedChange = { maintainAspectRatio = it }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Maintain Aspect Ratio")
                        }

                        // Quick Size Presets
                        Text(
                            text = "Quick Presets:",
                            style = MaterialTheme.typography.labelLarge
                        )
                        
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(
                                "25%" to 25, "50%" to 50, "75%" to 75,
                                "100%" to 100, "150%" to 150, "200%" to 200
                            ).forEach { (label, value) ->
                                AssistChip(
                                    onClick = {
                                        usePercentage = true
                                        percentage = value.toString()
                                    },
                                    label = { Text(label) }
                                )
                            }
                        }

                        // Resize Button
                        Button(
                            onClick = {
                                scope.launch {
                                    isProcessing = true
                                    resizedBitmap = resizeImage(
                                        originalBitmap!!,
                                        targetWidth,
                                        targetHeight,
                                        percentage,
                                        usePercentage
                                    )
                                    isProcessing = false
                                }
                            },
                            enabled = !isProcessing && originalBitmap != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            if (isProcessing) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text("Resize Image")
                            }
                        }
                    }
                }
            }

            // Resized Image Preview
            if (resizedBitmap != null) {
                Text(
                    text = "Resized Result",
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
                            bitmap = resizedBitmap!!.asImageBitmap(),
                            contentDescription = "Resized Image",
                            modifier = Modifier
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        
                        Text(
                            text = "New Size: ${resizedBitmap!!.width} x ${resizedBitmap!!.height} px",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        // Size comparison info
                        originalBitmap?.let { orig ->
                            val origPixels = orig.width * orig.height
                            val newPixels = resizedBitmap!!.width * resizedBitmap!!.height
                            val change = ((newPixels.toFloat() / origPixels - 1) * 100).toInt()
                            Text(
                                text = "Pixel change: ${if (change >= 0) "+" else ""}$change%",
                                style = MaterialTheme.typography.bodySmall,
                                color = if (change <= 0) MaterialTheme.colorScheme.primary 
                                       else MaterialTheme.colorScheme.error
                            )
                        }
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    saveResizedImage(context, resizedBitmap!!)
                                    showSaveMessage = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Resized Image")
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
        }
    }
}

private suspend fun resizeImage(
    bitmap: Bitmap,
    widthStr: String,
    heightStr: String,
    percentageStr: String,
    usePercentage: Boolean
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val newWidth: Int
        val newHeight: Int
        
        if (usePercentage) {
            val scale = (percentageStr.toFloatOrNull() ?: 100f) / 100f
            newWidth = (bitmap.width * scale).toInt().coerceAtLeast(1)
            newHeight = (bitmap.height * scale).toInt().coerceAtLeast(1)
        } else {
            newWidth = (widthStr.toIntOrNull() ?: bitmap.width).coerceAtLeast(1)
            newHeight = (heightStr.toIntOrNull() ?: bitmap.height).coerceAtLeast(1)
        }
        
        Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun saveResizedImage(
    context: Context,
    bitmap: Bitmap
) = withContext(Dispatchers.IO) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "Resized_$timestamp.jpg"
    
    val picturesDir = android.os.Environment.getExternalStoragePublicDirectory(
        android.os.Environment.DIRECTORY_PICTURES
    )
    val appDir = File(picturesDir, "ToolBox")
    if (!appDir.exists()) appDir.mkdirs()
    
    val file = File(appDir, fileName)
    val outputStream = FileOutputStream(file)
    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, outputStream)
    outputStream.flush()
    outputStream.close()
    
    val intent = android.content.Intent(android.content.Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
    intent.data = android.net.Uri.fromFile(file)
    context.sendBroadcast(intent)
}
