package com.dannyk.toolbox.ui.screens.tools.everyday

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ToolBoxApplication
import com.dannyk.toolbox.data.local.preferences.PreferencesManager
import com.dannyk.toolbox.ui.components.ToolHeader
import kotlinx.coroutines.launch
import java.util.Calendar
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign

data class Habit(
    val id: Int,
    val name: String,
    var dailyCount: Int = 0,
    var targetCount: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val colorIndex: Int = 0
)

data class DailyRecord(
    val date: String, // "yyyy-MM-dd"
    val counts: MutableMap<Int, Int> // habitId -> count
)

@Composable
fun HabitCounterScreen(navHostController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val preferencesManager = (context.applicationContext as ToolBoxApplication).preferencesManager
    val scope = rememberCoroutineScope()
    
    // Habits state - persisted via DataStore would be ideal, using remember for now with save/load
    var habits by remember { mutableStateOf(listOf<Habit>()) }
    var dailyRecords by remember { mutableStateOf(mapOf<String, DailyRecord>()) }
    
    // UI State
    var showAddDialog by remember { mutableStateOf(false) }
    var editingHabit by remember { mutableStateOf<Habit?>(null) }
    var newHabitName by remember { mutableStateOf("") }
    var newHabitTarget by remember { mutableIntStateOf(1) }
    var selectedHabitForHeatmap by remember { mutableStateOf<Habit?>(null) }
    
    // Load saved data on first composition
    LaunchedEffect(Unit) {
        // In a real app, this would load from Room/DataStore
        // For now, initialize with empty list or load from preferences
        val savedHabits = preferencesManager.getHabits()
        if (savedHabits.isNotEmpty()) {
            habits = savedHabits
        }
        
        val savedRecords = preferencesManager.getDailyRecords()
        if (savedRecords.isNotEmpty()) {
            dailyRecords = savedRecords
        }
    }
    
    // Get today's date string
    fun getTodayString(): String {
        val cal = Calendar.getInstance()
        return "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
    }
    
    // Update habit count for today
    fun updateHabitCount(habitId: Int, delta: Int) {
        habits = habits.map { habit ->
            if (habit.id == habitId) {
                habit.copy(dailyCount = (habit.dailyCount + delta).coerceAtLeast(0))
            } else habit
        }
        
        // Update daily record
        val today = getTodayString()
        val todayRecord = dailyRecords[today] ?: DailyRecord(today, mutableMapOf())
        val currentCount = todayRecord.counts[habitId] ?: 0
        todayRecord.counts[habitId] = (currentCount + delta).coerceAtLeast(0)
        dailyRecords = dailyRecords + (today to todayRecord)
        
        // Save to preferences
        scope.launch {
            preferencesManager.saveHabits(habits)
            preferencesManager.saveDailyRecords(dailyRecords)
        }
    }
    
    // Add new habit
    fun addHabit(name: String, target: Int) {
        val nextId = if (habits.isNotEmpty()) habits.maxOf { it.id } + 1 else 1
        val colorIndex = habits.size % habitColors.size
        val newHabit = Habit(id = nextId, name = name, targetCount = target, colorIndex = colorIndex)
        habits = habits + newHabit
        
        scope.launch {
            preferencesManager.saveHabits(habits)
        }
    }
    
    // Delete habit
    fun deleteHabit(habitId: Int) {
        habits = habits.filter { it.id != habitId }
        
        scope.launch {
            preferencesManager.saveHabits(habits)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolHeader(
            title = "Habit Counter",
            subtitle = "Track and build positive habits",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Summary cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "${habits.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Habits",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val todayTotal = habits.sumOf { it.dailyCount }
                    Text(
                        text = "$todayTotal",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Today's Total",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val completedCount = habits.count { it.dailyCount >= it.targetCount }
                    Text(
                        text = "$completedCount/${habits.size}",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Completed",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Add habit button
        Button(
            onClick = { 
                editingHabit = null
                newHabitName = ""
                newHabitTarget = 1
                showAddDialog = true 
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Habit")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Habits list
        if (habits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.TrackChanges,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No habits yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "Tap 'Add New Habit' to get started",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 80.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(habits, key = { it.id }) { habit ->
                    val isCompleted = habit.dailyCount >= habit.targetCount
                    val progress = (habit.dailyCount.toFloat() / habit.targetCount.toFloat()).coerceIn(0f, 1f)
                    
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isCompleted) 
                                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f) 
                            else 
                                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Color indicator
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(habitColors[habit.colorIndex % habitColors.size]),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                                
                                Spacer(modifier = Modifier.width(12.dp))
                                
                                // Name and info
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = habit.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "${habit.dailyCount} / ${habit.targetCount} today",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        
                                        if (isCompleted) {
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = MaterialTheme.colorScheme.primary
                                            ) {
                                                Text(
                                                    text = "Done!",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = Color.White,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                                
                                // View heatmap button
                                IconButton(onClick = { selectedHabitForHeatmap = habit }) {
                                    Icon(
                                        Icons.Default.CalendarMonth,
                                        contentDescription = "View heatmap",
                                        tint = MaterialTheme.colorScheme.primary
                                    )
                                }
                                
                                // Menu button
                                var showMenu by remember { mutableStateOf(false) }
                                
                                Box {
                                    IconButton(onClick = { showMenu = true }) {
                                        Icon(
                                            Icons.Default.MoreVert,
                                            contentDescription = "Options"
                                        )
                                    }
                                    
                                    DropdownMenu(
                                        expanded = showMenu,
                                        onDismissRequest = { showMenu = false }
                                    ) {
                                        DropdownMenuItem(
                                            onClick = {
                                                editingHabit = habit
                                                newHabitName = habit.name
                                                newHabitTarget = habit.targetCount
                                                showAddDialog = true
                                                showMenu = false
                                            },
                                            text = { Row {
                                                Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                                Spacer(Modifier.width(8.dp))
                                                Text("Edit")
                                            }}
                                        )
                                        DropdownMenuItem(
                                            onClick = {
                                                deleteHabit(habit.id)
                                                showMenu = false
                                            },
                                            text = { Row {
                                                Icon(Icons.Default.Delete, null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.error)
                                                Spacer(Modifier.width(8.dp))
                                                Text("Delete", color = MaterialTheme.colorScheme.error)
                                            }}
                                        )
                                    }
                                }
                            }
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // Progress bar
                            LinearProgressIndicator(
                                progress = { progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(4.dp)),
                                color = if (isCompleted) MaterialTheme.colorScheme.primary 
                                       else habitColors[habit.colorIndex % habitColors.size],
                                trackColor = MaterialTheme.colorScheme.surfaceContainerest
                            )
                            
                            Spacer(modifier = Modifier.height(12.dp))
                            
                            // +/- buttons
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                // Decrease button
                                OutlinedButton(
                                    onClick = { updateHabitCount(habit.id, -1) },
                                    enabled = habit.dailyCount > 0,
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Remove, null)
                                }
                                
                                // Count display
                                Surface(
                                    shape = RoundedCornerShape(12.dp),
                                    color = MaterialTheme.colorScheme.surfaceContainer
                                ) {
                                    Text(
                                        text = "${habit.dailyCount}",
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp)
                                    )
                                }
                                
                                // Increase button
                                Button(
                                    onClick = { updateHabitCount(habit.id, 1) },
                                    shape = CircleShape,
                                    modifier = Modifier.size(48.dp),
                                    contentPadding = PaddingValues(0.dp)
                                ) {
                                    Icon(Icons.Default.Add, null)
                                }
                            }
                            
                            // Quick add buttons
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 8.dp),
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                listOf(1, 5, 10).forEach { amount ->
                                    FilterChip(
                                        selected = false,
                                        onClick = { updateHabitCount(habit.id, amount) },
                                        label = { Text("+$amount", fontSize = MaterialTheme.typography.labelSmall.fontSize) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Add/Edit dialog
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text(if (editingHabit != null) "Edit Habit" else "New Habit") },
                text = {
                    Column {
                        OutlinedTextField(
                            value = newHabitName,
                            onValueChange = { newHabitName = it },
                            label = { Text("Habit name") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Default.Flag, null) }
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        Text(text = "Daily Target", style = MaterialTheme.typography.bodyMedium)
                        
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IconButton(onClick = { newHabitTarget = (newHabitTarget - 1).coerceAtLeast(1) }) {
                                Icon(Icons.Default.Remove, null)
                            }
                            
                            Text(
                                text = "$newHabitTarget",
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold
                            )
                            
                            IconButton(onClick = { newHabitTarget++ }) {
                                Icon(Icons.Default.Add, null)
                            }
                            
                            Text(
                                text = "times per day",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (newHabitName.isNotBlank()) {
                                if (editingHabit != null) {
                                    // Edit existing
                                    habits = habits.map {
                                        if (it.id == editingHabit!!.id) {
                                            it.copy(name = newHabitName, targetCount = newHabitTarget)
                                        } else it
                                    }
                                } else {
                                    // Add new
                                    addHabit(newHabitName, newHabitTarget)
                                }
                                showAddDialog = false
                            }
                        },
                        enabled = newHabitName.isNotBlank()
                    ) {
                        Text(if (editingHabit != null) "Save" else "Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
        
        // Heatmap dialog
        selectedHabitForHeatmap?.let { habit ->
            AlertDialog(
                onDismissRequest = { selectedHabitForHeatmap = null },
                title = { 
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(habitColors[habit.colorIndex % habitColors.size])
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("${habit.name} - Heatmap")
                    }
                },
                text = {
                    HeatmapCalendar(
                        habit = habit,
                        dailyRecords = dailyRecords
                    )
                },
                confirmButton = {
                    TextButton(onClick = { selectedHabitForHeatmap = null }) {
                        Text("Close")
                    }
                }
            )
        }
    }
}

@Composable
private fun HeatmapCalendar(
    habit: Habit,
    dailyRecords: Map<String, DailyRecord>
) {
    val calendar = Calendar.getInstance()
    val currentDay = calendar.get(Calendar.DAY_OF_MONTH)
    val currentMonth = calendar.get(Calendar.MONTH)
    val currentYear = calendar.get(Calendar.YEAR)
    
    // Generate last 35 days of data
    val days = (34 downTo 0).map { daysAgo ->
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_MONTH, -daysAgo)
        val dateStr = "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.MONTH) + 1}-${cal.get(Calendar.DAY_OF_MONTH)}"
        val record = dailyRecords[dateStr]
        val count = record?.counts?.get(habit.id) ?: 0
        Triple(cal.get(Calendar.DAY_OF_MONTH), count, daysAgo == 0)
    }
    
    Column {
        Text(
            text = "Last 35 Days Activity",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Grid of cells (7 columns for weeks)
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            // Day labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun").forEach { day ->
                    Text(
                        text = day,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                        modifier = Modifier.weight(1f),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
            
            // Week rows
            val weeks = days.chunked(7)
            weeks.forEach { week ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Fill row to 7 items
                    val paddedWeek = week + List(7 - week.size) { Triple(0, 0, false) }
                    
                    paddedWeek.forEach { (day, count, isToday) ->
                        val intensity = when {
                            count == 0 -> 0f
                            count < habit.targetCount -> 0.33f
                            count == habit.targetCount -> 0.66f
                            else -> 1f
                        }
                        
                        val baseColor = habitColors[habit.colorIndex % habitColors.size]
                        
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .aspectRatio(1f)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    if (isToday) MaterialTheme.colorScheme.primary
                                    else baseColor.copy(alpha = intensity * 0.7f)
                                )
                                .then(
                                    if (isToday) Modifier.then(
                                        androidx.compose.ui.draw.border(
                                            width = 2.dp,
                                            color = MaterialTheme.colorScheme.primary,
                                            shape = RoundedCornerShape(4.dp)
                                        )
                                    ) else Modifier
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            if (count > 0 && !isToday) {
                                Text(
                                    text = "$count",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (intensity > 0.5f) Color.White else Color.Transparent
                                )
                            }
                        }
                    }
                }
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Legend
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Less",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.width(8.dp))
            
            listOf(0f, 0.25f, 0.5f, 0.75f, 1f).forEach { intensity ->
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(habitColors[habit.colorIndex % habitColors.size].copy(alpha = intensity * 0.7f))
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "More",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Stats
        val totalDaysWithData = dailyRecords.values.count { it.counts.containsKey(habit.id) }
        val totalCount = dailyRecords.values.sumOf { it.counts[habit.id] ?: 0 }
        val avgPerDay = if (totalDaysWithData > 0) totalCount.toDouble() / totalDaysWithData else 0.0
        
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$totalCount", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Total", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = String.format("%.1f", avgPerDay), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Avg/Day", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "$totalDaysWithData", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(text = "Active Days", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

// Predefined colors for habits
val habitColors = listOf(
    Color(0xFFE53935), // Red
    Color(0xFF43A047), // Green
    Color(0xFF1E88E5), // Blue
    Color(0xFFFB8C00), // Orange
    Color(0xFF8E24AA), // Purple
    Color(0xFF00ACC1), // Cyan
    Color(0xFFFF7043), // Deep Orange
    Color(0xFF3949AB), // Indigo
)
