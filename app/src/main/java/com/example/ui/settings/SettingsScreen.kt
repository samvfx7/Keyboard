package com.example.ui.settings

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.AetherDatabase
import com.example.data.preferences.KeyboardPreferences
import com.example.data.preferences.KeyboardTheme
import com.example.keyboard.engine.AdaptiveHitboxEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    prefs: KeyboardPreferences,
    database: AetherDatabase
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val currentTheme by prefs.theme.collectAsState()
    val heightFactor by prefs.heightFactor.collectAsState()
    val squircleRadius by prefs.squircleRadiusDp.collectAsState()
    val keySpacing by prefs.keySpacingDp.collectAsState()
    val hapticStrength by prefs.hapticStrength.collectAsState()
    val soundEnabled by prefs.soundEnabled.collectAsState()
    val showNumberRow by prefs.showNumberRow.collectAsState()
    val showEmojiRow by prefs.showEmojiRow.collectAsState()

    val autocorrectEnabled by prefs.autocorrectEnabled.collectAsState()
    val autocorrectSensitivity by prefs.autocorrectSensitivity.collectAsState()
    val nextWordPrediction by prefs.nextWordPrediction.collectAsState()
    val smartLocalModel by prefs.smartLocalModel.collectAsState()
    val swipeTypingEnabled by prefs.swipeTypingEnabled.collectAsState()
    val personalLearningEnabled by prefs.personalLearningEnabled.collectAsState()

    val currentLanguage by prefs.currentLanguage.collectAsState()
    val mixedLanguageMode by prefs.mixedLanguageMode.collectAsState()

    val clipboardHistoryEnabled by prefs.clipboardHistoryEnabled.collectAsState()
    val clipboardAutoExpireMinutes by prefs.clipboardAutoExpireMinutes.collectAsState()

    val learnedWordsCount by database.aetherDao().getLearnedWordsCount().collectAsState(initial = 0)
    val learnedPhrasesCount by database.aetherDao().getLearnedPhrasesCount().collectAsState(initial = 0)

    var showClearDataDialog by remember { mutableStateOf(false) }
    var sandboxText by remember { mutableStateOf("") }

    // Check IME Activation status
    val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
    val isEnabled = imm?.enabledInputMethodList?.any { it.packageName == context.packageName } ?: false

    val darkBg = Color(0xFF000000)
    val cardBg = Color(0xFF141417)
    val cardBorder = Color(0x6627272A)
    val accent = Color(0xFF3B82F6)
    val emerald = Color(0xFF10B981)
    val textWhite = Color(0xFFFAFAFA)
    val textMuted = Color(0xFF71717A)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Sentinel AI", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                            Text("v2.4.0 • OFFLINE ONLY", color = textMuted, fontFamily = FontFamily.Monospace, fontSize = 11.sp)
                        }

                        // Pulse Security Badge
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0x1A10B981))
                                .border(1.dp, Color(0x3310B981), RoundedCornerShape(8.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(emerald)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("SECURE", color = emerald, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = darkBg)
            )
        },
        containerColor = darkBg
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // BENTO GRID DASHBOARD
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                // Bento Hero Processing Tile
                Card(
                    colors = CardDefaults.cardColors(containerColor = cardBg),
                    border = BorderStroke(1.dp, cardBorder),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("LIVE PROCESSING", color = textMuted, fontWeight = FontWeight.Bold, fontSize = 11.sp, letterSpacing = 1.sp)
                            Icon(Icons.Default.FlashOn, contentDescription = null, tint = textMuted, modifier = Modifier.size(16.dp))
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text("“The future of typing is local and private.”", color = textWhite, fontWeight = FontWeight.Medium, fontSize = 16.sp)

                        Spacer(modifier = Modifier.height(16.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF27272A))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(0.65f)
                                    .clip(CircleShape)
                                    .background(accent)
                            )
                        }
                    }
                }

                // 2x2 Bento Stat Tiles
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("0", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("DATA LEAKS", color = textMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("12ms", color = accent, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("LATENCY", color = textMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("$learnedWordsCount", color = emerald, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("LOCAL VOCAB", color = textMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp)
                        }
                    }

                    Card(
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        border = BorderStroke(1.dp, cardBorder),
                        shape = RoundedCornerShape(20.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("$learnedPhrasesCount", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 24.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text("PHRASE CONTEXTS", color = textMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 0.5.sp)
                        }
                    }
                }

                // Bento Hitbox Footer Label
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = textMuted, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("ADAPTIVE HITBOXES ACTIVE", color = textMuted, fontWeight = FontWeight.Bold, fontSize = 10.sp, letterSpacing = 1.sp)
                }
            }

            // Setup & Activation Wizard Bento Card
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, if (isEnabled) Color(0x4010B981) else cardBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isEnabled) Icons.Default.CheckCircle else Icons.Default.Info,
                            contentDescription = null,
                            tint = if (isEnabled) emerald else accent,
                            modifier = Modifier.size(22.dp)
                        )
                        Text(
                            text = if (isEnabled) "AetherKey System IME Active" else "System Activation Required",
                            color = textWhite,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = if (isEnabled) "AetherKey is active as your primary offline keyboard."
                        else "1. Enable AetherKey in Android Keyboard Settings.\n2. Select AetherKey as your current Input Method.",
                        color = textMuted,
                        fontSize = 13.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                context.startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = accent),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("1. Enable Keyboard", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                imm?.showInputMethodPicker()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF27272A)),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("2. Switch Keyboard", color = textWhite, fontSize = 12.sp)
                        }
                    }
                }
            }

            // Interactive Playground Sandbox Field
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Keyboard Sandbox Test", color = accent, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text("Tap below to test AetherKey and observe live predictions & haptics:", color = textMuted, fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = sandboxText,
                        onValueChange = { sandboxText = it },
                        placeholder = { Text("Type here to test keyboard...", color = textMuted) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = accent,
                            unfocusedBorderColor = Color(0xFF27272A),
                            focusedTextColor = textWhite,
                            unfocusedTextColor = textWhite,
                            focusedContainerColor = Color(0xFF09090B),
                            unfocusedContainerColor = Color(0xFF09090B)
                        )
                    )
                }
            }

            // SECTION 1: KEYBOARD DESIGN & THEMES
            SettingsSectionHeader("Keyboard Appearance & Layout", Icons.Default.Keyboard, accent)

            // Theme Selector
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Color Theme", color = textWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        KeyboardTheme.entries.forEach { th ->
                            val isSelected = th == currentTheme
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color(android.graphics.Color.parseColor(th.bgHex)))
                                    .border(
                                        width = if (isSelected) 3.dp else 1.dp,
                                        color = if (isSelected) accent else Color(0xFF27272A),
                                        shape = RoundedCornerShape(12.dp)
                                    )
                                    .clickable { prefs.setTheme(th) },
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(Color(android.graphics.Color.parseColor(th.accentHex)), CircleShape)
                                )
                            }
                        }
                    }
                    Text(
                        text = "Active: ${currentTheme.displayName}",
                        color = textMuted,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // Keyboard Height Slider
            SettingsSliderTile(
                title = "Keyboard Height",
                value = heightFactor,
                range = 0.8f..1.3f,
                valueText = "${(heightFactor * 100).toInt()}%",
                onValueChange = { prefs.setHeightFactor(it) },
                cardBg = cardBg,
                cardBorder = cardBorder,
                accent = accent,
                textWhite = textWhite,
                textMuted = textMuted
            )

            // Squircle Radius Slider
            SettingsSliderTile(
                title = "Squircle Key Corner Radius",
                value = squircleRadius.toFloat(),
                range = 4f..20f,
                valueText = "${squircleRadius}dp",
                onValueChange = { prefs.setSquircleRadius(it.toInt()) },
                cardBg = cardBg,
                cardBorder = cardBorder,
                accent = accent,
                textWhite = textWhite,
                textMuted = textMuted
            )

            // Haptic Strength Slider
            SettingsSliderTile(
                title = "Haptic Vibration Strength",
                value = hapticStrength,
                range = 0f..1f,
                valueText = if (hapticStrength == 0f) "Off" else "${(hapticStrength * 100).toInt()}%",
                onValueChange = { prefs.setHapticStrength(it) },
                cardBg = cardBg,
                cardBorder = cardBorder,
                accent = accent,
                textWhite = textWhite,
                textMuted = textMuted
            )

            // Toggles
            SettingsSwitchTile("Key Sound Effects", soundEnabled, cardBg, cardBorder, textWhite, accent) { prefs.setSoundEnabled(it) }
            SettingsSwitchTile("Number Row Always Visible", showNumberRow, cardBg, cardBorder, textWhite, accent) { prefs.setShowNumberRow(it) }

            // SECTION 2: INTELLIGENCE & LOCAL AI
            SettingsSectionHeader("Intelligence & Local AI", Icons.Default.Psychology, accent)

            SettingsSwitchTile("Smart Autocorrect", autocorrectEnabled, cardBg, cardBorder, textWhite, accent) { prefs.setAutocorrectEnabled(it) }
            if (autocorrectEnabled) {
                SettingsSliderTile(
                    title = "Autocorrect Sensitivity",
                    value = autocorrectSensitivity,
                    range = 0.3f..0.9f,
                    valueText = "${(autocorrectSensitivity * 100).toInt()}%",
                    onValueChange = { prefs.setAutocorrectSensitivity(it) },
                    cardBg = cardBg,
                    cardBorder = cardBorder,
                    accent = accent,
                    textWhite = textWhite,
                    textMuted = textMuted
                )
            }

            SettingsSwitchTile("Next-Word Predictions", nextWordPrediction, cardBg, cardBorder, textWhite, accent) { prefs.setNextWordPrediction(it) }
            SettingsSwitchTile("Smart Local Context Engine", smartLocalModel, cardBg, cardBorder, textWhite, accent) { prefs.setSmartLocalModel(it) }
            SettingsSwitchTile("Swipe / Glide Typing", swipeTypingEnabled, cardBg, cardBorder, textWhite, accent) { prefs.setSwipeTypingEnabled(it) }
            SettingsSwitchTile("Personal Vocabulary Learning", personalLearningEnabled, cardBg, cardBorder, textWhite, accent) { prefs.setPersonalLearningEnabled(it) }

            // SECTION 3: LANGUAGES
            SettingsSectionHeader("Languages & Dictionaries", Icons.Default.Language, accent)

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, cardBorder),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Primary Language", color = textWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        listOf("en" to "English", "es" to "Spanish", "fr" to "French", "de" to "German").forEach { (code, name) ->
                            val isSelected = code == currentLanguage
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSelected) accent else Color(0xFF27272A))
                                    .clickable { prefs.setCurrentLanguage(code) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = name,
                                    color = if (isSelected) Color.White else textWhite,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                        }
                    }
                }
            }

            SettingsSwitchTile("Seamless Mixed-Language Mode", mixedLanguageMode, cardBg, cardBorder, textWhite, accent) { prefs.setMixedLanguageMode(it) }

            // SECTION 4: CLIPBOARD
            SettingsSectionHeader("Clipboard & Snippets", Icons.Default.ContentPaste, accent)

            SettingsSwitchTile("Clipboard History", clipboardHistoryEnabled, cardBg, cardBorder, textWhite, accent) { prefs.setClipboardHistoryEnabled(it) }

            // SECTION 5: PRIVACY CENTER & DATA INSPECTOR
            SettingsSectionHeader("Privacy Center & Data Inspector", Icons.Default.Lock, accent)

            Card(
                colors = CardDefaults.cardColors(containerColor = cardBg),
                border = BorderStroke(1.dp, Color(0x6610B981)),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Shield, contentDescription = null, tint = emerald)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Strict Offline Privacy Engine", color = textWhite, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "• 100% On-Device Processing\n• Zero Analytics or Remote Connections\n• Zero Network Permissions Requested\n• Automatic Protection in Password Fields",
                        color = textMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Local Personalization Stats:", color = textWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        PrivacyStatTile("Learned Words", "$learnedWordsCount", cardBg, cardBorder, textWhite, emerald, Modifier.weight(1f))
                        PrivacyStatTile("Learned Phrases", "$learnedPhrasesCount", cardBg, cardBorder, textWhite, accent, Modifier.weight(1f))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = { showClearDataDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7F1D1D)),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, tint = Color.White)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Clear All Learned Personalization Data", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Clear Data Confirmation Dialog
    if (showClearDataDialog) {
        AlertDialog(
            onDismissRequest = { showClearDataDialog = false },
            containerColor = cardBg,
            title = { Text("Clear All Learned Data?", color = textWhite, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "This will completely erase all locally learned words, phrase contexts, and adaptive hitbox touch calibration profiles. This action cannot be undone.",
                    color = textMuted
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        scope.launch(Dispatchers.IO) {
                            database.aetherDao().clearLearnedWords()
                            database.aetherDao().clearLearnedPhrases()
                            database.aetherDao().clearTouchOffsets()
                            database.aetherDao().clearCorrections()
                        }
                        AdaptiveHitboxEngine(database.aetherDao(), scope).clearCalibration()
                        showClearDataDialog = false
                    }
                ) {
                    Text("Clear Everything", color = Color(0xFFF87171), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDataDialog = false }) {
                    Text("Cancel", color = textMuted)
                }
            }
        )
    }
}

@Composable
private fun SettingsSectionHeader(title: String, icon: ImageVector, tint: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
    ) {
        Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(title, color = tint, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun SettingsSwitchTile(
    title: String,
    checked: Boolean,
    cardBg: Color,
    cardBorder: Color,
    textWhite: Color,
    accent: Color,
    onCheckedChange: (Boolean) -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(title, color = textWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = accent,
                    uncheckedThumbColor = Color.Gray,
                    uncheckedTrackColor = Color(0xFF27272A)
                )
            )
        }
    }
}

@Composable
private fun SettingsSliderTile(
    title: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    valueText: String,
    onValueChange: (Float) -> Unit,
    cardBg: Color,
    cardBorder: Color,
    accent: Color,
    textWhite: Color,
    textMuted: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = cardBg),
        border = BorderStroke(1.dp, cardBorder),
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(title, color = textWhite, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                Text(valueText, color = accent, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = range,
                colors = SliderDefaults.colors(
                    thumbColor = accent,
                    activeTrackColor = accent,
                    inactiveTrackColor = Color(0xFF27272A)
                )
            )
        }
    }
}

@Composable
private fun PrivacyStatTile(
    label: String,
    count: String,
    cardBg: Color,
    cardBorder: Color,
    textWhite: Color,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(cardBg)
            .border(1.dp, cardBorder, RoundedCornerShape(12.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count, color = accent, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(label, color = textWhite, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}
