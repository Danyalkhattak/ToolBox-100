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
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dannyk.toolbox.ui.components.ToolTopAppBar
import com.journeyapps.barcodescanner.BarcodeCallback
import com.journeyapps.barcodescanner.BarcodeResult
import com.journeyapps.barcodescanner.DecoratedBarcodeView
import kotlinx.coroutines.delay

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
    var scanHistory by remember { mutableStateOf<List<QRScanHistoryItem>>(emptyList()) }

    // Reference to the scanner view for lifecycle management
    val barcodeViewRef = remember { mutableStateOf<DecoratedBarcodeView?>(null) }

    // Permission launcher
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
        if (!granted) {
            Toast.makeText(context, "Camera permission required for scanning", Toast.LENGTH_SHORT).show()
        }
    }

    // Request permission on first launch if not already granted
    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Lifecycle observer to start/stop the barcode view
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> barcodeViewRef.value?.resume()
                Lifecycle.Event.ON_PAUSE -> barcodeViewRef.value?.pause()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
        // Full-screen root Box – camera fills all space, overlays float on top
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraPermission) {
                // Camera preview
                AndroidView(
                    factory = { ctx ->
                        DecoratedBarcodeView(ctx).apply {
                            barcodeViewRef.value = this  // capture for lifecycle
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

                                            // Add to history (keep up to 20 entries)
                                            val newItem = QRScanHistoryItem(
                                                content = res.text,
                                                format = res.barcodeFormat?.name ?: "QR_CODE",
                                                timestamp = System.currentTimeMillis()
                                            )
                                            scanHistory = listOf(newItem) + scanHistory.take(19)
                                        }
                                    }
                                }

                                override fun possibleResultPoints(
                                    resultPoints: MutableList<com.google.zxing.ResultPoint>?
                                ) {
                                    // Optional: display aiming points
                                }
                            })
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // Scanning corner brackets + animated line (only when actively scanning)
                ScanOverlay(
                    isScanning = isScanning,
                    modifier = Modifier.matchParentSize()
                )

                // Success overlay when a code has been scanned
                if (!isScanning && scannedContent != null) {
                    Surface(
                        color = Color.Black.copy(alpha = 0.7f),
                        modifier = Modifier.matchParentSize()
                    ) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Button(
                                onClick = {
                                    // Resume scanning
                                    isScanning = true
                                    scannedContent = null
                                }
                            ) {
                                Text("Tap to Scan Again")
                            }
                        }
                    }
                }

                // Result panel – appears at the bottom when content was scanned
                if (scannedContent != null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            // Header with "Scanned" label and format badge
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Scanned",
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

                            // Scanned content preview
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

                            // Action buttons
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

                            // Extra "Scan Another" button for non-URL content
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

                            // Copied message
                            if (showCopiedMessage) {
                                Text(
                                    text = "Copied to clipboard!",
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                LaunchedEffect(Unit) {
                                    delay(2000)
                                    showCopiedMessage = false
                                }
                            }
                        }
                    }
                }

                // Instructional card (visible when scanning and no result yet)
                if (isScanning && scannedContent == null) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        ),
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                            .fillMaxWidth()
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

                // Scan history (top‑right or a small panel – here placed top‑right as a compact overlay)
                if (scanHistory.isNotEmpty()) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .width(200.dp)
                    ) {
                        Text(
                            text = "Recent",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            modifier = Modifier
                                .background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        scanHistory.take(3).forEach { item ->
                            HistoryItemCard(item = item, context = context)
                            Spacer(modifier = Modifier.height(4.dp))
                        }
                    }
                }

            } else {
                // No permission state
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
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

            // "Supported Formats" info card – anchored to bottom, always visible below result card
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp)
                ) {
                    Text(
                        text = "Supported Formats",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(4.dp))
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

        // Scanning line animation – only active while isScanning
        if (isScanning) {
            var linePosition by remember { mutableStateOf(0f) }

            LaunchedEffect(isScanning) {
                while (isScanning) {
                    delay(50)
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
    item: QRScanHistoryItem,
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
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.content.take(30) + if (item.content.length > 30) "..." else "",
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

data class QRScanHistoryItem(
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