package com.dannyk.toolbox.ui.screens.tools.image

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Crop
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
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

data class CropRegion(
    val left: Float = 0f,
    val top: Float = 0f,
    val right: Float = 1f,
    val bottom: Float = 1f
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageCropperScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var croppedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var cropRegion by remember { mutableStateOf(CropRegion()) }
    var selectedAspectRatio by remember { mutableStateOf("Free") }
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
                    // Reset crop region to full image
                    cropRegion = CropRegion()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    Scaffold(
        topBar = { ToolTopBar("Image Cropper", navController) }
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
                Text("Select Image to Crop")
            }

            if (originalBitmap != null) {
                // Aspect Ratio Selection
                Text(
                    text = "Aspect Ratio",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Free", "1:1", "4:3", "16:9").forEach { ratio ->
                        FilterChip(
                            selected = selectedAspectRatio == ratio,
                            onClick = {
                                selectedAspectRatio = ratio
                                applyAspectRatio(ratio, originalBitmap!!, cropRegion) { newRegion ->
                                    cropRegion = newRegion
                                    croppedBitmap = null
                                }
                            },
                            label = { Text(ratio) }
                        )
                    }
                }

                // Crop Preview Area
                Text(
                    text = "Crop Preview (drag corners to adjust)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(originalBitmap!!.width.toFloat() / originalBitmap!!.height.toFloat())
                            .padding(8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CropOverlay(
                            bitmap = originalBitmap!!,
                            cropRegion = cropRegion,
                            onCropRegionChanged = { newRegion -> cropRegion = newRegion }
                        )
                    }
                }

                // Current Crop Info
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val imgW = originalBitmap!!.width
                        val imgH = originalBitmap!!.height
                        val cropW = ((cropRegion.right - cropRegion.left) * imgW).toInt()
                        val cropH = ((cropRegion.bottom - cropRegion.top) * imgH).toInt()
                        
                        InfoRow("Original Size", "$imgW x $imgH px")
                        InfoRow("Crop Region", 
                            "${(cropRegion.left * 100).toInt()}%, ${(cropRegion.top * 100).toInt()}% to " +
                            "${(cropRegion.right * 100).toInt()}%, ${(cropRegion.bottom * 100).toInt()}%")
                        InfoRow("Output Size", "$cropW x $cropH px")
                    }
                }

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = { cropRegion = CropRegion(); croppedBitmap = null },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Reset")
                    }
                    
                    Button(
                        onClick = {
                            scope.launch {
                                isProcessing = true
                                croppedBitmap = performCrop(originalBitmap!!, cropRegion)
                                isProcessing = false
                            }
                        },
                        enabled = !isProcessing,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Crop, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        if (isProcessing) "Cropping..." else "Apply Crop"
                    }
                }
            }

            // Cropped Result
            if (croppedBitmap != null) {
                Text(
                    text = "Cropped Result",
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
                            bitmap = croppedBitmap!!.asImageBitmap(),
                            contentDescription = "Cropped Image",
                            modifier = Modifier
                                .height(200.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        
                        Text(
                            text = "${croppedBitmap!!.width} x ${croppedBitmap!!.height} px",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.primary,
                            fontWeight = FontWeight.Medium
                        )
                        
                        Button(
                            onClick = {
                                scope.launch {
                                    saveCroppedImage(context, croppedBitmap!!)
                                    showSaveMessage = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Save, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Save Cropped Image")
                        }
                    }
                }
            }

            if (showSaveMessage) {
                Snackbar(modifier = Modifier.padding(8.dp)) {
                    Text("Image saved successfully!")
                }
            }
        }
    }
}

@Composable
private fun CropOverlay(
    bitmap: Bitmap,
    cropRegion: CropRegion,
    onCropRegionChanged: (CropRegion) -> Unit
) {
    val density = LocalDensity.current
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    val canvasSize = size
                    val normalizedDeltaX = dragAmount.x / canvasSize.width
                    val normalizedDeltaY = dragAmount.y / canvasSize.height
                    
                    // Determine which edge is being dragged based on position
                    val touchX = change.position.x / canvasSize.width
                    val touchY = change.position.y / canvasSize.height
                    
                    val currentRegion = cropRegion
                    val tolerance = 0.15f
                    
                    val newRegion = when {
                        touchX < cropRegion.left + tolerance && touchY < cropRegion.top + tolerance -> {
                            // Top-left corner
                            currentRegion.copy(
                                left = (currentRegion.left + normalizedDeltaX).coerceIn(0f, currentRegion.right - 0.1f),
                                top = (currentRegion.top + normalizedDeltaY).coerceIn(0f, currentRegion.bottom - 0.1f)
                            )
                        }
                        touchX > cropRegion.right - tolerance && touchY < cropRegion.top + tolerance -> {
                            // Top-right corner
                            currentRegion.copy(
                                right = (currentRegion.right + normalizedDeltaX).coerceIn(currentRegion.left + 0.1f, 1f),
                                top = (currentRegion.top + normalizedDeltaY).coerceIn(0f, currentRegion.bottom - 0.1f)
                            )
                        }
                        touchX < cropRegion.left + tolerance && touchY > cropRegion.bottom - tolerance -> {
                            // Bottom-left corner
                            currentRegion.copy(
                                left = (currentRegion.left + normalizedDeltaX).coerceIn(0f, currentRegion.right - 0.1f),
                                bottom = (currentRegion.bottom + normalizedDeltaY).coerceIn(currentRegion.top + 0.1f, 1f)
                            )
                        }
                        touchX > cropRegion.right - tolerance && touchY > cropRegion.bottom - tolerance -> {
                            // Bottom-right corner
                            currentRegion.copy(
                                right = (currentRegion.right + normalizedDeltaX).coerceIn(currentRegion.left + 0.1f, 1f),
                                bottom = (currentRegion.bottom + normalizedDeltaY).coerceIn(currentRegion.top + 0.1f, 1f)
                            )
                        }
                        else -> currentRegion
                    }
                    onCropRegionChanged(newRegion)
                }
            }
    ) {
        // Draw the full image
        drawImage(bitmap.asImageBitmap())
        
        // Draw dark overlay outside crop area
        val overlayPath = Path().apply {
            // Full canvas
            addRect(androidx.compose.ui.geometry.Rect(Offset.Zero, size))
            // Subtract crop area
            val cropLeft = cropRegion.left * size.width
            val cropTop = cropRegion.top * size.height
            val cropRight = cropRegion.right * size.width
            val cropBottom = cropRegion.bottom * size.height
            addRect(androidx.compose.ui.geometry.Rect(
                Offset(cropLeft, cropTop), 
                Size(cropRight - cropLeft, cropBottom - cropTop)
            ))
            fillType = Path.FillType.EvenOdd
        }
        
        drawPath(path = overlayPath, color = Color.Black.copy(alpha = 0.5f))
        
        // Draw crop border
        val cropLeft = cropRegion.left * size.width
        val cropTop = cropRegion.top * size.height
        val cropRight = cropRegion.right * size.width
        val cropBottom = cropRegion.bottom * size.height
        
        drawRect(
            color = Color.White,
            topLeft = Offset(cropLeft, cropTop),
            size = Size(cropRight - cropLeft, cropBottom - cropTop),
            style = Stroke(width = 2.dp.toPx())
        )
        
        // Draw grid lines (rule of thirds)
        val innerWidth = cropRight - cropLeft
        val innerHeight = cropBottom - cropTop
        
        // Vertical lines
        for (i in 1..2) {
            val x = cropLeft + innerWidth * i / 3
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(x, cropTop),
                end = Offset(x, cropBottom),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Horizontal lines
        for (i in 1..2) {
            val y = cropTop + innerHeight * i / 3
            drawLine(
                color = Color.White.copy(alpha = 0.5f),
                start = Offset(cropLeft, y),
                end = Offset(cropRight, y),
                strokeWidth = 1.dp.toPx()
            )
        }
        
        // Draw corner handles
        val handleSize = 16.dp.toPx()
        val handleRadius = CornerRadius(handleSize / 2)
        
        listOf(
            Offset(cropLeft - handleSize/2, cropTop - handleSize/2),
            Offset(cropRight - handleSize/2, cropTop - handleSize/2),
            Offset(cropLeft - handleSize/2, cropBottom - handleSize/2),
            Offset(cropRight - handleSize/2, cropBottom - handleSize/2)
        ).forEach { offset ->
            drawRoundRect(
                color = Color.White,
                topLeft = offset,
                size = Size(handleSize, handleSize),
                radius = handleRadius
            )
        }
    }
}

private fun applyAspectRatio(
    aspectRatio: String,
    bitmap: Bitmap,
    currentRegion: CropRegion,
    onResult: (CropRegion) -> Unit
) {
    if (aspectRatio == "Free") return
    
    val ratios = mapOf(
        "1:1" to 1f,
        "4:3" to 4f / 3f,
        "16:9" to 16f / 9f
    )
    
    val targetRatio = ratios[aspectRatio] ?: return
    val centerX = (currentRegion.left + currentRegion.right) / 2
    val centerY = (currentRegion.top + currentRegion.bottom) / 2
    
    // Calculate max possible size maintaining center and aspect ratio
    val maxWidth = minOf(centerX, 1f - centerX) * 2
    val maxHeight = minOf(centerY, 1f - centerY) * 2
    
    val width: Float
    val height: Float
    
    if (maxWidth / maxHeight > targetRatio) {
        height = maxHeight * 0.9f
        width = height * targetRatio
    } else {
        width = maxWidth * 0.9f
        height = width / targetRatio
    }
    
    onResult(CropRegion(
        left = centerX - width / 2,
        top = centerY - height / 2,
        right = centerX + width / 2,
        bottom = centerY + height / 2
    ))
}

private suspend fun performCrop(
    bitmap: Bitmap,
    region: CropRegion
): Bitmap? = withContext(Dispatchers.IO) {
    try {
        val left = (region.left * bitmap.width).toInt().coerceIn(0, bitmap.width - 1)
        val top = (region.top * bitmap.height).toInt().coerceIn(0, bitmap.height - 1)
        val right = (region.right * bitmap.width).toInt().coerceIn(left + 1, bitmap.width)
        val bottom = (region.bottom * bitmap.height).toInt().coerceIn(top + 1, bitmap.height)
        
        val width = right - left
        val height = bottom - top
        
        Bitmap.createBitmap(bitmap, left, top, width, height)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

private suspend fun saveCroppedImage(
    context: Context,
    bitmap: Bitmap
) = withContext(Dispatchers.IO) {
    val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
    val fileName = "Cropped_$timestamp.jpg"
    
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

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = label, style = MaterialTheme.typography.bodySmall, 
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(text = value, style = MaterialTheme.typography.bodySmall, 
             fontWeight = FontWeight.Medium)
    }
}
