package com.dannyk.toolbox.ui.screens.settings

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.BuildConfig
import com.dannyk.toolbox.R
import com.dannyk.toolbox.ToolBoxApplication
import com.dannyk.toolbox.data.local.preferences.PreferencesManager
import com.dannyk.toolbox.tools.ToolRegistry
import com.dannyk.toolbox.ui.components.ToolCard
import kotlinx.coroutines.launch
import android.content.Context
import androidx.compose.foundation.ScrollState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavHostController) {
    val context = LocalContext.current
    val preferencesManager = (context.applicationContext as ToolBoxApplication).preferencesManager
    val scope = rememberCoroutineScope()
    
    var showClearHistoryDialog by remember { mutableStateOf(false) }
    
    // Theme state
    var selectedTheme by remember { 
        mutableStateOf(PreferencesManager.THEME_SYSTEM)
    }
    
    // Recent tools for display
    val recentTools by preferencesManager.recentTools.collectAsState(initial = emptyList())
    val recentToolIds = recentTools.map { it.first }.take(10)
    val recentToolsList = recentToolIds.mapNotNull { id -> ToolRegistry.getToolById(id) }
    
    // Favorites for display
    val favoriteIds by preferencesManager.favoriteIds.collectAsState(initial = emptySet())
    val favoriteTools = ToolRegistry.allTools.filter { it.id in favoriteIds }
    
    LaunchedEffect(Unit) {
        preferencesManager.themeMode.collect { theme ->
            selectedTheme = theme
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Settings", fontWeight = FontWeight.Bold) 
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            text = "Theme",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    
                        // Theme options - horizontal row with equal width
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ThemeOption(
                                text = "System",
                                icon = Icons.Default.PhoneAndroid,
                                selected = selectedTheme == PreferencesManager.THEME_SYSTEM,
                                onClick = {
                                    scope.launch { preferencesManager.setThemeMode(PreferencesManager.THEME_SYSTEM) }
                                    selectedTheme = PreferencesManager.THEME_SYSTEM
                                },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOption(
                                text = "Light",
                                icon = Icons.Default.Lightbulb,
                                selected = selectedTheme == PreferencesManager.THEME_LIGHT,
                                onClick = {
                                    scope.launch { preferencesManager.setThemeMode(PreferencesManager.THEME_LIGHT) }
                                    selectedTheme = PreferencesManager.THEME_LIGHT
                                },
                                modifier = Modifier.weight(1f)
                            )
                            ThemeOption(
                                text = "Dark",
                                icon = Icons.Default.NightsStay,
                                selected = selectedTheme == PreferencesManager.THEME_DARK,
                                onClick = {
                                    scope.launch { preferencesManager.setThemeMode(PreferencesManager.THEME_DARK) }
                                    selectedTheme = PreferencesManager.THEME_DARK
                                },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
            
            // Favorites Section
            item {
                Divider()
                SettingsSection(title = "Favorites", icon = Icons.Default.Star) {
                    if (favoriteTools.isEmpty()) {
                        Text(
                            text = "No favorites yet. Tap the heart icon on any tool to add it here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            favoriteTools.take(5).forEach { tool ->
                                ToolCard(
                                    tool = tool,
                                    onClick = {
                                        navController.navigate(tool.route)
                                    },
                                    onFavoriteClick = {
                                        scope.launch {
                                            if (tool.id in favoriteIds) {
                                                preferencesManager.removeFavorite(tool.id)
                                            } else {
                                                preferencesManager.addFavorite(tool.id)
                                            }
                                        }
                                    }
                                )
                            }
                            if (favoriteTools.size > 5) {
                                TextButton(
                                    onClick = { /* Show all favorites */ },
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text("View All ${favoriteTools.size} Favorites")
                                }
                            }
                        }
                    }
                }
            }
            
            // Recently Used Section
            item {
                Divider()
                SettingsSection(title = "Recently Used", icon = Icons.Default.History) {
                    if (recentToolsList.isEmpty()) {
                        Text(
                            text = "No recent activity. Tools you use will appear here.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            recentToolsList.forEach { tool: com.dannyk.toolbox.domain.model.Tool ->
                                ToolCard(
                                    tool = tool,
                                    onClick = {
                                        navController.navigate(tool.route)
                                    }
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            OutlinedButton(
                                onClick = {
                                    showClearHistoryDialog = true
                                },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = MaterialTheme.colorScheme.error
                                )
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Clear History")
                            }
                        }
                    }
                }
            }
            
            // Updates Section
            item {
                Divider()
                SettingsSection(title = "Updates", icon = Icons.Default.CloudDownload) {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        SettingsItem(
                            title = "Current Version",
                            subtitle = BuildConfig.VERSION_NAME + " (Build ${BuildConfig.VERSION_CODE})",
                            icon = Icons.Default.Info
                        )
                        
                        SettingsItem(
                            title = "Check for Updates",
                            subtitle = "Check GitHub for latest version",
                            icon = Icons.Default.Download,
                            onClick = {
                                scope.launch {
                                    try {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Danyalkhattak/ToolBox-100/releases/latest"))
                                        context.startActivity(intent)
                                    } catch (e: Exception) {
                                        // Fallback: open main repo
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Danyalkhattak/ToolBox-100"))
                                        context.startActivity(intent)
                                    }
                                }
                            }
                        )
                    }
                }
            }
            
            // About Section - Fully Centered
            item {
                Divider()
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Section header centered
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(bottom = 16.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "About",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    
                    // App info card - Centered
                    Card(
                        modifier = Modifier.width(max(300.dp, LocalConfiguration.current.screenWidthDp.dp - 64.dp)),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            // App Icon - larger, no background
                            Image(
                                painter = painterResource(id = R.mipmap.ic_launcher),
                                contentDescription = "ToolBox-100 Icon",
                                modifier = Modifier.size(96.dp),
                                contentScale = ContentScale.Crop
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            Text(
                                text = "ToolBox-100",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "Version ${BuildConfig.VERSION_NAME}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            
                            Text(
                                text = "100+ tools in one powerful app",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            Text(
                                text = "Built with ❤️ using Jetpack Compose",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Links - centered with max width
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(max(300.dp, LocalConfiguration.current.screenWidthDp.dp - 64.dp)),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        SettingsItem(
                            title = "GitHub Repository",
                            subtitle = "View source code & contribute",
                            icon = Icons.Default.Code,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Danyalkhattak/ToolBox-100"))
                                context.startActivity(intent)
                            }
                        )
                        
                        SettingsItem(
                            title = "Open Source Licenses",
                            subtitle = "Third-party libraries",
                            icon = Icons.Default.Description,
                            onClick = { /* Show licenses */ }
                        )
                        
                        SettingsItem(
                            title = "Privacy Policy",
                            subtitle = "No data collected",
                            icon = Icons.Default.Security,
                            onClick = { /* Privacy */ }
                        )
                    }
                }
            }
            
            // Copyright - Centered
            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp, bottom = 32.dp)
                ) {
                    Text(
                        text = "\u00A9 2024 ToolBox-100. All rights reserved.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Made with ❤️ by Danny K",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
    
    // Clear history confirmation dialog
    if (showClearHistoryDialog) {
        AlertDialog(
            onDismissRequest = { showClearHistoryDialog = false },
            title = { Text("Clear Recent History?") },
            text = { Text("This will remove all recently used tool history. This action cannot be undone.") },
            confirmButton = {
                TextButton(onClick = {
                    scope.launch { preferencesManager.clearRecentHistory() }
                    showClearHistoryDialog = false
                }) {
                    Text("Clear", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearHistoryDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    icon: ImageVector,
    content: @Composable () -> Unit
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }
        content()
    }
}

@Composable
private fun SettingsItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: (() -> Unit)? = null
) {
    Surface(
        onClick = { onClick?.invoke() },
        enabled = onClick != null,
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onClick != null) {
                Icon(
                    imageVector = Icons.Default.KeyboardArrowRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ThemeOption(
    text: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (selected) MaterialTheme.colorScheme.primaryContainer else Color.Transparent,
            contentColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.Center) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text, style = MaterialTheme.typography.labelMedium)
        }
    }
}
