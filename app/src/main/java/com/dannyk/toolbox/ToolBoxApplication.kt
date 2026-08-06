package com.dannyk.toolbox

import android.app.Application
import dagger.hilt.android.HiltAndroidApp
import com.dannyk.toolbox.data.local.AppDatabase
import com.dannyk.toolbox.data.local.preferences.PreferencesManager

@HiltAndroidApp
class ToolBoxApplication : Application() {
    
    val database: AppDatabase by lazy { AppDatabase.getDatabase(this) }
    val preferencesManager: PreferencesManager by lazy { PreferencesManager(this) }
    
    override fun onCreate() {
        super.onCreate()
        instance = this
    }
    
    companion object {
        lateinit var instance: ToolBoxApplication
            private set
    }
}
