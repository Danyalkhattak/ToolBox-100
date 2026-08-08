package com.dannyk.toolbox.ui.screens.tools.everyday

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.dannyk.toolbox.ToolBoxApplication
import com.dannyk.toolbox.data.local.entity.NoteEntity
import com.dannyk.toolbox.ui.components.SearchBar
import com.dannyk.toolbox.ui.components.ToolHeader
import kotlinx.coroutines.launch
import androidx.compose.ui.graphics.Color
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.LaunchedEffect
import java.util.Calendar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotesScreen(navHostController: NavHostController) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val db = (context.applicationContext as ToolBoxApplication).database
    val noteDao = db.noteDao()
    val scope = rememberCoroutineScope()
    
    // Notes state from Room
    var notes by remember { mutableStateOf(listOf<NoteEntity>()) }
    var searchQuery by remember { mutableStateOf("") }
    
    // Editing state
    var isEditing by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<NoteEntity?>(null) }
    var noteTitle by remember { mutableStateOf("") }
    var noteContent by remember { mutableStateOf("") }
    
    // Load notes on composition and when changed
    LaunchedEffect(Unit) {
        noteDao.getAllNotes().collect { noteList ->
            notes = if (searchQuery.isEmpty()) {
                noteList
            } else {
                noteList.filter { 
                    it.title.contains(searchQuery, ignoreCase = true) || 
                    it.content.contains(searchQuery, ignoreCase = true)
                }
            }
        }
    }
    
    // Save or update note
    fun saveNote() {
        if (noteTitle.isBlank() && noteContent.isBlank()) return
        
        scope.launch {
            if (editingNote != null) {
                // Update existing note
                val updatedNote = editingNote!!.copy(
                    title = noteTitle.ifBlank { "Untitled" },
                    content = noteContent,
                    updatedAt = System.currentTimeMillis()
                )
                noteDao.updateNote(updatedNote)
            } else {
                // Create new note
                val newNote = NoteEntity(
                    title = noteTitle.ifBlank { "Untitled" },
                    content = noteContent,
                    createdAt = System.currentTimeMillis(),
                    updatedAt = System.currentTimeMillis()
                )
                noteDao.insertNote(newNote)
            }
            
            // Reset editing state
            isEditing = false
            editingNote = null
            noteTitle = ""
            noteContent = ""
        }
    }
    
    // Delete note
    fun deleteNote(note: NoteEntity) {
        scope.launch {
            noteDao.deleteNote(note)
        }
    }
    
    // Toggle pin status
    fun togglePin(note: NoteEntity) {
        scope.launch {
            val updatedNote = note.copy(isPinned = !note.isPinned, updatedAt = System.currentTimeMillis())
            noteDao.updateNote(updatedNote)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        ToolHeader(
            title = "Quick Notes",
            subtitle = "Create and manage your notes",
            onBack = { navHostController.popBackStack() }
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Search bar
        SearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = "Search notes..."
        )
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Add note button
        Button(
            onClick = {
                editingNote = null
                noteTitle = ""
                noteContent = ""
                isEditing = true
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            shape = RoundedCornerShape(14.dp)
        ) {
            Icon(Icons.Default.Add, null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("New Note")
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        if (isEditing) {
            // Edit/Create card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLow
                ),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = if (editingNote != null) "Edit Note" else "New Note",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        
                        Row {
                            TextButton(onClick = { 
                                isEditing = false
                                editingNote = null
                                noteTitle = ""
                                noteContent = ""
                            }) {
                                Text("Cancel")
                            }
                            
                            Button(
                                onClick = { saveNote() },
                                enabled = noteTitle.isNotBlank() || noteContent.isNotBlank()
                            ) {
                                Text("Save")
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = noteTitle,
                        onValueChange = { noteTitle = it },
                        label = { Text("Title") },
                        singleLine = true,
                        leadingIcon = { Icon(Icons.Default.Title, null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    OutlinedTextField(
                        value = noteContent,
                        onValueChange = { noteContent = it },
                        label = { Text("Content") },
                        singleLine = false,
                        minLines = 4,
                        maxLines = 8,
                        leadingIcon = { Icon(Icons.Default.Description, null) },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    )
                    
                    // Auto-save indicator
                    if (noteTitle.isNotBlank() || noteContent.isNotBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Auto-saves when you tap Save",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
        }
        
        // Notes count
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${notes.size} note${if (notes.size != 1) "s" else ""}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.weight(1f)
            )
            
            if (searchQuery.isNotEmpty()) {
                TextButton(onClick = { searchQuery = "" }) {
                    Text("Clear Search")
                }
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // Notes list
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Note,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.3f)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (searchQuery.isNotEmpty()) "No matching notes" else "No notes yet",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (searchQuery.isNotEmpty()) "Try a different search term"
                        else "Tap 'New Note' to create one",
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
                items(notes, key = { it.id }) { note ->
                    NoteCard(
                        note = note,
                        onClick = {
                            editingNote = note
                            noteTitle = note.title
                            noteContent = note.content
                            isEditing = true
                        },
                        onDelete = { deleteNote(note) },
                        onTogglePin = { togglePin(note) }
                    )
                }
            }
        }
    }
}

@Composable
private fun NoteCard(
    note: NoteEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = if (note.isPinned) 
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
                // Pin icon for pinned notes
                if (note.isPinned) {
                    Icon(
                        imageVector = Icons.Default.Star,
                        contentDescription = "Pinned",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                
                // Title
                Text(
                    text = note.title.ifEmpty { "Untitled" },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                
                // Menu button
                Box {
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            Icons.Default.MoreVert,
                            contentDescription = "Options",
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            onClick = {
                                onTogglePin()
                                showMenu = false
                            },
                            text = { 
                                Row {
                                    Icon(
                                        if (note.isPinned) Icons.Default.Star else Icons.Outlined.Star,
                                        null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(if (note.isPinned) "Unpin" else "Pin")
                                }
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onClick()
                                showMenu = false
                            },
                            text = { 
                                Row {
                                    Icon(Icons.Default.Edit, null, modifier = Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("Edit")
                                }
                            }
                        )
                        DropdownMenuItem(
                            onClick = {
                                onDelete()
                                showMenu = false
                            },
                            text = { 
                                Row {
                                    Icon(
                                        Icons.Default.Delete, 
                                        null, 
                                        modifier = Modifier.size(18.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Delete", color = MaterialTheme.colorScheme.error)
                                }
                            }
                        )
                    }
                }
            }
            
            // Content preview
            if (note.content.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = note.content,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            Spacer(modifier = Modifier.height(10.dp))
            
            // Metadata row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Timestamps
                Icon(
                    Icons.Default.Schedule,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                
                Text(
                    text = formatTimestamp(note.updatedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (note.createdAt != note.updatedAt) {
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "edited",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
                
                Spacer(modifier = Modifier.weight(1f))
                
                // Character count
                Text(
                    text = "${note.content.length} chars",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                )
            }
        }
    }
}

private fun formatTimestamp(timestamp: Long): String {
    val now = System.currentTimeMillis()
    val diff = now - timestamp
    
    return when {
        diff < 60_000 -> "Just now"
        diff < 3600_000 -> "${diff / 60_000}m ago"
        diff < 86400_000 -> "${diff / 3600_000}h ago"
        diff < 604800_000 -> "${diff / 86400_000}d ago"
        else -> {
            val cal = java.util.Calendar.getInstance()
            cal.timeInMillis = timestamp
            "${cal.get(java.util.Calendar.MONTH) + 1}/${cal.get(java.util.Calendar.DAY_OF_MONTH)}"
        }
    }
}
