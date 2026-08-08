package com.dannyk.toolbox.ui.screens.tools.files

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolScreenLayout
import androidx.compose.ui.graphics.Color
import android.content.ClipboardManager
import android.content.ClipData
import android.content.Context
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MimeTypeLookupScreen(navController: NavHostController) {
    val context = LocalContext.current
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedMimeType by remember { mutableStateOf<MimeTypeInfo?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Filter MIME types based on search and category
    val filteredMimeTypes = remember(searchQuery, selectedCategory) {
        var results = mimeTypeDatabase
        
        if (selectedCategory != "All") {
            results = results.filter { it.category == selectedCategory }
        }
        
        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase()
            results = results.filter { 
                it.mimeType.lowercase().contains(query) || 
                it.extensions.any { ext -> ext.contains(query) } ||
                it.description.lowercase().contains(query)
            }
        }
        
        results.sortedBy { it.mimeType }
    }

    ToolScreenLayout(
        title = "MIME Type Lookup",
        navController = navController
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Panel - MIME Type List
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search MIME type or extension...") },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .focusRequester(focusRequester),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() })
                    )
                    
                    // Category Filter Chips
                    LazyRow(
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mimeTypeCategories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.fillMaxWidth())
                    
                    // Results Count
                    Text(
                        text = "${filteredMimeTypes.size} MIME types found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                    
                    // MIME Type List
                    if (filteredMimeTypes.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No MIME types found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(filteredMimeTypes) { mime ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 2.dp)
                                        .clickable { 
                                            selectedMimeType = if (selectedMimeType == mime) null else mime
                                        },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedMimeType == mime) 
                                            MaterialTheme.colorScheme.primaryContainer 
                                        else 
                                            MaterialTheme.colorScheme.surface
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
                                                text = mime.mimeType,
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontFamily = FontFamily.Monospace,
                                                maxLines = 1
                                            )
                                            Spacer(modifier = Modifier.height(2.dp))
                                            Text(
                                                text = mime.extensions.joinToString(", "),
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                        
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = mime.category,
                                                style = MaterialTheme.typography.labelSmall,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Right Panel - Details
            if (selectedMimeType != null) {
                Divider(modifier = Modifier.fillMaxWidth(), 
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                )
                
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Header with MIME type
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = MaterialTheme.colorScheme.primaryContainer,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "MIME Type",
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedMimeType!!.mimeType,
                                style = MaterialTheme.typography.headlineSmall,
                                fontFamily = FontFamily.Monospace,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IconButton(
                                    onClick = {
                                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                                        val clip = android.content.ClipData.newPlainText("mime type", selectedMimeType!!.mimeType)
                                        clipboard.setPrimaryClip(clip)
                                        android.widget.Toast.makeText(context, "Copied!", android.widget.Toast.LENGTH_SHORT).show()
                                    }
                                ) {
                                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy")
                                }
                                
                                Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                    Text(
                                        text = selectedMimeType!!.category,
                                        style = MaterialTheme.typography.labelSmall,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Extensions
                    DetailCard(title = "File Extensions") {
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            selectedMimeType!!.extensions.forEach { ext ->
                                Surface(
                                    shape = RoundedCornerShape(4.dp),
                                    color = MaterialTheme.colorScheme.surfaceVariant
                                ) {
                                    Text(
                                        text = ext,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                    
                    // Description
                    DetailCard(title = "Description") {
                        Text(
                            text = selectedMimeType!!.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // Common Use Cases
                    if (selectedMimeType!!.useCases.isNotEmpty()) {
                        DetailCard(title = "Common Use Cases") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                selectedMimeType!!.useCases.forEach { useCase ->
                                    Text(
                                        text = "• $useCase",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                    
                    // Technical Details
                    if (selectedMimeType!!.technicalDetails.isNotEmpty()) {
                        DetailCard(title = "Technical Details") {
                            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                selectedMimeType!!.technicalDetails.forEach { detail ->
                                    Row {
                                        Text(
                                            text = "${detail.first}: ",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = detail.second,
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                // Placeholder when no selection
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Select a MIME type",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap on a MIME type from the list\nto view detailed information",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailCard(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(8.dp))
            content()
        }
    }
}

// Data class for MIME type information
data class MimeTypeInfo(
    val mimeType: String,
    val extensions: List<String>,
    val category: String,
    val description: String,
    val useCases: List<String> = emptyList(),
    val technicalDetails: List<Pair<String, String>> = emptyList()
)

// Categories for filtering
private val mimeTypeCategories = listOf(
    "All", "Text", "Image", "Audio", "Video", "Application", "Multipart", "Message"
)

// Comprehensive MIME type database
private val mimeTypeDatabase = listOf(
    // Text Types
    MimeTypeInfo("text/plain", listOf("txt", "text", "conf", "def", "list", "log", "in", "ini"), "Text",
        "Plain text file without any formatting."),
    MimeTypeInfo("text/html", listOf("html", "htm", "shtml"), "Text",
        "HyperText Markup Language for web pages.",
        listOf("Web pages", "Email HTML"),
        listOf("Standard" to "RFC 2854", "Charset" to "UTF-8 or ISO-8859-1")),
    MimeTypeInfo("text/css", listOf("css"), "Text",
        "Cascading Style Sheets for styling web documents.",
        listOf("Web styling", "User stylesheets")),
    MimeTypeInfo("text/javascript", listOf("js", "mjs"), "Text",
        "JavaScript programming language source code.",
        listOf("Web development", "Server-side with Node.js")),
    MimeTypeInfo("text/xml", listOf("xml", "xsl", "xsp", "xsl", "rdf", "wsdl", "xpdl"), "Text",
        "Extensible Markup Language for structured data.",
        listOf("Data interchange", "Configuration files", "Web services")),
    MimeTypeInfo("text/csv", listOf("csv"), "Text",
        "Comma-Separated Values format for tabular data.",
        listOf("Data export/import", "Spreadsheets")),
    MimeTypeInfo("text/markdown", listOf("md", "markdown", "mdown", "mkd"), "Text",
        "Markdown lightweight markup language.",
        listOf("Documentation", "Readme files", "Blogging")),
    MimeTypeInfo("text/calendar", listOf("ics", "ifb", "ical"), "Text",
        "iCalendar format for calendar events and scheduling.",
        listOf("Calendar invites", "Event sharing")),
    MimeTypeInfo("text/vcard", listOf("vcf", "vcard"), "Text",
        "vCard electronic business card format.",
        listOf("Contact information", "Address books")),
    MimeTypeInfo("text/rtf", listOf("rtf"), "Text",
        "Rich Text Format supporting basic document formatting.",
        listOf("Document exchange", "Word processing")),
    MimeTypeInfo("text/yaml", listOf("yaml", "yml"), "Text",
        "YAML Ain't Markup Language - human-readable data serialization.",
        listOf("Configuration files", "CI/CD pipelines", "Kubernetes configs")),
    MimeTypeInfo("text/toml", listOf("toml"), "Text",
        "Tom's Obvious Minimal Language configuration format.",
        listof("Rust/Cargo config", "Python pyproject.toml")),
    MimeTypeInfo("text/x-python", listOf("py", "pyw", "pyc", "pyo", "pyz"), "Text",
        "Python programming language source code.",
        listof("Scripts", "Applications", "Jupyter notebooks")),
    MimeTypeInfo("text/x-java-source", listOf("java"), "Text",
        "Java programming language source code.",
        listof("Android apps", "Enterprise applications", "Web backends")),
    MimeTypeInfo("text/x-shellscript", listOf("sh", "bash", "zsh", "csh", "ksh"), "Text",
        "Shell script for Unix/Linux command interpreters.",
        listof("System automation", "DevOps scripts", "Server configuration")),
    
    // Image Types
    MimeTypeInfo("image/jpeg", listOf("jpg", "jpeg", "jpe", "jif", "jfif", "jfi"), "Image",
        "Joint Photographic Experts Group image format with lossy compression.",
        listof("Photographs", "Web images", "Digital cameras"),
        listOf("Compression" to "Lossy", "Transparency" to "Not supported", "Max Colors" to "16.7 million")),
    MimeTypeInfo("image/png", listOf("png"), "Image",
        "Portable Network Graphics with lossless compression and transparency support.",
        listof("Graphics", "Logos", "Screenshots", "Images requiring transparency"),
        listOf("Compression" to "Lossless", "Transparency" to "Alpha channel supported", "Patent-free" to "Yes")),
    MimeTypeInfo("image/gif", listOf("gif"), "Image",
        "Graphics Interchange Format supporting animation and limited colors.",
        listof("Simple animations", "Memes", "Low-color graphics"),
        listOf("Animation" to "Supported", "Max Colors" to "256", "Compression" to "LZW lossless")),
    MimeTypeInfo("image/webp", listOf("webp"), "Image",
        "Modern image format by Google with superior compression.",
        listof("Web images", "Progressive loading", "Animated images"),
        listOf("Compression" to "Lossy/Lossless", "Browser Support" to "Modern browsers", "Creator" to "Google")),
    MimeTypeInfo("image/svg+xml", listOf("svg", "svgz"), "Image",
        "Scalable Vector Graphics - XML-based vector image format.",
        listof("Logos", "Icons", "Diagrams", "Responsive images"),
        listOf("Scalability" to "Infinite", "Format" to "XML-based vector", "Editor" to "Inkscape/Illustrator")),
    MimeTypeInfo("image/bmp", listOf("bmp", "dib"), "Image",
        "Bitmap Image Format - uncompressed raster graphics.",
        listof("Windows wallpapers", "Simple graphics", "Legacy compatibility"),
        listOf("Compression" to "None/RLE", "Platform" to "Windows native", "File Size" to "Large")),
    MimeTypeInfo("image/tiff", listOf("tif", "tiff"), "Image",
        "Tagged Image File Format for high-quality images.",
        listof("Printing", "Publishing", "Scanning", "Professional photography"),
        listOf("Compression" to "Lossy/Lossless/None", "Layers" to "Supported", "Color Depth" to "Up to 32-bit")),
    MimeTypeInfo("image/x-icon", listOf("ico"), "Image",
        "Windows icon format containing multiple sizes in one file.",
        listof("Favicons", "Application icons", "Windows shortcuts"),
        listOf("Sizes" to "Multiple in one file", "Format" to "BMP/PNG based", "Max Size" to "256x256")),
    MimeTypeInfo("image/heic", listOf("heic", "heif"), "Image",
        "High Efficiency Image Container - Apple's modern photo format.",
        listof("iPhone photos", "HEIF/HEIC images", "Space-efficient storage"),
        listOf("Compression" to "Better than JPEG", "Creator" to "Apple/MPEG", "Support" to "iOS/macOS native")),
    MimeTypeInfo("image/vnd.adobe.photoshop", listOf("psd"), "Image",
        "Adobe Photoshop native format with layers and effects.",
        listof("Photo editing", "Graphic design", "Layered compositions"),
        listOf("Layers" to "Supported", "Creator" to "Adobe", "Proprietary" to "Yes")),
    MimeTypeInfo("image/avif", listOf("avif"), "Image",
        "AV1 Image File Format - next-generation image codec.",
        listof("Next-gen web images", "Superior compression", "HDR support"),
        listOf("Codec" to "AV1", "Compression" to "40% better than JPEG", "Browser Support" to "Growing")),
    
    // Audio Types
    MimeTypeInfo("audio/mpeg", listOf("mp3", "mpga", "mp2", "mpa", "m2a"), "Audio",
        "MPEG Audio Layer III - most popular audio format.",
        listof("Music playback", "Podcasts", "Streaming audio"),
        listOf("Compression" to "Lossy", "Quality Levels" to "Variable bitrate", "Compatibility" to "Universal")),
    MimeTypeInfo("audio/wav", listOf("wav", "wave"), "Audio",
        "Waveform Audio File Format - uncompressed PCM audio.",
        listof("Audio editing", "Studio recording", "Sound design"),
        listOf("Compression" to "None (lossless)", "Quality" to "CD quality+", "File Size" to "Large")),
    MimeTypeInfo("audio/flac", listOf("flac"), "Audio",
        "Free Lossless Audio Codec - compressed without quality loss.",
        listof("Hi-res audio", "Archiving", "Audiophile music"),
        listOf("Compression" to "Lossless", "Quality" to "CD to 24-bit/192kHz", "Open Source" to "Yes")),
    MimeTypeInfo("audio/aac", listOf("aac", "adts", "loas", "ass"), "Audio",
        "Advanced Audio Coding - efficient modern audio codec.",
        listof("iTunes music", "YouTube streaming", "Mobile audio"),
        listOf("Compression" to "Lossy", "Efficiency" to "Better than MP3", "Apple Default" to "Yes")),
    MimeTypeInfo("audio/ogg", listOf("oga", "ogg", "opus", "spx"), "Audio",
        "Ogg container format for Vorbis/Opus/Speex audio.",
        listof("Open-source audio", "Streaming", "Gaming audio"),
        listOf("Container" to "Ogg", "Codecs" to "Vorbis/Opus/Speex", "Patent-free" to "Mostly")),
    MimeTypeInfo("audio/mp4", listOf("m4a", "mp4a", "f4a"), "Audio",
        "MPEG-4 Audio - commonly used AAC in MP4 container.",
        listof("Apple Music", "iTunes purchases", "Ringtones"),
        listOf("Container" to "MP4", "Default Codec" to "AAC", "Apple Standard" to "Yes")),
    MimeTypeInfo("audio/webm", listOf("weba"), "Audio",
        "WebM audio using Opus/Vorbis codecs.",
        listof("Web audio", "HTML5 video audio", "Streaming"),
        listOf("Container" to "WebM", "Codecs" to "Opus/Vorbis", "Royalty-free" to "Yes")),
    MimeTypeInfo("audio/midi", listOf("mid", "midi", "rmi", "kar"), "Audio",
        "Musical Instrument Digital Interface - musical notes, not audio.",
        listof("Music composition", "Ringtone creation", "DAW projects"),
        listOf("Content" to "Musical data", "Playback" to "Requires synthesizer", "File Size" to "Very small")),
    MimeTypeInfo("audio/x-ms-wma", listOf("wma"), "Audio",
        "Windows Media Audio - Microsoft's proprietary format.",
        listof("Windows Media Player", "Legacy streaming", "DRM content"),
        listOf("Creator" to "Microsoft", "DRM Support" to "Yes", "Compression" to "Lossy")),
    
    // Video Types
    MimeTypeInfo("video/mp4", listOf("mp4", "mp4v", "mpg4"), "Video",
        "MPEG-4 Part 14 - most widely supported video format.",
        listof("Streaming", "Mobile video", "Social media", "Downloads"),
        listOf("Container" to "MP4", "Codecs" to "H.264/H.265/AV1", "Compatibility" to "Excellent")),
    MimeTypeInfo("video/webm", listOf("webm"), "Video",
        "WebM open royalty-free video format for the web.",
        listof("HTML5 video", "Web streaming", "VP9/AV1 content"),
        listOf("Container" to "Matroska subset", "Codecs" to "VP8/VP9/AV1 + Opus", "Royalty-free" to "Yes")),
    MimeTypeInfo("video/quicktime", listOf("mov", "qt"), "Video",
        "QuickTime Movie format - Apple's standard video container.",
        listof("macOS/iOS videos", "Final Cut Pro", "Camera recordings"),
        listOf("Creator" to "Apple", "Container" to "QuickTime", "Native Platform" to "macOS/iOS")),
    MimeTypeInfo("video/x-msvideo", listOf("avi"), "Video",
        "Audio Video Interleave - classic Microsoft multimedia container.",
        listof("Legacy video", "DVD rips", "Camcorder footage"),
        listOf("Creator" to "Microsoft", "Age" to "1992", "Limitations" to "No metadata support")),
    MimeTypeInfo("video/x-matroska", listOf("mkv", "mk3d", "mks"), "Video",
        "Matroska Video - feature-rich open container format.",
        listof("HD movies", "Anime", "Multiple audio tracks", "Subtitles"),
        listOf("Features" to "Extensive", "Open Source" to "Yes", "Metadata" to "Full support")),
    MimeTypeInfo("video/ms-asf", listOf("asf", "asx"), "Video",
        "Advanced Systems Format - Microsoft's streaming container.",
        listof("Windows Media", "Streaming servers", "DRM content"),
        listOf("Creator" to "Microsoft", "DRM" to "PlayReady", "Streaming" to "Optimized")),
    MimeTypeInfo("video/x-flv", listOf("flv"), "Video",
        "Flash Video - formerly popular for web streaming.",
        listof("Legacy Flash content", "Old streams", "Screen recordings"),
        listOf("Platform" to "Flash Player (EOL)", "Status" to "Deprecated", "Alternative" to "MP4/WebM")),
    MimeTypeInfo("video/3gpp", listOf("3gp", "3g2"), "Video",
        "3GPP Multimedia - optimized for mobile phones.",
        listof("Mobile video", "MMS messages", "Legacy phone content"),
        listOf("Target" to "Mobile devices", "Compression" to "High", "Quality" to "Lower")),
    MimeTypeInfo("video/ogg", listOf("ogv"), "Video",
        "Ogg video using Theora codec.",
        listof("Open-source video", "Wikipedia content", "HTML5 fallback"),
        listOf("Codec" to "Theora", "Container" to "Ogg", "Status" to "Largely superseded by WebM")),
    MimeTypeInfo("video/mpeg", listOf("mpeg", "mpg", "mpe", "m1v", "m2v", "mp2", "mpa", "mpv2"), "Video",
        "MPEG video - older standard for digital video.",
        listof("DVD video", "Broadcast TV", "VCD/SVCD"),
        listOf("Standards" to "MPEG-1/MPEG-2", "Use Case" to "Physical media", "Quality" to "SD/HD")),
    
    // Application Types
    MimeTypeInfo("application/pdf", listOf("pdf"), "Application",
        "Portable Document Format - universal document format.",
        listof("Documents", "Forms", "E-books", "Print-ready files"),
        listOf("Standard" to "ISO 32000", "Creator" to "Adobe", "Features" to "Text, images, forms, signatures")),
    MimeTypeInfo("application/zip", listOf("zip"), "Application",
        "ZIP archive format for file compression.",
        listof("File distribution", "Software packages", "Office documents (.docx etc.)"),
        listOf("Compression" to "DEFLATE", "Standard" to "PKWARE", "Multi-file" to "Supported")),
    MimeTypeInfo("application/vnd.rar", listOf("rar"), "Application",
        "RAR archive format with high compression ratio.",
        listof("File archiving", "Split archives", "Recovery records"),
        listOf("Creator" to "RARLAB", "Compression" to "Excellent", "License" to "Proprietary")),
    MimeTypeInfo("application/x-7z-compressed", listOf("7z"), "Application",
        "7-Zip archive format with very high compression ratio.",
        listof("Maximum compression", "Open-source alternative", "Long-term archival"),
        listOf("Compression" to "LZMA/LZMA2", "Open Source" to "Yes", "Ratio" to "Often best")),
    MimeTypeInfo("application/gzip", listOf("gz", "gzip"), "Application",
        "GNU Zip compression format for single files.",
        listof("Unix compression", "Tar.gz archives", "HTTP compression"),
        listOf("Algorithm" to "DEFLATE", "Single File" to "Yes", "Use With" to "tar for directories")),
    MimeTypeInfo("application/x-tar", listOf("tar"), "Application",
        "Tape Archive format for combining multiple files.",
        listof("Unix archiving", "Source distributions", "Backups"),
        listOf("Compression" to "None (use gzip/bzip2)", "Unix Standard" to "Yes", "Metadata" to "Preserved")),
    MimeTypeInfo("application/json", listOf("json", "map"), "Application",
        "JavaScript Object Notation - lightweight data format.",
        listof("API responses", "Config files", "Data exchange", "NoSQL databases"),
        listOf("Syntax" to "JavaScript subset", "Human-readable" to "Yes", "Schema" to "JSON Schema")),
    MimeTypeInfo("application/xml", listOf("xml"), "Application",
        "Extensible Markup Language for structured data.",
        listof("SOAP services", "Configuration", "Office formats (DOCX, XLSX)"),
        listOf("Validation" to "DTD/XSD", "Namespaces" to "Supported", "Transformation" to "XSLT")),
    MimeTypeInfo("application/javascript", listOf("js", "mjs"), "Application",
        "JavaScript code served as application rather than script.",
        listof("Modules", "Worker scripts", "Service workers"),
        listOf("Execution" to "Sandboxed", "Runtime" to "Browser/Node.js", "Type" to "Dynamic")),
    MimeTypeInfo("application/octet-stream", listOf("bin", "exe", "dll", "deb", "dmg", "iso", "img"), "Application",
        "Generic binary data - default for unknown types.",
        listof("Executables", "Binary downloads", "Any binary file"),
        listOf("Fallback" to "Default binary type", "Security" to "May trigger download", "Usage" to "When specific type unknown")),
    MimeTypeInfo("application/vnd.openxmlformats-officedocument.wordprocessingml.document", listOf("docx"), "Application",
        "Microsoft Word Open XML document format.",
        listof("Word processing", "Documents", "Templates"),
        listOf("Based On" to "Office Open XML", "Contains" to "XML+media in ZIP", "Standard" to "ECMA-376")),
    MimeTypeInfo("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", listOf("xlsx"), "Application",
        "Microsoft Excel Open XML spreadsheet format.",
        listof("Spreadsheets", "Data analysis", "Charts"),
        listOf("Based On" to "Office Open XML", "Sheets" to "Unlimited", "Formulas" to "Extensive")),
    MimeTypeInfo("application/vnd.openxmlformats-officedocument.presentationml.presentation", listOf("pptx"), "Application",
        "Microsoft PowerPoint Open XML presentation format.",
        listof("Presentations", "Slideshows", "Lectures"),
        listOf("Based On" to "Office Open XML", "Media" to "Embedded", "Animations" to "Supported")),
    MimeTypeInfo("application/msword", listOf("doc", "dot"), "Application",
        "Legacy Microsoft Word binary format.",
        listof("Legacy documents", "Template files", ".doc compatibility"),
        listOf("Status" to "Legacy", "Successor" to "DOCX", "Format" to "OLE compound document")),
    MimeTypeInfo("application/vnd.ms-excel", listOf("xls", "xlt", "xla"), "Application",
        "Legacy Microsoft Excel binary spreadsheet format.",
        listof("Legacy spreadsheets", "Macros", "Templates"),
        listOf("Status" to "Legacy", "Successor" to "XLSX", "Features" to "Limited vs XLSX")),
    MimeTypeInfo("application/vnd.ms-powerpoint", listOf("ppt", "pps", "pot"), "Application",
        "Legacy Microsoft PowerPoint binary presentation format.",
        listof("Legacy presentations", "Slide shows", "Templates"),
        listOf("Status" to "Legacy", "Successor" to "PPTX", "Format" to "OLE compound document")),
    MimeTypeInfo("application/vnd.oasis.opendocument.text", listOf("odt", "odt"), "Application",
        "OpenDocument Text - open standard word processing format.",
        listof("LibreOffice Writer", "Apache OpenOffice", "Cross-platform docs"),
        listOf("Standard" to "ISO/IEC 26300", "Open Source" to "Yes", "Zip Based" to "Yes")),
    MimeTypeInfo("application/epub+zip", listOf("epub"), "Application",
        "Electronic Publication format for e-books.",
        listof("E-books", "Digital publishing", "Reading apps"),
        listOf("Based On" to "HTML/CSS in ZIP", "Reflowable" to "Yes", "Standard" to "IDPF")),
    MimeTypeInfo("application/x-apple-diskimage", listOf("dmg"), "Application",
        "Apple Disk Image format for macOS software distribution.",
        listof("macOS installers", "Disk images", "Software distribution"),
        listOf("Platform" to "macOS only", "Mounting" to "Automatic", "Compression" to "Optional")),
    MimeTypeInfo("application/vnd.android.package-archive", listOf("apk"), "Application",
        "Android Package Kit - installable app package for Android.",
        listof("Android applications", "App sideloading", "App backups"),
        listOf("Platform" to "Android", "Format" to "ZIP/JAR based", "Signing" to "Required")),
    MimeTypeInfo("application/x-sqlite3", listOf("sqlite", "sqlite3", "db"), "Application",
        "SQLite database stored as a single self-contained file.",
        listof("Embedded databases", "Mobile apps", "Desktop applications"),
        listOf("SQL Dialect" to "SQLite", "Server-less" to "Yes", "ACID Compliant" to "Yes")),
    MimeTypeInfo("application/x-sh", listOf("sh"), "Application",
        "Bourne-Again Shell script for Unix/Linux systems.",
        listof("System administration", "Automation", "DevOps scripts"),
        listOf("Interpreter" to "bash/sh", "Platform" to "Unix-like", "Permissions" to "Execute required")),
    MimeTypeInfo("application/x-perl", listOf("pl", "pm"), "Application",
        "Perl scripting language source code.",
        listof("System administration", "Text processing", "CGI scripts"),
        listOf("Interpreter" to "perl", "Paradigm" to "Multi-paradigm", "CPAN" to "Rich module ecosystem")),
    MimeTypeInfo("application/x-ruby", listOf("rb"), "Application",
        "Ruby programming language source code.",
        listof("Web development (Rails)", "Scripting", "Automation"),
        listOf("Interpreter" to "ruby", "Framework" to "Ruby on Rails", "Paradigm" to "Object-oriented")),
    MimeTypeInfo("application/java-archive", listOf("jar"), "Application",
        "Java ARchive - packaged Java classes and resources.",
        listof("Java libraries", "Executable JARs", "Android components"),
        listOf("Format" to "ZIP with manifest", "Contents" to "Class files + resources", "Executor" to "JVM")),
    MimeTypeInfo("application/vnd.ms-fontobject", listOf("eot"), "Application",
        "Embedded OpenType font format for Internet Explorer.",
        listof("Web fonts (IE)", "Embedded fonts"),
        listOf("Browser" to "IE primarily", "Format" to "Compact OpenType", "Status" to "Legacy")),
    MimeTypeInfo("application/font-woff", listOf("woff"), "Application",
        "Web Open Font Format for web typography.",
        listof("Web fonts", "Custom typography", "Performance optimization"),
        listOf("Compression" to "woff/zlib", "Browser Support" to "Universal", "WOFF2" to "Newer version exists")),
    MimeTypeInfo("application/font-woff2", listOf("woff2"), "Application",
        "Web Open Font Format 2 with better compression.",
        listof("Modern web fonts", "Fast-loading fonts", "Google Fonts"),
        listOf("Compression" to "Brotli", "Size" to "~30% smaller than WOFF", "Support" to "Modern browsers")),
    
    // Multipart Types
    MimeTypeInfo("multipart/form-data", emptyList(), "Multipart",
        "Multipart form data for HTTP file uploads.",
        listof("File uploads", "Form submissions with files", "API requests"),
        listOf("Protocol" to "HTTP", "Boundary" to "Required delimiter", "Use Case" to "POST requests")),
    MimeTypeInfo("multipart/mixed", emptyList(), "Multipart",
        "Mixed content multipart message.",
        listof("Email attachments", "Mixed content types", "API responses")),
    MimeTypeInfo("multipart/alternative", emptyList(), "Multipart",
        "Alternative representations of same content (e.g., plain + HTML).",
        listof("Email (plain + HTML)", "Content negotiation", "Fallback versions")),
    
    // Message Types
    MimeTypeInfo("message/rfc822", listOf("eml", "mail", "mht", "mhtml"), "Message",
        "Internet email message format per RFC 822/2822.",
        listof("Email messages", "Email archiving", "Email exports"),
        listOf("Standard" to "RFC 5322", "Headers" to "From/To/Subject/Date", "Body" to "MIME encoded")),
)

// Helper function for creating lists inline
private fun <T> listof(vararg items: T): List<T> = items.toList()
