package com.dannyk.toolbox.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.navigation.NavHostController
import com.dannyk.toolbox.R
import com.dannyk.toolbox.ToolBoxApplication
import com.dannyk.toolbox.data.local.preferences.PreferencesManager
import com.dannyk.toolbox.domain.model.Category
import com.dannyk.toolbox.domain.model.Tool
import com.dannyk.toolbox.tools.ToolRegistry
import com.dannyk.toolbox.ui.components.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val preferencesManager = (context.applicationContext as ToolBoxApplication).preferencesManager
    val coroutineScope = rememberCoroutineScope()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<Category?>(null) }
    
    val favoriteIds by preferencesManager.favoriteIds.collectAsState(initial = emptySet())
    val recentTools by preferencesManager.recentTools.collectAsState(initial = emptyList())
    
    // Show category view or search results
    val showSearchResults = searchQuery.isNotEmpty()
    val searchResults = if (showSearchResults) ToolRegistry.searchTools(searchQuery) else emptyList()
    
    // Get favorite and recent tools
    val favoriteTools = ToolRegistry.allTools.filter { it.id in favoriteIds }
    val recentToolIds = recentTools.map { it.first }.take(10)
    val recentToolsList = recentToolIds.mapNotNull { id -> ToolRegistry.getToolById(id) }
    
    // Filter tools by category
    val filteredTools = if (selectedCategory != null) {
        ToolRegistry.getToolsByCategory(selectedCategory!!)
    } else {
        ToolRegistry.allTools
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Image(
                            painter = painterResource(id = R.mipmap.ic_launcher),
                            contentDescription = "ToolBox-100 Icon",
                            modifier = Modifier.size(40.dp),
                            contentScale = ContentScale.Crop
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "ToolBox-100",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { navController.navigate("settings") }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Settings"
                        )
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
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Search Bar
            item {
                SearchBar(
                    query = searchQuery,
                    onQueryChange = { searchQuery = it },
                    placeholder = "Search 100 tools..."
                )
            }
            
            if (showSearchResults) {
                // Search Results
                if (searchResults.isEmpty()) {
                    item {
                        EmptyState(
                            icon = Icons.Default.Search,
                            message = "No matching tools",
                            subMessage = "Try different keywords"
                        )
                    }
                } else {
                    item {
                        SectionHeader(title = "Search Results (${searchResults.size})")
                    }
                    
                    items(searchResults) { tool ->
                        WideToolCard(
                            tool = tool,
                            onClick = {
                                coroutineScope.launch {
                                    navigateToTool(navController, tool.id, preferencesManager)
                                }
                            },
                            onFavoriteClick = {
                                coroutineScope.launch {
                                    toggleFavorite(tool.id, preferencesManager)
                                }
                            }
                        )
                    }
                }
            } else {
                // Categories - Horizontal Chips
                item {
                    CategoryChipsRow(
                        categories = Category.entries,
                        selectedCategory = selectedCategory,
                        onCategorySelected = { category ->
                            selectedCategory = if (selectedCategory == category) null else category
                        }
                    )
                }
                
                // Favorites Section
                if (favoriteTools.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "⭐ Favorites (${favoriteTools.size})",
                            actionText = "See All",
                            onActionClick = { /* Navigate to favorites */ }
                        )
                    }
                    
                    items(favoriteTools.take(5)) { tool ->
                        WideToolCard(
                            tool = tool,
                            onClick = {
                                coroutineScope.launch {
                                    navigateToTool(navController, tool.id, preferencesManager)
                                }
                            },
                            onFavoriteClick = {
                                coroutineScope.launch {
                                    toggleFavorite(tool.id, preferencesManager)
                                }
                            }
                        )
                    }
                }
                
                // Recently Used Section
                if (recentToolsList.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "🕐 Recently Used",
                            actionText = "Clear History",
                            onActionClick = {
                                coroutineScope.launch {
                                    preferencesManager.clearRecentHistory()
                                }
                            }
                        )
                    }
                    
                    items(recentToolsList.take(8)) { tool ->
                        WideToolCard(
                            tool = tool,
                            onClick = {
                                coroutineScope.launch {
                                    navigateToTool(navController, tool.id, preferencesManager)
                                }
                            },
                            onFavoriteClick = {
                                coroutineScope.launch {
                                    toggleFavorite(tool.id, preferencesManager)
                                }
                            }
                        )
                    }
                }
                
                // All Tools Section - Wide cards, one per line
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(
                        title = if (selectedCategory != null) 
                            "${selectedCategory!!.displayName} (${filteredTools.size})" 
                        else 
                            "All Tools (${ToolRegistry.allTools.size})"
                    )
                }
                
                items(filteredTools) { tool ->
                    WideToolCard(
                        tool = tool,
                        onClick = {
                            coroutineScope.launch {
                                navigateToTool(navController, tool.id, preferencesManager)
                            }
                        },
                        onFavoriteClick = {
                            coroutineScope.launch {
                                toggleFavorite(tool.id, preferencesManager)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryChipsRow(
    categories: List<Category>,
    selectedCategory: Category?,
    onCategorySelected: (Category) -> Unit
) {
    Column {
        Text(
            text = "Categories",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )
        
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // "All" chip
            FilterChip(
                selected = selectedCategory == null,
                onClick = { /* Pass null to deselect - handled by parent */ },
                label = { 
                    Text(
                        text = "All (${ToolRegistry.allTools.size})",
                        style = MaterialTheme.typography.labelMedium
                    ) 
                },
                modifier = Modifier.height(36.dp)
            )
            
            categories.forEach { category ->
                val count = ToolRegistry.getToolsByCategory(category).size
                FilterChip(
                    selected = selectedCategory == category,
                    onClick = { onCategorySelected(category) },
                    label = { 
                        Text(
                            text = "${category.displayName} ($count)",
                            style = MaterialTheme.typography.labelMedium
                        ) 
                    },
                    modifier = Modifier.height(36.dp)
                )
            }
        }
    }
}

@Composable
private fun WideToolCard(
    tool: Tool,
    onClick: () -> Unit,
    onFavoriteClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val preferencesManager = (context.applicationContext as ToolBoxApplication).preferencesManager
    val favoriteIds by preferencesManager.favoriteIds.collectAsState(initial = emptySet())
    val isFavorite = tool.id in favoriteIds
    
    Card(
        modifier = Modifier
            .fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        onClick = onClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = getToolIcon(tool.iconResName),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(14.dp))
            
            // Text content - full width available
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = tool.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = tool.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }
            
            if (onFavoriteClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onFavoriteClick,
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = if (isFavorite) "Unfavorite" else "Favorite",
                        tint = if (isFavorite) MaterialTheme.colorScheme.error 
                               else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
                    )
                }
            }
            
            // Arrow indicator
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

private suspend fun navigateToTool(
    navController: NavHostController,
    toolId: Int,
    preferencesManager: PreferencesManager
) {
    val tool = ToolRegistry.getToolById(toolId) ?: return
    preferencesManager.addToRecent(toolId)
    navController.navigate(tool.route)
}

private suspend fun toggleFavorite(
    toolId: Int,
    preferencesManager: PreferencesManager
) {
    val currentFavorites = preferencesManager.favoriteIds.first()
    if (toolId in currentFavorites) {
        preferencesManager.removeFavorite(toolId)
    } else {
        preferencesManager.addFavorite(toolId)
    }
}
