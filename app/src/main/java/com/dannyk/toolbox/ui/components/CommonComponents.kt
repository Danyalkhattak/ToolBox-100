package com.dannyk.toolbox.ui.components

import java.text.DecimalFormat

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ToolBoxApplication
import com.dannyk.toolbox.data.local.preferences.PreferencesManager
import com.dannyk.toolbox.domain.model.Tool
import androidx.compose.ui.graphics.Color
import android.content.Context
import androidx.compose.material.icons.filled.Check
import com.dannyk.toolbox.ui.components.formatCurrency

// Icon mapping for tool icons
@Composable
fun getToolIcon(iconResName: String): ImageVector {
    return when (iconResName) {
        "calculate" -> Icons.Outlined.Calculate
        "science" -> Icons.Outlined.Science
        "percent" -> Icons.Outlined.Percent
        "local_offer" -> Icons.Outlined.LocalOffer
        "restaurant" -> Icons.Outlined.Restaurant
        "group" -> Icons.Outlined.Group
        "cake" -> Icons.Outlined.Cake
        "event" -> Icons.Outlined.Event
        "schedule" -> Icons.Outlined.Schedule
        "monitor_weight" -> Icons.Outlined.MonitorWeight
        "straighten" -> Icons.Outlined.Straighten
        "fitness_center" -> Icons.Outlined.FitnessCenter
        "thermostat" -> Icons.Outlined.Thermostat
        "crop_square" -> Icons.Outlined.CropSquare
        "water_drop" -> Icons.Outlined.WaterDrop
        "speed" -> Icons.Outlined.Speed
        "timer" -> Icons.Outlined.Timer
        "storage" -> Icons.Outlined.Storage
        "binary" -> Icons.Outlined.Code
        "looks_one" -> Icons.Outlined.LooksOne
        "fraction" -> Icons.Outlined.FormatListNumbered
        "compare_arrows" -> Icons.Outlined.CompareArrows
        "tune" -> Icons.Outlined.Tune
        "analytics" -> Icons.Outlined.Analytics
        "functions" -> Icons.Outlined.Functions
        "verified" -> Icons.Outlined.Verified
        "exclamation" -> Icons.Outlined.PriorityHigh
        "power" -> Icons.Outlined.Power
        "square_root" -> Icons.Outlined.Functions
        "casino" -> Icons.Outlined.Casino
        "format_list_numbered" -> Icons.Outlined.FormatListNumbered
        "text_fields" -> Icons.Outlined.Edit
        "subject" -> Icons.Outlined.Subject
        "format_case" -> Icons.Outlined.TextFormat
        "undo" -> Icons.Outlined.Undo
        "content_copy_off" -> Icons.Outlined.ContentCopy
        "sort_by_alpha" -> Icons.Outlined.SortByAlpha
        "find_replace" -> Icons.Outlined.FindInPage
        "diff" -> Icons.Outlined.CompareArrows
        "link" -> Icons.Outlined.Link
        "data_object" -> Icons.Outlined.DataObject
        "check_circle" -> Icons.Outlined.CheckCircle
        "lock" -> Icons.Outlined.Lock
        "lock_open" -> Icons.Outlined.LockOpen
        "code" -> Icons.Outlined.Code
        "html" -> Icons.Outlined.Code
        "fingerprint" -> Icons.Outlined.Fingerprint
        "regular_expression" -> Icons.Outlined.TextFields
        "password" -> Icons.Outlined.Password
        "security_update" -> Icons.Outlined.Security
        "enhanced_encryption" -> Icons.Outlined.EnhancedEncryption
        "vpn_key" -> Icons.Outlined.VpnKey
        "key" -> Icons.Outlined.VpnKey
        "verified_user" -> Icons.Outlined.VerifiedUser
        "token" -> Icons.Outlined.VpnKey
        "dialpad" -> Icons.Outlined.Dialpad
        "chat" -> Icons.Outlined.ChatBubbleOutline
        "help" -> Icons.Outlined.Help
        "ip" -> Icons.Outlined.Language
        "dns" -> Icons.Outlined.Dns
        "network_check" -> Icons.Outlined.NetworkCheck
        "http" -> Icons.Outlined.Language
        "smartphone" -> Icons.Outlined.Smartphone
        "qr_code" -> Icons.Outlined.QrCode2
        "qr_code_scanner" -> Icons.Outlined.QrCodeScanner
        "wifi" -> Icons.Outlined.Wifi
        "barcode" -> Icons.Outlined.CenterFocusStrong
        "compress" -> Icons.Outlined.Compress
        "crop" -> Icons.Outlined.Crop
        "crop_rotate" -> Icons.Outlined.CropRotate
        "image" -> Icons.Outlined.Image
        "colorize" -> Icons.Outlined.Colorize
        "palette" -> Icons.Outlined.Palette
        "gradient" -> Icons.Outlined.Brush
        "color_lens" -> Icons.Outlined.ColorLens
        "contrast" -> Icons.Outlined.Contrast
        "info" -> Icons.Outlined.Info
        "picture_as_pdf" -> Icons.Outlined.PictureAsPdf
        "pdf" -> Icons.Outlined.PictureAsPdf
        "description" -> Icons.Outlined.Description
        "qr_code_2" -> Icons.Outlined.QrCode2
        "insert_drive_file" -> Icons.Outlined.InsertDriveFile
        "web" -> Icons.Outlined.Language
        "edit_note" -> Icons.Outlined.EditNote
        "alarm" -> Icons.Outlined.Alarm
        "hourglass_top" -> Icons.Outlined.HourglassTop
        "public" -> Icons.Outlined.Public
        "music_note" -> Icons.Outlined.MusicNote
        "currency_exchange" -> Icons.Outlined.CurrencyExchange
        "shuffle" -> Icons.Outlined.Shuffle
        "track_changes" -> Icons.Outlined.TrackChanges
        "note" -> Icons.Outlined.Note
        else -> Icons.Outlined.Build
    }
}

@Composable
fun ToolCard(
    tool: Tool,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val preferencesManager = (context.applicationContext as ToolBoxApplication).preferencesManager
    val favoriteIds by preferencesManager.favoriteIds.collectAsState(initial = emptySet())
    val isFavorite = tool.id in favoriteIds
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(48.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getToolIcon(tool.iconResName),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Text content
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (onFavoriteClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error 
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryCard(
    name: String,
    icon: ImageVector,
    toolCount: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(20.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "$toolCount tools",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    placeholder: String = "Search tools...",
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        placeholder = { Text(placeholder, color = MaterialTheme.colorScheme.onSurfaceVariant) },
        leadingIcon = {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (query.isNotEmpty()) {
            {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        singleLine = true,
        shape = MaterialTheme.shapes.large,
        modifier = modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = MaterialTheme.colorScheme.primary,
            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
        )
    )
}

@Composable
fun SectionHeader(
    title: String,
    actionText: String? = null,
    onActionClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        if (actionText != null && onActionClick != null) {
            TextButton(onClick = onActionClick) {
                Text(
                    text = actionText,
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
fun ResultCard(
    title: String? = null,
    result: String,
    onCopy: (() -> Unit)? = null,
    onShare: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            title?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = result,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                if (onCopy != null) {
                    IconButton(onClick = onCopy, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
                
                if (onShare != null) {
                    IconButton(onClick = onShare, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share",
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyState(
    icon: ImageVector = Icons.Default.Search,
    message: String = "No results found",
    subMessage: String? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        subMessage?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorState(
    message: String = "Something went wrong",
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.Error,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.error
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = message,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.error
        )
        if (onRetry != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Button(onClick = onRetry) {
                Text("Try Again")
            }
        }
    }
}

@Composable
fun LoadingState(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(48.dp),
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Loading...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
fun ToolHeader(
    title: String,
    subtitle: String? = null,
    isFavorite: Boolean = false,
    onBack: () -> Unit,
    onFavorite: ((Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Top bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back"
                )
            }
            
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            
            if (onFavorite != null) {
                IconButton(onClick = { onFavorite(!isFavorite) }) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Remove from favorites" else "Add to favorites",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
        
        // Subtitle
        subtitle?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)
            )
        }
    }
}

/**
 * Simple top bar used by individual tool screens that manage their own
 * Scaffold/layout and just need a back button + title.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolTopBar(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Top app bar for tool screens that build their own Scaffold and pass
 * this in as the `topBar`. Handles back navigation via [navController].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolTopAppBar(
    title: String,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    TopAppBar(
        title = { Text(text = title) },
        navigationIcon = {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back"
                )
            }
        },
        modifier = modifier
    )
}

/**
 * Full-screen scaffold wrapper for tool screens: renders a [ToolTopAppBar]
 * and hosts [content] below it, respecting the Scaffold's inner padding.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolScreenLayout(
    title: String,
    navController: NavHostController,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Scaffold(
        modifier = modifier,
        topBar = { ToolTopAppBar(title = title, navController = navController) }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            content()
        }
    }
}

@Composable
fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        icon?.let {
            Icon(imageVector = it, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
    }
}

@Composable
fun SecondaryButton(
    text: String,
    onClick: () -> Unit,
    enabled: Boolean = true,
    icon: ImageVector? = null,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        enabled = enabled,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large
    ) {
        icon?.let {
            Icon(imageVector = it, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text)
    }
}


/**
 * Format a number as currency string
 */
fun formatCurrency(amount: Double): String {
    return try {
        DecimalFormat("$#,##0.00").format(amount)
    } catch (e: Exception) {
        "$0.00"
    }
}
