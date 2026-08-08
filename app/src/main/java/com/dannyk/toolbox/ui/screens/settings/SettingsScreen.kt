package com.dannyk.toolbox.ui.screens.settings

import android.content.Intent
import android.net.Uri
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.BuildConfig
import com.dannyk.toolbox.ToolBoxApplication
import com.dannyk.toolbox.data.local.preferences.PreferencesManager
import kotlinx.coroutines.launch
import android.content.Context
import androidx.compose.material3.Divider
import androidx.compose.material.icons.filled.Check

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
    
    LaunchedEffect(Unit) {
        preferencesManager.themeMode.collect { theme ->
            selectedTheme = theme
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Appearance Section
            SettingsSection(title = "Appearance", icon = Icons.Default.Palette) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Theme",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // Theme options
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
                            icon = Icons.Default.LightBulb,
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
            
            Divider()
            
            // App Section
            SettingsSection(title = "App", icon = Icons.Default.Apps) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsItem(
                        title = "Favorites",
                        subtitle = "Manage your favorite tools",
                        icon = Icons.Default.Star,
                        onClick = { /* Navigate to favorites */ }
                    )
                    
                    SettingsItem(
                        title = "Recently Used Tools",
                        subtitle = "${BuildConfig.VERSION_NAME}",
                        icon = Icons.Default.History,
                        onClick = { /* Show recent */ }
                    )
                    
                    SettingsItem(
                        title = "Clear Recent History",
                        subtitle = "Remove all recently used tools",
                        icon = Icons.Default.Delete,
                        onClick = { showClearHistoryDialog = true }
                    )
                }
            }
            
            Divider()
            
            // Updates Section
            SettingsSection(title = "Updates", icon = Icons.Default.CloudDownload) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    SettingsItem(
                        title = "Current Version",
                        subtitle = BuildConfig.VERSION_NAME,
                        icon = Icons.Default.Info
                    )
                    
                    SettingsItem(
                        title = "Check for Updates",
                        subtitle = "Check if a newer version is available",
                        icon = Icons.Default.Download,
                        onClick = { /* Check updates */ }
                    )
                }
            }
            
            Divider()
            
            // About Section
            SettingsSection(title = "About", icon = Icons.Default.Info) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // App info card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = Icons.Default.Build,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ToolBox-100",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Version ${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "100 tools in one app",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    
                    SettingsItem(
                        title = "GitHub Repository",
                        subtitle = "View source code and report issues",
                        icon = Icons.Default.Code,
                        onClick = {
                            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/Danyalkhattak/ToolBox-100"))
                            context.startActivity(intent)
                        }
                    )
                    
                    SettingsItem(
                        title = "Open Source Licenses",
                        subtitle = "View third-party licenses",
                        icon = Icons.Default.Description,
                        onClick = { /* Show licenses dialog */ }
                    )
                    
                    SettingsItem(
                        title = "Privacy Policy",
                        subtitle = "No data is collected or shared",
                        icon = Icons.Default.Security,
                        onClick = { /* Show privacy policy */ }
                    )
                }
            }
            
            // Copyright
            Text(
                text = "\u00A9 2024 ToolBox-100. All rights reserved.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                textAlign = TextAlign.Center
            )
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
                .padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
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
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text, style = MaterialTheme.typography.labelSmall)
        }
    }
}
