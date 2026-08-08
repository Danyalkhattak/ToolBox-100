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
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.exifinterface.media.ExifInterface
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
import android.content.ClipboardManager
import androidx.compose.ui.graphics.Path

data class ExifMetadata(
    // Camera info
    val make: String? = null,
    val model: String? = null,
    
    // Date and time
    val dateTime: String? = null,
    val dateTimeOriginal: String? = null,
    val dateTimeDigitized: String? = null,
    
    // Dimensions
    val width: Int = 0,
    val height: Int = 0,
    val pixelXDimension: String? = null,
    val pixelYDimension: String? = null,
    
    // GPS data
    val latitude: Double? = null,
    val longitude: Double? = null,
    val altitude: String? = null,
    val gpsDateStamp: String? = null,
    val gpsTimeStamp: String? = null,
    
    // Camera settings
    val iso: String? = null,
    val aperture: String? = null,
    val focalLength: String? = null,
    val exposureTime: String? = null,
    val flash: String? = null,
    val whiteBalance: String? = null,
    
    // Orientation
    val orientation: String? = null,
    
    // Software
    val software: String? = null,
    
    // Other
    val imageDescription: String? = null,
    val artist: String? = null,
    val copyright: String? = null,
    val resolutionUnit: String? = null,
    val xResolution: String? = null,
    yResolution: String? = null,
    val colorSpace: String? = null,
    val compression: String? = null,
    
    // File info
    val fileSize: Long = 0L,
    val fileName: String? = null,
    val mimeType: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MetadataViewerScreen(navController: NavHostController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val clipboardManager = LocalClipboardManager.current
    
    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var metadata by remember { mutableStateOf<ExifMetadata?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            scope.launch {
                isLoading = true
                errorMessage = null
                
                try {
                    // Copy file to temp location for ExifInterface (needs File path)
                    val tempFile = copyUriToTempFile(context, uri)
                    
                    if (tempFile != null) {
                        // Load bitmap for preview
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bytes = inputStream?.readBytes() ?: byteArrayOf()
                        bitmap = withContext(Dispatchers.IO) {
                            BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                        }
                        
                        // Extract EXIF metadata
                        metadata = withContext(Dispatchers.IO) {
                            extractExifMetadata(tempFile, bytes.size.toLong(), 
                                context.contentResolver.getType(uri))
                        }
                        
                        // Clean up temp file
                        tempFile.delete()
                    } else {
                        errorMessage = "Could not process this file"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    errorMessage = "Error reading metadata: ${e.message}"
                } finally {
                    isLoading = false
                }
            }
        }
    }

    Scaffold(
        topBar = { ToolTopBar("EXIF Metadata Viewer", navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Pick Image Button
            OutlinedButton(
                onClick = { imagePicker.launch("image/*") },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Select Image to View Metadata")
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Reading metadata...", style = MaterialTheme.typography.bodyMedium,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            if (errorMessage != null) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.error)
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(errorMessage!!, color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            // Image Preview + Basic Info
            if (bitmap != null && metadata != null) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(16.dp),
                            verticalAlignment = Alignment.Start
                        ) {
                            // Thumbnail
                            Image(
                                bitmap = bitmap!!.asImageBitmap(),
                                contentDescription = "Selected Image",
                                modifier = Modifier
                                    .size(120.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )
                            
                            // Quick stats
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = "Image Information",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.SemiBold
                                )
                                
                                MetaDataRow("Dimensions", "${metadata!!.width} × ${metadata!!.height} px")
                                MetaDataRow("File Size", formatFileSize(metadata!!.fileSize))
                                MetaDataRow("Type", metadata!!.mimeType ?: "Unknown")
                                metadata!!.dateTime?.let { 
                                    MetaDataRow("Date Taken", it) 
                                }
                            }
                        }
                    }
                }
                
                // ==================== CAMERA INFORMATION ====================
                SectionHeaderWithCopy(
                    title = "Camera Information",
                    metadata = metadata!!,
                    clipboardManager = clipboardManager,
                    fields = listOf(
                        "Make" to metadata!!.make,
                        "Model" to metadata!!.model,
                        "Software" to metadata!!.software
                    )
                )
                
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetaDataItem("Camera Make", metadata!!.make)
                        MetaDataItem("Camera Model", metadata!!.model)
                        MetaDataItem("Software", metadata!!.software)
                    }
                }

                // ==================== DATE & TIME ====================
                SectionHeaderWithCopy(
                    title = "Date & Time",
                    metadata = metadata!!,
                    clipboardManager = clipboardManager,
                    fields = listOf(
                        "DateTime" to metadata!!.dateTime,
                        "Original" to metadata!!.dateTimeOriginal,
                        "Digitized" to metadata!!.dateTimeDigitized
                    )
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetaDataItem("Date Time", metadata!!.dateTime)
                        MetaDataItem("Date Original", metadata!!.dateTimeOriginal)
                        MetaDataItem("Date Digitized", metadata!!.dateTimeDigitized)
                    }
                }

                // ==================== CAMERA SETTINGS ====================
                SectionHeaderWithCopy(
                    title = "Camera Settings",
                    metadata = metadata!!,
                    clipboardManager = clipboardManager,
                    fields = listOf(
                        "ISO" to metadata!!.iso,
                        "Aperture" to metadata!!.aperture,
                        "Focal Length" to metadata!!.focalLength,
                        "Exposure Time" to metadata!!.exposureTime,
                        "Flash" to metadata!!.flash,
                        "White Balance" to metadata!!.whiteBalance
                    )
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetaDataItem("ISO Speed Rating", metadata!!.iso)
                        MetaDataItem("F-Number / Aperture", metadata!!.aperture)
                        MetaDataItem("Focal Length", metadata!!.focalLength)
                        MetaDataItem("Exposure Time", metadata!!.exposureTime)
                        MetaDataItem("Flash", metadata!!.flash)
                        MetaDataItem("White Balance", metadata!!.whiteBalance)
                    }
                }

                // ==================== IMAGE PROPERTIES ====================
                SectionHeaderWithCopy(
                    title = "Image Properties",
                    metadata = metadata!!,
                    clipboardManager = clipboardManager,
                    fields = listOf(
                        "Width" to metadata!!.width.toString(),
                        "Height" to metadata!!.height.toString(),
                        "Orientation" to metadata!!.orientation,
                        "Color Space" to metadata!!.colorSpace,
                        "Compression" to metadata!!.compression,
                        "Resolution Unit" to metadata!!.resolutionUnit
                    )
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetaDataItem("Pixel X Dimension", metadata!!.pixelXDimension ?: "${metadata!!.width}")
                        MetaDataItem("Pixel Y Dimension", metadata!!.pixelYDimension ?: "${metadata!!.height}")
                        MetaDataItem("Orientation", metadata!!.orientation)
                        MetaDataItem("X Resolution", metadata!!.xResolution)
                        MetaDataItem("Y Resolution", metadata!!.yResolution)
                        MetaDataItem("Resolution Unit", metadata!!.resolutionUnit)
                        MetaDataItem("Color Space", metadata!!.colorSpace)
                        MetaDataItem("Compression", metadata!!.compression)
                    }
                }

                // ==================== GPS LOCATION ====================
                if (metadata!!.latitude != null || metadata!!.longitude != null) {
                    SectionHeaderWithCopy(
                        title = "GPS Location",
                        metadata = metadata!!,
                        clipboardManager = clipboardManager,
                        fields = listOf(
                            "Latitude" to metadata!!.latitude?.toString(),
                            "Longitude" to metadata!!.longitude?.toString(),
                            "Altitude" to metadata!!.altitude
                        )
                    )

                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MetaDataItem("Latitude", metadata!!.latitude?.let { 
                                String.format("%.6f°", it) 
                            })
                            MetaDataItem("Longitude", metadata!!.longitude?.let { 
                                String.format("%.6f°", it) 
                            })
                            MetaDataItem("Altitude", metadata!!.altitude)
                            MetaDataItem("GPS Date Stamp", metadata!!.gpsDateStamp)
                            MetaDataItem("GPS Time Stamp", metadata!!.gpsTimeStamp)
                            
                            // Google Maps link
                            if (metadata!!.latitude != null && metadata!!.longitude != null) {
                                val mapsUrl = "https://maps.google.com/?q=${metadata!!.latitude},${metadata!!.longitude}"
                                
                                OutlinedButton(
                                    onClick = {
                                        try {
                                            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, 
                                                android.net.Uri.parse(mapsUrl))
                                            context.startActivity(intent)
                                        } catch (_: Exception) {}
                                    },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("Open in Maps")
                                }
                            }
                        }
                    }
                }

                // ==================== OTHER INFO ====================
                SectionHeaderWithCopy(
                    title = "Other Information",
                    metadata = metadata!!,
                    clipboardManager = clipboardManager,
                    fields = listOf(
                        "Description" to metadata!!.imageDescription,
                        "Artist" to metadata!!.artist,
                        "Copyright" to metadata!!.copyright
                    )
                )

                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        MetaDataItem("Image Description", metadata!!.imageDescription)
                        MetaDataItem("Artist / Author", metadata!!.artist)
                        MetaDataItem("Copyright", metadata!!.copyright)
                    }
                }

                // Copy All Button
                Button(
                    onClick = {
                        val meta = metadata!!
                        val allMetadata = buildString {
                            appendLine("=== EXIF METADATA ===")
                            appendLine()
                            appendLine("--- Camera ---")
                            appendLine("Make: ${meta.make ?: "N/A"}")
                            appendLine("Model: ${meta.model ?: "N/A"}")
                            appendLine("Software: ${meta.software ?: "N/A"}")
                            appendLine()
                            appendLine("--- Date & Time ---")
                            appendLine("DateTime: ${meta.dateTime ?: "N/A"}")
                            appendLine("Original: ${meta.dateTimeOriginal ?: "N/A"}")
                            appendLine("Digitized: ${meta.dateTimeDigitized ?: "N/A"}")
                            appendLine()
                            appendLine("--- Camera Settings ---")
                            appendLine("ISO: ${meta.iso ?: "N/A"}")
                            appendLine("Aperture: ${meta.aperture ?: "N/A"}")
                            appendLine("Focal Length: ${meta.focalLength ?: "N/A"}")
                            appendLine("Exposure Time: ${meta.exposureTime ?: "N/A"}")
                            appendLine("Flash: ${meta.flash ?: "N/A"}")
                            appendLine()
                            appendLine("--- Image Properties ---")
                            appendLine("Dimensions: ${meta.width} × ${meta.height}")
                            appendLine("Orientation: ${meta.orientation ?: "N/A"}")
                            appendLine("Color Space: ${meta.colorSpace ?: "N/A"}")
                            appendLine()
                            if (meta.latitude != null) {
                                appendLine("--- GPS Location ---")
                                appendLine("Latitude: ${meta.latitude}")
                                appendLine("Longitude: ${meta.longitude}")
                                appendLine("Altitude: ${meta.altitude ?: "N/A"}")
                                appendLine()
                            }
                            appendLine("--- Other ---")
                            appendLine("Description: ${meta.imageDescription ?: "N/A"}")
                            appendLine("Artist: ${meta.artist ?: "N/A"}")
                            appendLine("Copyright: ${meta.copyright ?: "N/A"}")
                        }
                        clipboardManager.setText(AnnotatedString(allMetadata))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Copy All Metadata")
                }
            } else if (!isLoading) {
                // Empty state
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(
                        modifier = Modifier.padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Info, contentDescription = null, 
                             modifier = Modifier.size(64.dp), 
                             tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Select an image to view its EXIF metadata",
                             style = MaterialTheme.typography.bodyLarge,
                             color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Supports JPEG, PNG, WebP, and other common formats",
                             style = MaterialTheme.typography.bodySmall,
                             color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeaderWithCopy(
    title: String,
    metadata: ExifMetadata,
    clipboardManager: androidx.compose.ui.platform.ClipboardManager,
    fields: List<Pair<String, String?>>
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        
        IconButton(onClick = {
            val text = fields.joinToString("\n") { (label, value) ->
                "$label: ${value ?: "N/A"}"
            }
            clipboardManager.setText(AnnotatedString("$title\n${"-".repeat(title.length)}\n$text"))
        }) {
            Icon(Icons.Default.ContentCopy, contentDescription = "Copy section", 
                 modifier = Modifier.size(18.dp))
        }
    }
}

@Composable
private fun MetaDataRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, style = MaterialTheme.typography.bodySmall, 
             color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun MetaDataItem(label: String, value: String?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        Text(
            label, 
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(140.dp)
        )
        Text(
            value ?: "Not available",
            style = MaterialTheme.typography.bodyMedium,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}

private suspend fun copyUriToTempFile(context: Context, uri: Uri): File? = 
    withContext(Dispatchers.IO) {
        return@withContext try {
            val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
            
            val tempDir = context.cacheDir
            val tempFile = File(tempDir, "temp_image_${System.currentTimeMillis()}")
            
            val outputStream = FileOutputStream(tempFile)
            inputStream.copyTo(outputStream)
            
            outputStream.flush()
            outputStream.close()
            inputStream.close()
            
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

private fun extractExifMetadata(file: File, fileSize: Long, mimeType: String?): ExifMetadata {
    return try {
        val exif = ExifInterface(file.absolutePath)
        
        // Get dimensions from bitmap or EXIF
        val width = exif.getAttributeInt(ExifInterface.TAG_IMAGE_WIDTH, 0).takeIf { it > 0 } ?: 0
        val height = exif.getAttributeInt(ExifInterface.TAG_IMAGE_LENGTH, 0).takeIf { it > 0 } ?: 0
        
        // Parse GPS coordinates
        val latLong = FloatArray(2)
        val hasGps = exif.getLatLong(latLong)
        
        ExifMetadata(
            make = exif.getAttribute(ExifInterface.TAG_MAKE),
            model = exif.getAttribute(ExifInterface.TAG_MODEL),
            dateTime = exif.getAttribute(ExifInterface.TAG_DATETIME),
            dateTimeOriginal = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL),
            dateTimeDigitized = exif.getAttribute(ExifInterface.TAG_DATETIME_DIGITIZED),
            width = width,
            height = height,
            pixelXDimension = exif.getAttribute(ExifInterface.TAG_PIXEL_X_DIMENSION),
            pixelYDimension = exif.getAttribute(ExifInterface.TAG_PIXEL_Y_DIMENSION),
            latitude = if (hasGps) latLong[0].toDouble() else null,
            longitude = if (hasGps) latLong[1].toDouble() else null,
            altitude = exif.getAttribute(ExifInterface.TAG_ALTITUDE),
            gpsDateStamp = exif.getAttribute(ExifInterface.TAG_GPS_DATE_STAMP),
            gpsTimeStamp = exif.getAttribute(ExifInterface.TAG_GPS_TIMESTAMP),
            iso = exif.getAttribute(ExifInterface.TAG_ISO_SPEED_RATINGS),
            aperture = exif.getAttribute(ExifInterface.TAG_F_NUMBER)?.let { "f/$it" },
            focalLength = exif.getAttribute(ExifInterface.TAG_FOCAL_LENGTH)?.let { "${it}mm" },
            exposureTime = parseExposureTime(exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME)),
            flash = parseFlashValue(exif.getAttribute(ExifInterface.TAG_FLASH)),
            whiteBalance = when (exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE)) {
                "0" -> "Auto"
                "1" -> "Manual"
                else -> exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE)
            },
            orientation = parseOrientation(exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1)),
            software = exif.getAttribute(ExifInterface.TAG_SOFTWARE),
            imageDescription = exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION),
            artist = exif.getAttribute(ExifInterface.TAG_ARTIST),
            copyright = exif.getAttribute(ExifInterface.TAG_COPYRIGHT),
            resolutionUnit = parseResolutionUnit(exif.getAttributeInt(ExifInterface.TAG_RESOLUTION_UNIT, 2)),
            xResolution = exif.getAttribute(ExifInterface.TAG_X_RESOLUTION),
            yResolution = exif.getAttribute(ExifInterface.TAG_Y_RESOLUTION),
            colorSpace = parseColorSpace(exif.getAttributeInt(ExifInterface.TAG_COLOR_SPACE, 1)),
            compression = exif.getAttribute(ExifInterface.TAG_COMPRESSION),
            fileSize = fileSize,
            fileName = file.name,
            mimeType = mimeType
        )
    } catch (e: Exception) {
        e.printStackTrace()
        ExifMetadata(fileSize = fileSize, fileName = file.name, mimeType = mimeType)
    }
}

private fun parseExposureTime(value: String?): String? {
    if (value == null) return null
    
    return try {
        val rational = value.split("/").map { it.trim().toFloatOrNull() ?: 0f }
        if (rational.size == 2 && rational[1] > 0) {
            val time = rational[0] / rational[1]
            if (time < 1) {
                val denom = (1 / time).roundToInt()
                "1/${denom}s"
            } else {
                "${time}s"
            }
        } else {
            value
        }
    } catch (_: Exception) {
        value
    }
}

private fun parseFlashValue(value: String?): String? {
    if (value == null) return null
    
    return try {
        val flashInt = value.toIntOrNull() ?: return value
        val didFire = (flashInt and 0x01) != 0
        val mode = when ((flashInt shr 3) and 0x03) {
            1 -> "Compulsory"
            2 -> "Suppressed"
            3 -> "Auto"
            else -> "Unknown"
        }
        val returnLight = (flashInt and 0x04) != 0
        val modeDetected = (flashInt and 0x08) != 0
        
        buildString {
            if (didFire) append("Fired") else append("Did not fire")
            append(" ($mode)")
            if (returnLight) append(", Return light detected")
            if (modeDetected) append(", Mode detected")
        }
    } catch (_: Exception) {
        value
    }
}

private fun parseOrientation(value: Int): String {
    return when (value) {
        1 -> "Normal (0°)"
        2 -> "Flip Horizontal"
        3 -> "Rotate 180°"
        4 -> "Flip Vertical"
        5 -> "Transpose"
        6 -> "Rotate 90° CW"
        7 -> "Transverse"
        8 -> "Rotate 90° CCW"
        else -> "Unknown ($value)"
    }
}

private fun parseResolutionUnit(value: Int): String {
    return when (value) {
        2 -> "inches"
        3 -> "centimeters"
        else -> "Unknown ($value)"
    }
}

private fun parseColorSpace(value: Int): String {
    return when (value) {
        1 -> "sRGB"
        0xFFFF -> "Uncalibrated"
        else -> "Unknown ($value)"
    }
}

private fun formatFileSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    if (bytes < 1024 * 1024) return "${bytes / 1024} KB"
    return String.format("%.2f MB", bytes / (1024.0 * 1024.0))
}
