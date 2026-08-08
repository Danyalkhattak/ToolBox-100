package com.dannyk.toolbox.ui.screens.tools.internet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import com.google.zxing.BarcodeFormat
import androidx.compose.ui.text.font.FontWeight
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.ui.view.AndroidView
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.LaunchedEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarcodeScannerScreen(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    
    var scannedContent by remember { mutableStateOf<String?>(null) }
    var scannedFormat by remember { mutableStateOf<String?>(null) }
    var scannedFormatType by remember { mutableStateOf<BarcodeFormat?>(null) }
    var isScanning by remember { mutableStateOf(true) }
    var showCopiedMessage by remember { mutableStateOf(false) }
    
    // Scan History - persisted in memory (could use DataStore/Room for persistence)
    var scanHistory by remember { 
        mutableStateOf(loadScanHistory(context))
    }
    
    // Selected format filter
    var selectedFilter by remember { mutableStateOf<String?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "Camera permission required for scanning", Toast.LENGTH_SHORT).show()
        }
    }

    // Request permission on first launch
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Scaffold(
        topBar = {
            ToolTopAppBar(
                title = "Barcode Scanner",
                navController = navController
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Camera View / Scanner Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    // ZXing Barcode Scanner View - Multiple formats
                    AndroidView(
                        factory = { ctx ->
                            DecoratedBarcodeView(ctx).apply {
                                // Support all common barcode formats
                                val formats = listOf(
                                    BarcodeFormat.QR_CODE,
                                    BarcodeFormat.CODE_39,
                                    BarcodeFormat.CODE_128,
                                    BarcodeFormat.EAN_8,
                                    BarcodeFormat.EAN_13,
                                    BarcodeFormat.UPC_A,
                                    BarcodeFormat.UPC_E,
                                    BarcodeFormat.DATA_MATRIX,
                                    BarcodeFormat.PDF_417,
                                    BarcodeFormat.AZTEC,
                                    BarcodeFormat.ITF,
                                    BarcodeFormat.CODABAR
                                )
                                barcodeView.decoderFactory = 
                                    com.journeyapps.barcodescanner.DefaultDecoderFactory(formats)
                                
                                decodeContinuous(object : BarcodeCallback {
                                    override fun barcodeResult(result: BarcodeResult?) {
                                        result?.let { res ->
                                            if (isScanning && res.text.isNotBlank()) {
                                                scannedContent = res.text
                                                scannedFormat = getFormatDisplayName(res.barcodeFormat)
                                                scannedFormatType = res.barcodeFormat
                                                isScanning = false
                                                
                                                // Add to history
                                                val newItem = ScanHistoryItem(
                                                    content = res.text,
                                                    format = getFormatDisplayName(res.barcodeFormat),
                                                    formatType = res.barcodeFormat?.name ?: "UNKNOWN",
                                                    timestamp = System.currentTimeMillis()
                                                )
                                                scanHistory = listOf(newItem) + scanHistory.take(49)
                                                saveScanHistory(context, scanHistory)
                                            }
                                        }
                                    }
                                    
                                    override fun possibleResultPoints(resultPoints: MutableList<com.google.zxing.ResultPoint>?) {
                                        // Optional: Show scanning animation points
                                    }
                                })
                            }
                        },
                        modifier = Modifier.fillMaxSize()
                    )
                    
                    // Scanning overlay
                    Box(modifier = Modifier.matchParentSize()) {
                        BarcodeScanOverlay(isScanning = isScanning)
                        
                        if (!isScanning && scannedContent != null) {
                            Surface(
                                color = Color.Black.copy(alpha = 0.6f),
                                modifier = Modifier.matchParentSize()
                            )
                        }
                    }
                } else {
                    // No permission state
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Camera Permission Required",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Please grant camera permission to scan barcodes.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Button(onClick = {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }) {
                                Text("Grant Permission")
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result Display
            if (scannedContent != null) {
                ResultCard(
                    content = scannedContent!!,
                    format = scannedFormat!!,
                    formatType = scannedFormatType,
                    context = context,
                    onCopy = {
                        copyToClipboard(context, scannedContent!!)
                        showCopiedMessage = true
                    },
                    onOpenUrl = {
                        try {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedContent))
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            Toast.makeText(context, "Cannot open this URL", Toast.LENGTH_SHORT).show()
                        }
                    },
                    onScanAgain = {
                        isScanning = true
                        scannedContent = null
                    },
                    showCopiedMessage = showCopiedMessage,
                    onShowCopiedChanged = { showCopiedMessage = it }
                )
            } else if (hasCameraPermission) {
                // Instructions when scanning
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Point camera at a barcode",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Supports QR Code, Code 39, Code 128, EAN, UPC, and more",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Scan History Section
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan History",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    if (scanHistory.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Surface(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = CircleShape
                        ) {
                            Text(
                                text = "${scanHistory.size}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                
                if (scanHistory.isNotEmpty()) {
                    TextButton(onClick = {
                        scanHistory = emptyList()
                        saveScanHistory(context, emptyList())
                    }) {
                        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clear")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Format Filter Chips
            if (scanHistory.isNotEmpty()) {
                val uniqueFormats = scanHistory.map { it.format }.distinct().sorted()
                
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedFilter == null,
                        onClick = { selectedFilter = null },
                        label = { Text("All") }
                    )
                    
                    uniqueFormats.forEach { format ->
                        FilterChip(
                            selected = selectedFilter == format,
                            onClick = { selectedFilter = if (selectedFilter == format) null else format },
                            label = { Text(format) }
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
            }

            // History List
            if (scanHistory.isEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(
                        modifier = Modifier.padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No scans yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                val filteredHistory = if (selectedFilter != null) {
                    scanHistory.filter { it.format == selectedFilter }
                } else {
                    scanHistory
                }
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredHistory, key = { it.timestamp.toString() }) { item ->
                        BarcodeHistoryItemCard(
                            item = item,
                            context = context,
                            onCopy = {
                                copyToClipboard(context, item.content)
                                showCopiedMessage = true
                            },
                            onDelete = {
                                scanHistory = scanHistory.filter { it.timestamp != item.timestamp }
                                saveScanHistory(context, scanHistory)
                            }
                        )
                    }
                }
            }

            // Supported Formats Info
            Spacer(modifier = Modifier.height(16.dp))
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Supported Barcode Formats",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Spacer(modifier = Modifier.height(10.dp))
                    
                    val formats = listOf(
                        "QR Code" to "URLs, text, Wi-Fi, vCard, etc.",
                        "Code 39" to "General purpose alphanumeric",
                        "Code 128" to "High-density alphanumeric",
                        "EAN-13" to "Retail products (13 digits)",
                        "EAN-8" to "Small retail products (8 digits)",
                        "UPC-A" to "US retail products (12 digits)",
                        "UPC-E" to "Compressed UPC (6 digits)",
                        "Data Matrix" to "2D barcode, small size",
                        "PDF 417" to "2D stacked linear",
                        "Aztec" to "2D, efficient encoding"
                    )
                    
                    formats.chunked(2).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            row.forEach { (format, desc) ->
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = format,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium)
                                    )
                                    Text(
                                        text = desc,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            // Fill remaining space if odd number of items
                            if (row.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                        if (row != formats.lastOrNull()?.let { listOf(it) }) {
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResultCard(
    content: String,
    format: String,
    formatType: BarcodeFormat?,
    context: Context,
    onCopy: () -> Unit,
    onOpenUrl: () -> Unit,
    onScanAgain: () -> Unit,
    showCopiedMessage: Boolean,
    onShowCopiedChanged: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(
                        text = format,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                Text(
                    text = "✓ Scanned Successfully",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Surface(
                color = MaterialTheme.colorScheme.surface,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = content,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontFamily = FontFamily.Monospace
                    ),
                    maxLines = 5,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(12.dp)
                )
            }

            if (showCopiedMessage) {
                Text(
                    text = "Copied to clipboard!",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    onShowCopiedChanged(false)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onCopy,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy")
                }

                // Open URL button if it's a URL or looks like one
                val isUrl = content.startsWith("http://") || content.startsWith("https://") ||
                           content.startsWith("www.") || format.contains("QR", ignoreCase = true)
                
                if (isUrl && (content.startsWith("http") || content.startsWith("www"))) {
                    Button(
                        onClick = onOpenUrl,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCode2, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Open")
                    }
                } else {
                    OutlinedButton(
                        onClick = onScanAgain,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Scan Again")
                    }
                }
            }
            
            if (!isUrl || !content.startsWith("http")) {
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onScanAgain,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Scan Another")
                }
            }
        }
    }
}

@Composable
private fun BarcodeHistoryItemCard(
    item: ScanHistoryItem,
    context: Context,
    onCopy: () -> Unit,
    onDelete: () -> Void?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Format indicator
            Surface(
                color = getFormatColor(item.formatType).copy(alpha = 0.15f),
                shape = RoundedCornerShape(4.dp)
            ) {
                Text(
                    text = item.format.take(3).uppercase(),
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                    color = getFormatColor(item.formatType),
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.content.take(35) + if (item.content.length > 35) "..." else "",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = item.format,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatTimestamp(item.timestamp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(18.dp)
                )
            }
            
            IconButton(onClick = { onDelete() }, modifier = Modifier.size(36.dp)) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    modifier = Modifier.size(18.dp),
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
private fun BarcodeScanOverlay(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        contentAlignment = Alignment.Center
    ) {
        // Corner brackets
        val cornerColor = Color.White
        val cornerLength = 30.dp
        val cornerThickness = 3.dp
        
        // Top-left corner
        Box(modifier = Modifier.align(Alignment.TopStart)) {
            Box(
                modifier = Modifier
                    .width(cornerLength)
                    .height(cornerThickness)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .width(cornerThickness)
                    .height(cornerLength)
                    .background(cornerColor)
            )
        }
        
        // Top-right corner
        Box(modifier = Modifier.align(Alignment.TopEnd)) {
            Box(
                modifier = Modifier
                    .width(cornerLength)
                    .height(cornerThickness)
                    .background(cornerColor)
            )
            Box(
                modifier = Modifier
                    .width(cornerThickness)
                    .height(cornerLength)
                    .background(cornerColor)
                    .align(Alignment.TopEnd)
            )
        }
        
        // Bottom-left corner
        Box(modifier = Modifier.align(Alignment.BottomStart)) {
            Box(
                modifier = Modifier
                    .width(cornerLength)
                    .height(cornerThickness)
                    .background(cornerColor)
                    .align(Alignment.BottomStart)
            )
            Box(
                modifier = Modifier
                    .width(cornerThickness)
                    .height(cornerLength)
                    .background(cornerColor)
                    .align(Alignment.BottomStart)
            )
        }
        
        // Bottom-right corner
        Box(modifier = Modifier.align(Alignment.BottomEnd)) {
            Box(
                modifier = Modifier
                    .width(cornerLength)
                    .height(cornerThickness)
                    .background(cornerColor)
                    .align(Alignment.BottomEnd)
            )
            Box(
                modifier = Modifier
                    .width(cornerThickness)
                    .height(cornerLength)
                    .background(cornerColor)
                    .align(Alignment.BottomEnd)
            )
        }
        
        // Scanning line animation
        if (isScanning) {
            var linePosition by remember { mutableStateOf(0f) }
            
            LaunchedEffect(Unit) {
                while (true) {
                    kotlinx.coroutines.delay(50)
                    linePosition = if (linePosition >= 1f) 0f else linePosition + 0.02f
                }
            }
            
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(2.dp)
                    .background(Color.Green.copy(alpha = 0.7f))
                    .offset(y = ((linePosition - 0.5f) * 180).dp)
            )
        }
    }
}

// Helper functions and data classes

data class ScanHistoryItem(
    val content: String,
    val format: String,
    val formatType: String,
    val timestamp: Long
)

private fun getFormatDisplayName(format: BarcodeFormat?): String {
    return when (format) {
        BarcodeFormat.QR_CODE -> "QR Code"
        BarcodeFormat.CODE_39 -> "Code 39"
        BarcodeFormat.CODE_128 -> "Code 128"
        BarcodeFormat.EAN_8 -> "EAN-8"
        BarcodeFormat.EAN_13 -> "EAN-13"
        BarcodeFormat.UPC_A -> "UPC-A"
        BarcodeFormat.UPC_E -> "UPC-E"
        BarcodeFormat.DATA_MATRIX -> "Data Matrix"
        BarcodeFormat.PDF_417 -> "PDF 417"
        BarcodeFormat.AZTEC -> "Aztec"
        BarcodeFormat.ITF -> "ITF"
        BarcodeFormat.CODABAR -> "Codabar"
        else -> format?.name ?: "Unknown"
    }
}

private fun getFormatColor(formatType: String?): Color {
    return when (formatType?.uppercase()) {
        "QR_CODE" -> Color(0xFF4CAF50)
        "CODE_39", "CODE_128" -> Color(0xFF2196F3)
        "EAN_8", "EAN_13", "UPC_A", "UPC_E" -> Color(0xFFFF9800)
        "DATA_MATRIX", "PDF_417", "AZTEC" -> Color(0xFF9C27B0)
        else -> Color.Gray
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000L -> "Just now"
        diff < 3600_000L -> "${diff / 60_000}m ago"
        diff < 86400_000L -> "${diff / 3600_000}h ago"
        else -> {
            val sdf = java.text.SimpleDateFormat("MMM d, HH:mm", java.util.Locale.getDefault())
            sdf.format(java.util.Date(timestamp))
        }
    }
}

// Simple persistence using SharedPreferences
private const val PREFS_NAME = "barcode_scanner_prefs"
private const val HISTORY_KEY = "scan_history"

private fun saveScanHistory(context: Context, history: List<ScanHistoryItem>) {
    try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val json = org.json.JSONArray()
        
        history.forEach { item ->
            val obj = org.json.JSONObject().apply {
                put("content", item.content)
                put("format", item.format)
                put("formatType", item.formatType)
                put("timestamp", item.timestamp)
            }
            json.put(obj)
        }
        
        prefs.edit().putString(HISTORY_KEY, json.toString()).apply()
    } catch (_: Exception) {}
}

private fun loadScanHistory(context: Context): List<ScanHistoryItem> {
    return try {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val jsonString = prefs.getString(HISTORY_KEY, null) ?: return emptyList()
        
        val json = org.json.JSONArray(jsonString)
        val list = mutableListOf<ScanHistoryItem>()
        
        for (i in 0 until json.length()) {
            val obj = json.getJSONObject(i)
            list.add(ScanHistoryItem(
                content = obj.getString("content"),
                format = obj.getString("format"),
                formatType = obj.getString("formatType"),
                timestamp = obj.getLong("timestamp")
            ))
        }
        
        list
    } catch (_: Exception) {
        emptyList()
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("Barcode Content", text)
    clipboardManager.setPrimaryClip(clip)
}
