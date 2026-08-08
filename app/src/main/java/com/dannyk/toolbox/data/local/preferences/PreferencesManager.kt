package com.dannyk.toolbox.data.local.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import androidx.compose.foundation.lazy.items

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class PreferencesManager(context: Context) {
    
    private val dataStore = context.dataStore
    
    companion object {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val FAVORITES_KEY = stringPreferencesKey("favorites")
        val RECENT_TOOLS_KEY = stringPreferencesKey("recent_tools")
        val HABITS_KEY = stringPreferencesKey("habits_data")
        val DAILY_RECORDS_KEY = stringPreferencesKey("daily_records_data")
        
        const val THEME_SYSTEM = "system"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
    }
    
    // Theme
    val themeMode: Flow<String> = dataStore.data.map { preferences ->
        preferences[THEME_MODE] ?: THEME_SYSTEM
    }
    
    suspend fun setThemeMode(mode: String) {
        dataStore.edit { preferences ->
            preferences[THEME_MODE] = mode
        }
    }
    
    // Favorites (stored as comma-separated IDs)
    val favoriteIds: Flow<Set<Int>> = dataStore.data.map { preferences ->
        val favString = preferences[FAVORITES_KEY] ?: ""
        if (favString.isEmpty()) emptySet()
        else favString.split(",").mapNotNull { it.trim().toIntOrNull() }.toSet()
    }
    
    suspend fun addFavorite(toolId: Int) {
        dataStore.edit { preferences ->
            val current = preferences[FAVORITES_KEY] ?: ""
            val ids = if (current.isEmpty()) mutableListOf() 
                       else current.split(",").map { it.trim() }.toMutableList()
            if (toolId.toString() !in ids) {
                ids.add(toolId.toString())
                preferences[FAVORITES_KEY] = ids.joinToString(",")
            }
        }
    }
    
    suspend fun removeFavorite(toolId: Int) {
        dataStore.edit { preferences ->
            val current = preferences[FAVORITES_KEY] ?: ""
            val ids = current.split(",").filter { it.trim() != toolId.toString() }
            preferences[FAVORITES_KEY] = ids.joinToString(",")
        }
    }
    
    suspend fun isFavorite(toolId: Int): Boolean {
        return dataStore.data.map { preferences ->
            val current = preferences[FAVORITES_KEY] ?: ""
            toolId.toString() in current.split(",").map { it.trim() }
        }.first()
    }
    
    // Recent Tools (stored as comma-separated ID:timestamp pairs)
    val recentTools: Flow<List<Pair<Int, Long>>> = dataStore.data.map { preferences ->
        val recentString = preferences[RECENT_TOOLS_KEY] ?: ""
        if (recentString.isEmpty()) emptyList()
        else recentString.split("|").mapNotNull { entry ->
            val parts = entry.split(":")
            if (parts.size == 2) {
                val id = parts[0].trim().toIntOrNull()
                val timestamp = parts[1].trim().toLongOrNull()
                if (id != null && timestamp != null) id to timestamp else null
            } else null
        }
    }
    
    suspend fun addToRecent(toolId: Int, maxItems: Int = 20) {
        dataStore.edit { preferences ->
            val current = preferences[RECENT_TOOLS_KEY] ?: ""
            val entries = if (current.isEmpty()) mutableListOf()
                           else current.split("|").toMutableList()
            
            // Remove existing entry for this tool
            entries.removeAll { it.startsWith("$toolId:") }
            
            // Add new entry at beginning
            entries.add(0, "$toolId:${System.currentTimeMillis()}")
            
            // Trim to max items
            if (entries.size > maxItems) {
                preferences[RECENT_TOOLS_KEY] = entries.take(maxItems).joinToString("|")
            } else {
                preferences[RECENT_TOOLS_KEY] = entries.joinToString("|")
            }
        }
    }
    
    suspend fun clearRecentHistory() {
        dataStore.edit { preferences ->
            preferences[RECENT_TOOLS_KEY] = ""
        }
    }
    
    // Habits (stored as JSON-like string)
    suspend fun saveHabits(habitsJson: String) {
        dataStore.edit { preferences ->
            preferences[HABITS_KEY] = habitsJson
        }
    }
    
    fun getHabitsData(): Flow<String> = dataStore.data.map { preferences ->
        preferences[HABITS_KEY] ?: ""
    }
    
    suspend fun saveDailyRecords(recordsJson: String) {
        dataStore.edit { preferences ->
            preferences[DAILY_RECORDS_KEY] = recordsJson
        }
    }
    
    fun getDailyRecordsData(): Flow<String> = dataStore.data.map { preferences ->
        preferences[DAILY_RECORDS_KEY] ?: ""
    }
    
    // Convenience methods for HabitCounterScreen (using Gson serialization)
    private val gson = Gson()
    
    suspend fun <T> saveList(key: Preferences.Key<String>, items: List<T>) {
        val json = gson.toJson(items)
        dataStore.edit { preferences ->
            preferences[key] = json
        }
    }
    
    suspend fun <T> getList(key: Preferences.Key<String>, typeToken: TypeToken<List<T>>): List<T> {
        val json = dataStore.data.map { preferences -> 
            preferences[key] ?: "" 
        }.first()
        return if (json.isEmpty()) emptyList() else try {
            gson.fromJson(json, typeToken.type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    suspend fun <T> saveMap(key: Preferences.Key<String>, map: Map<String, T>) {
        val json = gson.toJson(map)
        dataStore.edit { preferences ->
            preferences[key] = json
        }
    }
    
    suspend fun <T> getMap(key: Preferences.Key<String>, typeToken: TypeToken<Map<String, T>>): Map<String, T> {
        val json = dataStore.data.map { preferences -> 
            preferences[key] ?: "" 
        }.first()
        return if (json.isEmpty()) emptyMap() else try {
            gson.fromJson(json, typeToken.type) ?: emptyMap()
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
