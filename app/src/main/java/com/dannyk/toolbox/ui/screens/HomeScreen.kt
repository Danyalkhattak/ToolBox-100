package com.dannyk.toolbox.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ToolBoxApplication
import com.dannyk.toolbox.data.local.preferences.PreferencesManager
import com.dannyk.toolbox.domain.model.Category
import com.dannyk.toolbox.tools.ToolRegistry
import com.dannyk.toolbox.ui.components.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavHostController) {
    val context = LocalContext.current
    val preferencesManager = (context.applicationContext as ToolBoxApplication).preferencesManager
    
    var searchQuery by remember { mutableStateOf("") }
    
    val favoriteIds by preferencesManager.favoriteIds.collectAsState(initial = emptySet())
    val recentTools by preferencesManager.recentTools.collectAsState(initial = emptyList())
    
    // Show category view or search results
    val showSearchResults = searchQuery.isNotEmpty()
    val searchResults = if (showSearchResults) ToolRegistry.searchTools(searchQuery) else emptyList()
    
    // Get favorite and recent tools
    val favoriteTools = ToolRegistry.allTools.filter { it.id in favoriteIds }
    val recentToolIds = recentTools.map { it.first }.take(10)
    val recentToolsList = recentToolIds.mapNotNull { id -> ToolRegistry.getToolById(id) }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "ToolBox-100",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
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
                            icon = Icons.Default.SearchOff,
                            message = "No matching tools",
                            subMessage = "Try different keywords"
                        )
                    }
                } else {
                    item {
                        SectionHeader(title = "Search Results (${searchResults.size})")
                    }
                    
                    items(searchResults) { tool ->
                        ToolCard(
                            tool = tool,
                            onClick = {
                                navigateToTool(navController, tool.id, preferencesManager)
                            },
                            onFavoriteClick = {
                                toggleFavorite(tool.id, preferencesManager)
                            }
                        )
                    }
                }
            } else {
                // Favorites Section
                if (favoriteTools.isNotEmpty()) {
                    item {
                        SectionHeader(
                            title = "Favorites",
                            actionText = "See All",
                            onActionClick = { /* Navigate to favorites */ }
                        )
                    }
                    
                    items(favoriteTools.take(6)) { tool ->
                        ToolCard(
                            tool = tool,
                            onClick = {
                                navigateToTool(navController, tool.id, preferencesManager)
                            },
                            onFavoriteClick = {
                                toggleFavorite(tool.id, preferencesManager)
                            }
                        )
                    }
                }
                
                // Recently Used Section
                if (recentToolsList.isNotEmpty()) {
                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                        SectionHeader(
                            title = "Recently Used",
                            actionText = "Clear History",
                            onActionClick = {
                                // Clear history action
                            }
                        )
                    }
                    
                    items(recentToolsList) { tool ->
                        ToolCard(
                            tool = tool,
                            onClick = {
                                navigateToTool(navController, tool.id, preferencesManager)
                            },
                            onFavoriteClick = {
                                toggleFavorite(tool.id, preferencesManager)
                            }
                        )
                    }
                }
                
                // Categories Section
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(title = "Categories")
                }
                
                val categories = Category.entries
                items(categories) { category ->
                    val toolsInCategory = ToolRegistry.getToolsByCategory(category)
                    CategoryCard(
                        name = category.displayName,
                        icon = getToolIcon(toolsInCategory.firstOrNull()?.iconResName ?: "build"),
                        toolCount = toolsInCategory.size,
                        onClick = {
                            // Could navigate to category detail screen
                        }
                    )
                }
                
                // All Tools Grid
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    SectionHeader(title = "All Tools")
                }
                
                item {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier.height((ToolRegistry.allTools.size / 2 * 100).dp.coerceAtMost(2000.dp)),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(ToolRegistry.allTools) { tool ->
                            ToolCard(
                                tool = tool,
                                onClick = {
                                    navigateToTool(navController, tool.id, preferencesManager)
                                },
                                onFavoriteClick = {
                                    toggleFavorite(tool.id, preferencesManager)
                                },
                                modifier = Modifier.height(90.dp)
                            )
                        }
                    }
                }
            }
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
    // Toggle would be handled via state
}
