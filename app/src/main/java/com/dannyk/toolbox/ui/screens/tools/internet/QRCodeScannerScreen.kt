package com.dannyk.toolbox.ui.screens.tools.internet

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import com.google.zxing.Result
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import java.util.concurrent.Executors
import androidx.compose.ui.text.font.FontWeight
import android.content.ClipboardManager
import android.content.ClipData
import androidx.compose.ui.view.AndroidView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QRCodeScannerScreen(
    navController: androidx.navigation.NavHostController,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
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
    var isScanning by remember { mutableStateOf(true) }
    var showCopiedMessage by remember { mutableStateOf(false) }
    var scanHistory by remember { mutableStateOf<List<ScanHistoryItem>>(emptyList()) }
    
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
                title = "QR Code Scanner",
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
                    .weight(1f, fill = true),
                contentAlignment = Alignment.Center
            ) {
                if (hasCameraPermission) {
                    // ZXing Barcode Scanner View
                    AndroidView(
                        factory = { ctx ->
                            DecoratedBarcodeView(ctx).apply {
                                val formats = listOf(com.google.zxing.BarcodeFormat.QR_CODE)
                                barcodeView.decoderFactory = 
                                    com.journeyapps.barcodescanner.DefaultDecoderFactory(formats)
                                
                                decodeContinuous(object : BarcodeCallback {
                                    override fun barcodeResult(result: BarcodeResult?) {
                                        result?.let { res ->
                                            if (isScanning && res.text.isNotBlank()) {
                                                scannedContent = res.text
                                                scannedFormat = res.barcodeFormat?.name ?: "QR_CODE"
                                                isScanning = false
                                                
                                                // Add to history
                                                val newItem = ScanHistoryItem(
                                                    content = res.text,
                                                    format = res.barcodeFormat?.name ?: "QR_CODE",
                                                    timestamp = System.currentTimeMillis()
                                                )
                                                scanHistory = listOf(newItem) + scanHistory.take(19)
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
                        // Corner brackets for scan area
                        ScanOverlay(isScanning = isScanning)
                        
                        if (!isScanning && scannedContent != null) {
                            // Success overlay
                            Surface(
                                color = Color.Black.copy(alpha = 0.7f),
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
                                modifier = Modifier.size(80.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                            )
                            
                            Spacer(modifier = Modifier.height(16.dp))
                            
                            Text(
                                text = "Camera Permission Required",
                                style = MaterialTheme.typography.titleMedium
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Please grant camera permission to scan QR codes.",
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
                            Text(
                                text = "✓ Scanned",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = scannedFormat ?: "QR_CODE",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Surface(
                            color = MaterialTheme.colorScheme.surface,
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                text = scannedContent!!,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontFamily = FontFamily.Monospace
                                ),
                                maxLines = 5,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(12.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedButton(
                                onClick = {
                                    copyToClipboard(context, scannedContent!!)
                                    showCopiedMessage = true
                                },
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = null)
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }

                            // Open URL button if it's a URL
                            if (scannedContent!!.startsWith("http://") || scannedContent!!.startsWith("https://")) {
                                Button(
                                    onClick = {
                                        try {
                                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(scannedContent))
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            Toast.makeText(context, "Cannot open this URL", Toast.LENGTH_SHORT).show()
                                        }
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.OpenInNew, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Open")
                                }
                            } else {
                                OutlinedButton(
                                    onClick = {
                                        isScanning = true
                                        scannedContent = null
                                    },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Icon(Icons.Default.QrCodeScanner, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Scan Again")
                                }
                            }
                        }

                        if (!scannedContent!!.startsWith("http")) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Button(
                                onClick = {
                                    isScanning = true
                                    scannedContent = null
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Scan Another")
                            }
                        }

                        if (showCopiedMessage) {
                            Text(
                                text = "Copied to clipboard!",
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.bodySmall
                            )
                            LaunchedEffect(Unit) {
                                kotlinx.coroutines.delay(2000)
                                showCopiedMessage = false
                            }
                        }
                    }
                }
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
                            text = "Point camera at a QR code",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scanning will start automatically...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Scan History
            if (scanHistory.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Recent Scans",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                scanHistory.take(3).forEach { item ->
                    HistoryItemCard(item = item, context = context)
                    Spacer(modifier = Modifier.height(6.dp))
                }
            }

            // Info Section
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
                        text = "Supported Formats",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(6.dp))
                    
                    Text(
                        text = "QR Code (all types): URLs, Text, Wi-Fi, vCard, Email, Phone, etc.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun ScanOverlay(
    isScanning: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(48.dp),
        contentAlignment = Alignment.Center
    ) {
        // Corner brackets
        val cornerColor = Color.White
        val cornerLength = 40.dp
        val cornerThickness = 4.dp
        
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
                    .background(Color.Green.copy(alpha = 0.8f))
                    .offset(y = ((linePosition - 0.5f) * 250).dp)
            )
        }
    }
}

@Composable
private fun HistoryItemCard(
    item: ScanHistoryItem,
    context: Context
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.content.take(40) + if (item.content.length > 40) "..." else "",
                    style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
                    maxLines = 1
                )
                Text(
                    text = formatTimestamp(item.timestamp),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            IconButton(
                onClick = { copyToClipboard(context, item.content) },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.ContentCopy,
                    contentDescription = "Copy",
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

data class ScanHistoryItem(
    val content: String,
    val format: String,
    val timestamp: Long
)

private fun formatTimestamp(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm:ss", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

private fun copyToClipboard(context: Context, text: String) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
    val clip = android.content.ClipData.newPlainText("QR Content", text)
    clipboardManager.setPrimaryClip(clip)
}
