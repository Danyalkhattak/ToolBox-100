package com.dannyk.toolbox.ui.screens.tools.files

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolScreenLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import androidx.compose.ui.graphics.Color
import android.os.Environment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PDFToImagesScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedPdfUri by remember { mutableStateOf<Uri?>(null) }
    var pdfFileName by remember { mutableStateOf<String?>(null) }
    var totalPages by remember { mutableStateOf(0) }
    
    // Options
    var startPage by remember { mutableStateOf("1") }
    var endPage by remember { mutableStateOf("") }
    var imageQuality by remember { mutableStateOf("High") }
    var imageFormat by remember { mutableStateOf("PNG") }
    var scaleOption by remember { mutableStateOf("1x") }
    
    var isProcessing by remember { mutableStateOf(false) }
    var extractedImages by remember { mutableStateOf<List<Pair<Int, Bitmap>>>(emptyList()) }
    var savedCount by remember { mutableStateOf(0) }

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedPdfUri = it
            pdfFileName = getFileNameFromUri(context, it)
            
            scope.launch {
                val pageCount = getPdfPageCount(context, it)
                totalPages = pageCount
                endPage = pageCount.toString()
            }
        }
    }

    ToolScreenLayout(
        title = "PDF to Images",
        navController = navController
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                // PDF Selection Section
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Select PDF File",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Button(
                            onClick = { pdfPickerLauncher.launch("application/pdf") },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Choose PDF")
                        }
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        if (selectedPdfUri == null) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(80.dp)
                                    .border(
                                        width = 2.dp,
                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "No PDF selected",
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
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.Image,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = pdfFileName ?: "PDF Document",
                                            style = MaterialTheme.typography.bodyMedium,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "$totalPages page(s)",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Page Range Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Page Range",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = startPage,
                                onValueChange = { 
                                    if (it.isEmpty() || (it.all { c -> c.isDigit() } && it.toIntOrNull()?.let { v -> v > 0 } == true)) {
                                        startPage = it 
                                    }
                                },
                                label = { Text("Start Page") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = selectedPdfUri != null
                            )
                            
                            Text(
                                text = "to",
                                style = MaterialTheme.typography.bodyLarge
                            )
                            
                            OutlinedTextField(
                                value = endPage,
                                onValueChange = { 
                                    if (it.isEmpty() || (it.all { c -> c.isDigit() } && it.toIntOrNull()?.let { v -> v > 0 } == true)) {
                                        endPage = it 
                                    }
                                },
                                label = { Text("End Page") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                enabled = selectedPdfUri != null
                            )
                        }
                        
                        if (totalPages > 0) {
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Total pages available: $totalPages",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            item {
                // Image Quality Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Image Quality / Scale",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("1x", "1.5x", "2x", "3x").forEach { scale ->
                                FilterChip(
                                    selected = scaleOption == scale,
                                    onClick = { scaleOption = scale },
                                    label = { Text(scale) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("Low", "Medium", "High", "Original").forEach { quality ->
                                FilterChip(
                                    selected = imageQuality == quality,
                                    onClick = { imageQuality = quality },
                                    label = { Text(quality) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Image Format Selection
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Output Format",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf("PNG", "JPEG", "WEBP").forEach { format ->
                                FilterChip(
                                    selected = imageFormat == format,
                                    onClick = { imageFormat = format },
                                    label = { Text(format) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }

            item {
                // Extract Button
                Button(
                    onClick = {
                        if (selectedPdfUri == null) {
                            Toast.makeText(context, "Please select a PDF file", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        val start = startPage.toIntOrNull() ?: 1
                        val end = endPage.toIntOrNull() ?: totalPages
                        
                        if (start < 1 || end < start || end > totalPages) {
                            Toast.makeText(context, "Invalid page range", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        
                        isProcessing = true
                        extractedImages = emptyList()
                        savedCount = 0
                        
                        scope.launch {
                            try {
                                val images = extractPagesAsImages(
                                    context = context,
                                    pdfUri = selectedPdfUri!!,
                                    startPage = start,
                                    endPage = end,
                                    scale = scaleOption.replace("x", "").toFloat(),
                                    format = imageFormat
                                )
                                
                                withContext(Dispatchers.Main) {
                                    extractedImages = images
                                    isProcessing = false
                                    
                                    Toast.makeText(
                                        context, 
                                        "Extracted ${images.size} page(s)", 
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                withContext(Dispatchers.Main) {
                                    isProcessing = false
                                    Toast.makeText(
                                        context, 
                                        "Error extracting pages: ${e.message}", 
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        }
                    },
                    enabled = selectedPdfUri != null && !isProcessing,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MaterialTheme.colorScheme.onPrimary
                        )
                    } else {
                        Icon(Icons.Default.Image, contentDescription = null)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isProcessing) "Extracting..." else "Extract Pages as Images")
                }
            }

            item {
                // Save All Button
                if (extractedImages.isNotEmpty()) {
                    OutlinedButton(
                        onClick = {
                            scope.launch {
                                var count = 0
                                for ((pageNumber, bitmap) in extractedImages) {
                                    val success = saveImageToGallery(
                                        context = context,
                                        bitmap = bitmap,
                                        fileName = "${pdfFileName?.substringBeforeLast(".") ?: "page"}_page$pageNumber",
                                        format = imageFormat
                                    )
                                    if (success) count++
                                }
                                
                                withContext(Dispatchers.Main) {
                                    savedCount = count
                                    Toast.makeText(
                                        context, 
                                        "Saved $count image(s) to Downloads/PDF_Images", 
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.Save, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Save All Images ($savedCount saved)")
                    }
                }
            }

            item {
                // Progress Indicator
                if (isProcessing) {
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
                                text = "Extracting pages from PDF...",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            item {
                // Extracted Images Grid
                if (extractedImages.isNotEmpty()) {
                    Card(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "Extracted Images (${extractedImages.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            LazyVerticalGrid(
                                columns = GridCells.Fixed(2),
                                modifier = Modifier.height((extractedImages.size * 180).coerceAtMost(600).dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(extractedImages) { (pageNumber, bitmap) ->
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(170.dp),
                                        shape = RoundedCornerShape(8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.fillMaxSize()
                                        ) {
                                            Image(
                                                bitmap = bitmap.asImageBitmap(),
                                                contentDescription = "Page $pageNumber",
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .weight(1f)
                                                    .border(
                                                        width = 1.dp,
                                                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                                    )
                                            )
                                            
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                                    .padding(4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    text = "Page $pageNumber",
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                                
                                                IconButton(
                                                    onClick = {
                                                        scope.launch {
                                                            saveImageToGallery(
                                                                context = context,
                                                                bitmap = bitmap,
                                                                fileName = "${pdfFileName?.substringBeforeLast(".") ?: "page"}_page$pageNumber",
                                                                format = imageFormat
                                                            )
                                                            withContext(Dispatchers.Main) {
                                                                Toast.makeText(context, "Image saved!", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    },
                                                    modifier = Modifier.size(28.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Save,
                                                        contentDescription = "Save",
                                                        modifier = Modifier.size(16.dp),
                                                        tint = MaterialTheme.colorScheme.primary
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            item {
                // Instructions
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "How to use:",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "• Select a PDF file from your device\n" +
                                   "• Choose the page range to extract\n" +
                                   "• Set image quality and scale factor\n" +
                                   "• Select output format (PNG, JPEG, WEBP)\n" +
                                   "• Tap 'Extract Pages' to convert\n" +
                                   "• View and save individual or all images",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

private fun getFileNameFromUri(context: Context, uri: Uri): String {
    var fileName = "document"
    context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
        if (cursor.moveToFirst()) {
            val nameIndex = cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
            if (nameIndex >= 0) {
                fileName = cursor.getString(nameIndex) ?: "document"
            }
        }
    }
    return fileName
}

private suspend fun getPdfPageCount(context: Context, uri: Uri): Int = 
    withContext(Dispatchers.IO) {
        var pageCount = 0
        try {
            val pfd = context.contentResolver.openFileDescriptor(uri, "r")
            pfd?.let {
                PdfRenderer(it).use { renderer ->
                    pageCount = renderer.pageCount
                }
                pfd.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        pageCount
    }

private suspend fun extractPagesAsImages(
    context: Context,
    pdfUri: Uri,
    startPage: Int,
    endPage: Int,
    scale: Float,
    format: String
): List<Pair<Int, Bitmap>> = withContext(Dispatchers.IO) {
    val images = mutableListOf<Pair<Int, Bitmap>>()
    
    try {
        val pfd = context.contentResolver.openFileDescriptor(pdfUri, "r")
        pfd?.let {
            PdfRenderer(it).use { renderer ->
                val actualEnd = minOf(endPage, renderer.pageCount)
                
                for (i in (startPage - 1) until actualEnd) {
                    try {
                        val page = renderer.openPage(i)
                        
                        val width = (page.width * scale).toInt()
                        val height = (page.height * scale).toInt()
                        
                        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        
                        images.add(Pair(i + 1, bitmap))
                    } catch (e: Exception) {
                        // Skip problematic pages
                    }
                }
            }
            pfd.close()
        }
    } catch (e: Exception) {
        throw e
    }
    
    images
}

private suspend fun saveImageToGallery(
    context: Context,
    bitmap: Bitmap,
    fileName: String,
    format: String
): Boolean = withContext(Dispatchers.IO) {
    try {
        val dir = File(
            android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS),
            "PDF_Images"
        )
        if (!dir.exists()) {
            dir.mkdirs()
        }
        
        val extension = when (format.uppercase()) {
            "JPEG", "JPG" -> "jpg"
            "WEBP" -> "webp"
            else -> "png"
        }
        
        val file = File(dir, "${fileName}_${System.currentTimeMillis()}.$extension")
        
        FileOutputStream(file).use { outputStream ->
            when (format.uppercase()) {
                "JPEG", "JPG" -> bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                "WEBP" -> bitmap.compress(Bitmap.CompressFormat.WEBP, 90, outputStream)
                else -> bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream)
            }
        }
        
        true
    } catch (e: Exception) {
        e.printStackTrace()
        false
    }
}
