package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.data.db.AetherDatabase
import com.example.data.preferences.KeyboardPreferences
import com.example.ui.settings.SettingsScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = KeyboardPreferences(this)
        val database = AetherDatabase.getInstance(this)

        setContent {
            MyApplicationTheme {
                SettingsScreen(prefs = prefs, database = database)
            }
        }
    }
}
