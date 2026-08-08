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
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ui.components.ToolScreenLayout
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.Divider
import androidx.compose.material3.HorizontalDivider
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.ui.draw.clip

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FileExtensionInfoScreen(navController: NavHostController) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var selectedExtension by remember { mutableStateOf<FileExtensionInfo?>(null) }
    
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    
    // Filter extensions based on search and category
    val filteredExtensions = remember(searchQuery, selectedCategory) {
        var results = fileExtensionsDatabase
        
        if (selectedCategory != "All") {
            results = results.filter { it.category == selectedCategory }
        }
        
        if (searchQuery.isNotEmpty()) {
            val query = searchQuery.lowercase()
            results = results.filter { 
                it.extension.contains(query) || 
                it.fullName.lowercase().contains(query) ||
                it.description.lowercase().contains(query)
            }
        }
        
        results.sortedBy { it.extension }
    }

    ToolScreenLayout(
        title = "File Extension Info",
        navController = navController
    ) {
        Row(modifier = Modifier.fillMaxSize()) {
            // Left Panel - List of Extensions
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
            ) {
                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        placeholder = { Text("Search extension (.pdf, .jpg...)") },
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
                        val categories = listOf("All", "Image", "Video", "Audio", "Document", "Archive", "Code", "Data", "System", "Other")
                        
                        items(categories) { category ->
                            FilterChip(
                                selected = selectedCategory == category,
                                onClick = { selectedCategory = category },
                                label = { Text(category) }
                            )
                        }
                    }
                    
                    Divider(modifier = Modifier.fillMaxWidth())
                    
                    // Extension List
                    Text(
                        text = "${filteredExtensions.size} extensions found",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
                    )
                    
                    if (filteredExtensions.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No extensions found",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(filteredExtensions) { ext ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(horizontal = 12.dp, vertical = 2.dp)
                                        .clickable { selectedExtension = ext },
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (selectedExtension == ext) 
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
                                        Surface(
                                            shape = RoundedCornerShape(4.dp),
                                            color = MaterialTheme.colorScheme.secondaryContainer
                                        ) {
                                            Text(
                                                text = ext.extension,
                                                style = MaterialTheme.typography.labelMedium,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        
                                        Spacer(modifier = Modifier.width(12.dp))
                                        
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = ext.fullName,
                                                style = MaterialTheme.typography.bodyMedium,
                                                maxLines = 1
                                            )
                                            Text(
                                                text = ext.mimeType,
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                maxLines = 1
                                            )
                                        }
                                        
                                        Text(
                                            text = ext.category,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
            
            // Right Panel - Details
            if (selectedExtension != null) {
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
                    // Header
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
                                text = selectedExtension!!.extension,
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = selectedExtension!!.fullName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Surface(shape = RoundedCornerShape(4.dp), color = MaterialTheme.colorScheme.secondaryContainer) {
                                Text(
                                    text = selectedExtension!!.category,
                                    style = MaterialTheme.typography.labelSmall,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    }
                    
                    // MIME Type
                    DetailCard(title = "MIME Type") {
                        Text(
                            text = selectedExtension!!.mimeType,
                            style = MaterialTheme.typography.bodyMedium,
                            fontFamily = androidx.compose.ui.text.font.FontFamily.Monospace
                        )
                    }
                    
                    // Description
                    DetailCard(title = "Description") {
                        Text(
                            text = selectedExtension!!.description,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    
                    // Common Applications
                    DetailCard(title = "Common Applications") {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            selectedExtension!!.commonApps.forEach { app ->
                                Text(
                                    text = "• $app",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                    
                    // Additional Info
                    if (selectedExtension!!.additionalInfo.isNotEmpty()) {
                        DetailCard(title = "Additional Information") {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                selectedExtension!!.additionalInfo.forEach { info ->
                                    Text(
                                        text = "• $info",
                                        style = MaterialTheme.typography.bodySmall
                                    )
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
                            text = "Select an extension",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Tap on an extension from the list\nto view detailed information",
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

// Data class for file extension information
data class FileExtensionInfo(
    val extension: String,
    val fullName: String,
    val mimeType: String,
    val category: String,
    val description: String,
    val commonApps: List<String>,
    val additionalInfo: List<String> = emptyList()
)

// Comprehensive database of ~200+ file extensions
private val fileExtensionsDatabase = listOf(
    // Image Formats
    FileExtensionInfo(".jpg", "JPEG Image", "image/jpeg", "Image",
        "Joint Photographic Experts Group image format, widely used for photographs and images with continuous-tone colors.",
        listOf("Photoshop", "GIMP", "Paint.NET", "Windows Photos", "Preview")),
    FileExtensionInfo(".jpeg", "JPEG Image", "image/jpeg", "Image",
        "Same as .jpg, alternative extension for JPEG format.",
        listOf("Photoshop", "GIMP", "Paint.NET", "Windows Photos")),
    FileExtensionInfo(".png", "Portable Network Graphics", "image/png", "Image",
        "Lossless image format supporting transparency, ideal for graphics, logos, and screenshots.",
        listOf("Photoshop", "GIMP", "Paint.NET", "Preview", "XnView")),
    FileExtensionInfo(".gif", "Graphics Interchange Format", "image/gif", "Image",
        "Supports animation and limited colors (256). Popular for simple animations and memes.",
        listOf("Browser", "GIMP", "Photoshop", "XnView")),
    FileExtensionInfo(".bmp", "Bitmap Image", "image/bmp", "Image",
        "Uncompressed raster image format. Large file sizes but widely compatible.",
        listOf("MS Paint", "Paint.NET", "GIMP", "Windows Photos")),
    FileExtensionInfo(".webp", "WebP Image", "image/webp", "Image",
        "Modern image format by Google, provides superior lossy and lossless compression.",
        listOf("Chrome", "GIMP", "Paint.NET", "XnConvert")),
    FileExtensionInfo(".svg", "Scalable Vector Graphics", "image/svg+xml", "Image",
        "XML-based vector image format that scales without quality loss.",
        listOf("Inkscape", "Illustrator", "Browser", "CorelDRAW")),
    FileExtensionInfo(".tiff", "Tagged Image File Format", "image/tiff", "Image",
        "High-quality image format used in publishing and professional photography.",
        listOf("Photoshop", "GIMP", "IrfanView", "XnView")),
    FileExtensionInfo(".ico", "Icon File", "image/x-icon", "Image",
        "Windows icon format containing multiple sizes in one file.",
        listOf("IcoFX", "GIMP", "Photoshop with plugin", "ImageMagick")),
    FileExtensionInfo(".heic", "High Efficiency Image Container", "image/heic", "Image",
        "Apple's modern image format with better compression than JPEG.",
        listOf("Apple Photos", "Adobe Bridge", "XnConvert", "HEIC converters")),
    FileExtensionInfo(".raw", "Raw Image Data", "image/raw", "Image",
        "Unprocessed data from digital camera sensors.",
        listOf("Lightroom", "Capture One", "Darktable", "Camera Raw")),
    FileExtensionInfo(".psd", "Photoshop Document", "image/vnd.adobe.photoshop", "Image",
        "Adobe Photoshop native format supporting layers and effects.",
        listOf("Photoshop", "GIMP", "Photopea", "Affinity Photo")),
    
    // Video Formats
    FileExtensionInfo(".mp4", "MPEG-4 Video", "video/mp4", "Video",
        "Most widely supported video format for streaming and storage.",
        listof("VLC", "MPV", "Windows Media Player", "QuickTime", "FFmpeg")),
    FileExtensionInfo(".avi", "Audio Video Interleave", "video/x-msvideo", "Video",
        "Classic multimedia container format by Microsoft.",
        listof("VLC", "MPV", "Windows Media Player", "Media Player Classic")),
    FileExtensionInfo(".mkv", "Matroska Video", "video/x-matroska", "Video",
        "Open container format supporting multiple audio/video/subtitle tracks.",
        listof("VLC", "MPV", "PotPlayer", "MPC-HC")),
    FileExtensionInfo(".mov", "QuickTime Movie", "video/quicktime", "Video",
        "Apple's video container format, commonly used in macOS/iOS.",
        listof("QuickTime", "VLC", "Final Cut Pro", "iMovie")),
    FileExtensionInfo(".wmv", "Windows Media Video", "video/x-ms-wmv", "Video",
        "Microsoft's proprietary video compression format.",
        listof("Windows Media Player", "VLC", "Media Player Classic")),
    FileExtensionInfo(".flv", "Flash Video", "video/x-flv", "Video",
        "Format formerly popular for web streaming via Flash Player.",
        listof("VLC", "Flash Player", "Rtmpdump")),
    FileExtensionInfo(".webm", "WebM Video", "video/webm", "Video",
        "Open royalty-free format designed for web use, based on Matroska.",
        listof("Browser", "VLC", "MPV", "FFmpeg")),
    FileExtensionInfo(".3gp", "3GPP Multimedia", "video/3gpp", "Video",
        "Compressed format optimized for mobile phones.",
        listof("VLC", "MPV", "QuickTime", "RealPlayer")),
    FileExtensionInfo(".ts", "MPEG Transport Stream", "video/mp2t", "Video",
        "Container format for MPEG video transmission and storage.",
        listof("VLC", "MPV", "FFmpeg", "TS Doctor")),
    
    // Audio Formats
    FileExtensionInfo(".mp3", "MPEG Audio Layer III", "audio/mpeg", "Audio",
        "Most popular audio format for music distribution and playback.",
        listof("VLC", "Foobar2000", "Winamp", "Music app", "Audacity")),
    FileExtensionInfo(".wav", "Waveform Audio", "audio/wav", "Audio",
        "Uncompressed audio format developed by Microsoft and IBM.",
        listof("VLC", "Audacity", "Windows Media Player", "QuickTime")),
    FileExtensionInfo(".flac", "Free Lossless Audio Codec", "audio/flac", "Audio",
        "Lossless audio compression preserving original quality.",
        listof("Foobar2000", "VLC", "MusicBee", "Audacity")),
    FileExtensionInfo(".aac", "Advanced Audio Coding", "audio/aac", "Audio",
        "Efficient audio codec used by Apple, YouTube, and others.",
        listof("iTunes", "VLC", "Foobar2000", "Winamp")),
    FileExtensionInfo(".ogg", "Ogg Vorbis", "audio/ogg", "Audio",
        "Open-source patent-free audio compression format.",
        listof("VLC", "Foobar2000", "Audacity", "MusicBee")),
    FileExtensionInfo(".wma", "Windows Media Audio", "audio/x-ms-wma", "Audio",
        "Microsoft's proprietary audio format.",
        listof("Windows Media Player", "VLC", "Foobar2000")),
    FileExtensionInfo(".m4a", "MPEG-4 Audio", "audio/mp4", "Audio",
        "Audio-only MP4 container, commonly used by Apple.",
        listof("iTunes", "VLC", "Foobar2000", "Music app")),
    FileExtensionInfo(".opus", "Opus Audio", "audio/opus", "Audio",
        "Highly versatile open codec for speech and music.",
        listof("VLC", "Foobar2000", "Opus Tools", "Audacity")),
    FileExtensionInfo(".mid", "MIDI File", "audio/midi", "Audio",
        "Musical Instrument Digital Interface - stores musical notes, not audio.",
        listof("DAWs", "VLC", "MIDI Players", "Synthesizers")),
    
    // Document Formats
    FileExtensionInfo(".pdf", "Portable Document Format", "application/pdf", "Document",
        "Universal document format preserving layout across platforms.",
        listof("Adobe Reader", "Chrome", "Foxit Reader", "Preview")),
    FileExtensionInfo(".doc", "Microsoft Word Document", "application/msword", "Document",
        "Legacy Microsoft Word binary format.",
        listof("Word", "LibreOffice Writer", "Google Docs", "WPS Office")),
    FileExtensionInfo(".docx", "Office Open XML Document", "application/vnd.openxmlformats-officedocument.wordprocessingml.document", "Document",
        "Modern Microsoft Word format based on XML and ZIP.",
        listof("Word", "LibreOffice Writer", "Google Docs", "Pages")),
    FileExtensionInfo(".xls", "Microsoft Excel Spreadsheet", "application/vnd.ms-excel", "Document",
        "Legacy Excel binary spreadsheet format.",
        listof("Excel", "LibreOffice Calc", "Google Sheets", "WPS Office")),
    FileExtensionInfo(".xlsx", "Office Open XML Spreadsheet", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", "Document",
        "Modern Excel spreadsheet format.",
        listof("Excel", "LibreOffice Calc", "Google Sheets", "Numbers")),
    FileExtensionInfo(".ppt", "Microsoft PowerPoint Presentation", "application/vnd.ms-powerpoint", "Document",
        "Legacy PowerPoint presentation format.",
        listof("PowerPoint", "LibreOffice Impress", "Keynote", "Google Slides")),
    FileExtensionInfo(".pptx", "Office Open XML Presentation", "application/vnd.openxmlformats-officedocument.presentationml.presentation", "Document",
        "Modern PowerPoint presentation format.",
        listof("PowerPoint", "LibreOffice Impress", "Keynote", "Google Slides")),
    FileExtensionInfo(".odt", "OpenDocument Text", "application/vnd.oasis.opendocument.text", "Document",
        "Open standard word processing format used by LibreOffice/OpenOffice.",
        listof("LibreOffice Writer", "Apache OpenOffice", "Calligra Words")),
    FileExtensionInfo(".rtf", "Rich Text Format", "application/rtf", "Document",
        "Cross-platform document format supporting basic formatting.",
        listof("WordPad", "Word", "LibreOffice Writer", "TextEdit")),
    FileExtensionInfo(".txt", "Plain Text File", "text/plain", "Document",
        "Simple unformatted text file.",
        listof("Notepad", "TextEdit", "VS Code", "Any text editor")),
    FileExtensionInfo(".epub", "Electronic Publication", "application/epub+zip", "Document",
        "E-book format widely used for digital books.",
        listof("Apple Books", "Adobe Digital Editions", "Calibre", "FBReader")),
    FileExtensionInfo(".mobi", "Mobipocket E-book", "application/x-mobipocket-ebook", "Document",
        "E-book format primarily used by Amazon Kindle.",
        listof("Kindle", "Calibre", "Mobipocket Reader")),
    
    // Archive Formats
    FileExtensionInfo(".zip", "ZIP Archive", "application/zip", "Archive",
        "Popular compressed archive format supporting lossless compression.",
        listof("7-Zip", "WinRAR", "WinZip", "Explorer built-in")),
    FileExtensionInfo(".rar", "RAR Archive", "application/vnd.rar", "Archive",
        "Proprietary archive format known for high compression ratios.",
        listof("WinRAR", "7-Zip", "UnRAR", "The Unarchiver")),
    FileExtensionInfo(".7z", "7-Zip Archive", "application/x-7z-compressed", "Archive",
        "Open archive format with high compression ratio.",
        listof("7-Zip", "PeaZip", "Keka", "The Unarchiver")),
    FileExtensionInfo(".tar", "Tape Archive", "application/x-tar", "Archive",
        "Unix archive format often combined with gzip/bzip2.",
        listof("7-Zip", "tar command", "PeaZip", "The Unarchiver")),
    FileExtensionInfo(".gz", "Gnu Zipped Archive", "application/gzip", "Archive",
        "Single-file compression using DEFLATE algorithm.",
        listof("7-Zip", "gzip", "Gunzip", "The Unarchiver")),
    FileExtensionInfo(".bz2", "BZip2 Compressed", "application/x-bzip2", "Archive",
        "Compression using Burrows-Wheeler algorithm.",
        listof("7-Zip", "bzip2", "PeaZip")),
    FileExtensionInfo(".iso", "ISO Disc Image", "application/octet-stream", "Archive",
        "Disc image format for optical media (CD/DVD/Blu-ray).",
        listof("Daemon Tools", "PowerISO", "UltraISO", "Virtual CloneDrive")),
    FileExtensionInfo(".dmg", "Apple Disk Image", "application/apple-diskimage", "Archive",
        "macOS disk image format for software distribution.",
        listof("Finder", "Disk Utility", "TransMac", "DMG Extractor")),
    
    // Code/Programming Files
    FileExtensionInfo(".java", "Java Source Code", "text/x-java-source", "Code",
        "Source code for Java programming language.",
        listof("IntelliJ IDEA", "Eclipse", "VS Code", "NetBeans")),
    FileExtensionInfo(".kt", "Kotlin Source Code", "text/x-kotlin", "Code",
        "Source code for Kotlin programming language.",
        listof("IntelliJ IDEA", "Android Studio", "VS Code")),
    FileExtensionInfo(".py", "Python Source Code", "text/x-python", "Code",
        "Source code for Python programming language.",
        listof("PyCharm", "VS Code", "Sublime Text", "IDLE")),
    FileExtensionInfo(".js", "JavaScript Source", "application/javascript", "Code",
        "Source code for JavaScript programming language.",
        listof("VS Code", "Sublime Text", "WebStorm", "Atom")),
    FileExtensionInfo(".ts", "TypeScript Source", "application/typescript", "Code",
        "Typed superset of JavaScript by Microsoft.",
        listof("VS Code", "WebStorm", "Sublime Text")),
    FileExtensionInfo(".c", "C Source Code", "text/x-csrc", "Code",
        "Source code for C programming language.",
        listof("VS Code", "CLion", "GCC", "Visual Studio")),
    FileExtensionInfo(".cpp", "C++ Source Code", "text/x-c++src", "Code",
        "Source code for C++ programming language.",
        listof("VS Code", "CLion", "Visual Studio", "GCC")),
    FileExtensionInfo(".cs", "C# Source Code", "text/x-csharp", "Code",
        "Source code for C# programming language.",
        listof("Visual Studio", "VS Code", "Rider", "MonoDevelop")),
    FileExtensionInfo(".php", "PHP Source Code", "application/x-php", "Code",
        "Server-side scripting language for web development.",
        listof("PHPStorm", "VS Code", "Sublime Text", "NetBeans")),
    FileExtensionInfo(".rb", "Ruby Source Code", "application/x-ruby", "Code",
        "Source code for Ruby programming language.",
        listof("RubyMine", "VS Code", "Sublime Text", "Atom")),
    FileExtensionInfo(".go", "Go Source Code", "text/x-go", "Code",
        "Source code for Go programming language by Google.",
        listof("GoLand", "VS Code", "Sublime Text", "LiteIDE")),
    FileExtensionInfo(".rs", "Rust Source Code", "text/x-rustsrc", "Code",
        "Source code for Rust systems programming language.",
        listof("VS Code", "CLion", "Rust Analyzer", "Sublime Text")),
    FileExtensionInfo(".swift", "Swift Source Code", "text/x-swift", "Code",
        "Source code for Swift programming language by Apple.",
        listof("Xcode", "AppCode", "VS Code")),
    FileExtensionInfo(".html", "HyperText Markup Language", "text/html", "Code",
        "Standard markup language for creating web pages.",
        listof("VS Code", "Sublime Text", "Brackets", "Notepad++")),
    FileExtensionInfo(".css", "Cascading Style Sheets", "text/css", "Code",
        "Style sheet language for describing HTML presentation.",
        listof("VS Code", "Sublime Text", "Brackets", "Stylus")),
    FileExtensionInfo(".json", "JavaScript Object Notation", "application/json", "Code",
        "Lightweight data interchange format.",
        listof("VS Code", "JSON Viewer", "Online editors", "Any text editor")),
    FileExtensionInfo(".xml", "Extensible Markup Language", "application/xml", "Code",
        "Markup language for encoding documents.",
        listof("VS Code", "XMLSpy", "Oxygen XML", "Notepad++")),
    FileExtensionInfo(".sql", "Structured Query Language", "application/sql", "Code",
        "Database query and manipulation language.",
        listof("MySQL Workbench", "DBeaver", "SQL Server Management Studio", "pgAdmin")),
    FileExtensionInfo(".sh", "Shell Script", "application/x-sh", "Code",
        "Script file for Unix/Linux shell interpreters.",
        listof("Terminal", "VS Code", "Sublime Text", "Nano")),
    FileExtensionInfo(".bat", "Batch File", "application/bat", "Code",
        "Script file for Windows Command Prompt.",
        listof("Notepad", "VS Code", "Command Prompt")),
    FileExtensionInfo(".ps1", "PowerShell Script", "text/x-powershell", "Code",
        "Script file for Windows PowerShell.",
        listof("PowerShell ISE", "VS Code", "Notepad++")),
    
    // Data Formats
    FileExtensionInfo(".csv", "Comma-Separated Values", "text/csv", "Data",
        "Plain text format for tabular data.",
        listof("Excel", "Google Sheets", "LibreOffice Calc", "VS Code")),
    FileExtensionInfo(".jsonl", "JSON Lines", "application/jsonl", "Data",
        "Newline-delimited JSON format for structured data streams.",
        listof("VS Code", "jq", "Python pandas", "DuckDB")),
    FileExtensionInfo(".yaml", "YAML Ain't Markup Language", "text/yaml", "Data",
        "Human-readable data serialization format.",
        listof("VS Code", "YAML Lint", "Online validators")),
    FileExtensionInfo(".yml", "YAML File", "text/yaml", "Data",
        "Alternative extension for YAML files.",
        listof("VS Code", "YAML Lint", "Config editors")),
    FileExtensionInfo(".toml", "Tom's Obvious Minimal Language", "text/toml", "Data",
        "Configuration file format designed to be easy to read.",
        listof("VS Code", "TOML parsers", "Cargo config")),
    FileExtensionInfo(".ini", "Initialization File", "text/x-ini", "Data",
        "Simple configuration file format.",
        listof("Notepad", "VS Code", "INI editors")),
    FileExtensionInfo(".sqlite", "SQLite Database", "application/vnd.sqlite3", "Data",
        "Self-contained SQL database stored as a single file.",
        listof("DB Browser for SQLite", "DBeaver", "SQLiteStudio", "Valentina Studio")),
    FileExtensionInfo(".db", "Database File", "application/octet-stream", "Data",
        "Generic database file (format varies by application).",
        listof("Various DB tools", "Depends on source application")),
    FileExtensionInfo(".parquet", "Apache Parquet", "application/parquet", "Data",
        "Columnar storage format for efficient analytics.",
        listof("PyArrow", "Spark", "Pandas", "DuckDB")),
    
    // System/Executable Files
    FileExtensionInfo(".exe", "Windows Executable", "application/vnd.microsoft.portable-executable", "System",
        "Executable program file for Windows operating system.",
        listof("Windows", "Wine", "DOSBox")),
    FileExtensionInfo(".msi", "Windows Installer Package", "application/x-msi", "System",
        "Installation package for Windows software.",
        listof("Windows Installer", "7-Zip", "Orca MSI Editor")),
    FileExtensionInfo(".dll", "Dynamic Link Library", "application/x-msdownload", "System",
        "Library file containing code and data for Windows programs.",
        listof("Windows", "Dependency Walker", "PE Explorer")),
    FileExtensionInfo(".so", "Shared Object", "application/x-sharedlib", "System",
        "Dynamic library file for Unix/Linux systems.",
        listof("Linux", "ldd", "nm", "objdump")),
    FileExtensionInfo(".app", "macOS Application", "application/x-apple-diskimage", "System",
        "Application bundle directory for macOS.",
        listof("macOS Finder", "Xcode")),
    FileExtensionInfo(".apk", "Android Package Kit", "application/vnd.android.package-archive", "System",
        "Installable package file for Android applications.",
        listof("Android", "APK Installer", "ADB")),
    FileExtensionInfo(".deb", "Debian Package", "application/vnd.debian.binary-package", "System",
        "Software package format for Debian-based Linux distributions.",
        listof("dpkg", "apt", "GDebi", "Software Center")),
    FileExtensionInfo(".rpm", "Red Hat Package Manager", "application/x-rpm", "System",
        "Package format for Red Hat/Fedora/CentOS Linux.",
        listof("rpm", "dnf", "yum", "PackageKit")),
    FileExtensionInfo(".sys", "System File", "application/octet-stream", "System",
        "Windows device driver or system file.",
        listof("Windows", "Driver utilities")),
    FileExtensionInfo(".drv", "Device Driver", "application/octet-stream", "System",
        "Hardware driver file for various operating systems.",
        listof("OS-specific driver installers")),
    
    // Other Common Formats
    FileExtensionInfo(".log", "Log File", "text/plain", "Other",
        "Text file recording events or transactions.",
        listof("Text editors", "Log viewers", "Tail", "grep")),
    FileExtensionInfo(".cfg", "Configuration File", "text/plain", "Other",
        "Generic configuration file for applications.",
        listof("Text editors", "Application-specific tools")),
    FileExtensionInfo(".conf", "Configuration File", "text/plain", "Other",
        "Configuration settings file for services or apps.",
        listof("Text editors", "Application-specific tools")),
    FileExtensionInfo(".bak", "Backup File", "application/octet-stream", "Other",
        "Backup copy of an original file.",
        listof("Original application", "Text editors for some types")),
    FileExtensionInfo(".tmp", "Temporary File", "application/octet-stream", "Other",
        "Temporary file created during program operation.",
        listof("Can usually be deleted safely")),
    FileExtensionInfo(".cache", "Cache File", "application/octet-stream", "Other",
        "Cached data for faster access.",
        listof("Application-specific", "Can be regenerated")),
    FileExtensionInfo(".lock", "Lock File", "application/octet-stream", "Other",
        "Indicates a resource is in use by a process.",
        listof("Application-specific")),
    FileExtensionInfo(".env", "Environment Variables", "text/plain", "Other",
        "Environment configuration for applications.",
        listof("VS Code", "dotenv", "Text editors")),
    FileExtensionInfo(".md", "Markdown Document", "text/markdown", "Other",
        "Lightweight markup language for formatted text.",
        listof("Typora", "VS Code", "Obsidian", "MarkText")),
    FileExtensionInfo(".key", "Keynote Presentation", "application/x-iwork-keynote-sffkey", "Other",
        "Apple Keynote presentation file.",
        listof("Keynote", "iWork suite")),
    FileExtensionInfo(".numbers", "Numbers Spreadsheet", "application/x-iwork-numbers-sffnumbers", "Other",
        "Apple Numbers spreadsheet file.",
        listof("Numbers", "iWork suite")),
    FileExtensionInfo(".pages", "Pages Document", "application/x-iwork-pages-sffpages", "Other",
        "Apple Pages word processor document.",
        listof("Pages", "iWork suite")),
)

// Helper function for list creation
private fun <T> listof(vararg items: T): List<T> = items.toList()
