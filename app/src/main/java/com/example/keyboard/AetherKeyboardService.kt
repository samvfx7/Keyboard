package com.example.keyboard

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.text.InputType
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.ExtractedTextRequest
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.SavedStateRegistry
import androidx.savedstate.SavedStateRegistryController
import androidx.savedstate.SavedStateRegistryOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.MainActivity
import com.example.data.db.AetherDatabase
import com.example.data.preferences.KeyboardPreferences
import com.example.keyboard.engine.AdaptiveHitboxEngine
import com.example.keyboard.engine.AutocorrectEngine
import com.example.keyboard.engine.ClipboardManagerEngine
import com.example.keyboard.engine.EmojiEngine
import com.example.keyboard.engine.KeyPosition
import com.example.keyboard.engine.LanguageEngine
import com.example.keyboard.engine.PredictionEngine
import com.example.keyboard.engine.PredictionResult
import com.example.keyboard.engine.SwipeTypingEngine
import com.example.keyboard.engine.TouchPoint
import com.example.keyboard.ui.KeyboardScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AetherKeyboardService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    private lateinit var database: AetherDatabase
    private lateinit var prefs: KeyboardPreferences
    private lateinit var languageEngine: LanguageEngine
    private lateinit var autocorrectEngine: AutocorrectEngine
    private lateinit var adaptiveHitboxEngine: AdaptiveHitboxEngine
    private lateinit var predictionEngine: PredictionEngine
    private lateinit var swipeTypingEngine: SwipeTypingEngine
    private lateinit var emojiEngine: EmojiEngine
    private lateinit var clipboardEngine: ClipboardManagerEngine

    private var currentWordBuffer by mutableStateOf("")
    private var predictionResult by mutableStateOf(PredictionResult("", emptyList()))
    private var isPasswordMode by mutableStateOf(false)
    private var isNumericMode by mutableStateOf(false)

    private val keyPositionsMap = mutableListOf<KeyPosition>()
    private var lastSpaceTime = 0L

    override fun onCreate() {
        super.onCreate()
        savedStateRegistryController.performRestore(null)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)

        database = AetherDatabase.getInstance(this)
        prefs = KeyboardPreferences(this)
        languageEngine = LanguageEngine()
        autocorrectEngine = AutocorrectEngine(languageEngine)
        adaptiveHitboxEngine = AdaptiveHitboxEngine(database.aetherDao(), serviceScope)
        emojiEngine = EmojiEngine()
        predictionEngine = PredictionEngine(database.aetherDao(), languageEngine, emojiEngine, autocorrectEngine, serviceScope)
        swipeTypingEngine = SwipeTypingEngine(languageEngine)
        clipboardEngine = ClipboardManagerEngine(this, database.aetherDao(), serviceScope)
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)

        val composeView = ComposeView(this)
        composeView.setViewTreeLifecycleOwner(this)
        composeView.setViewTreeViewModelStoreOwner(this)
        composeView.setViewTreeSavedStateRegistryOwner(this)

        composeView.setContent {
            val lang by prefs.currentLanguage.collectAsState()
            val mixedMode by prefs.mixedLanguageMode.collectAsState()

            KeyboardScreen(
                prefs = prefs,
                predictionResult = predictionResult,
                emojiEngine = emojiEngine,
                clipboardEngine = clipboardEngine,
                isPasswordMode = isPasswordMode,
                isNumericMode = isNumericMode,
                onKeyChar = { char, rx, ry -> handleKeyChar(char, rx, ry) },
                onBackspace = { handleBackspace() },
                onSpace = { handleSpace() },
                onEnter = { handleEnter() },
                onSuggestionSelected = { word -> handleSuggestionSelected(word) },
                onEmojiSelected = { emoji -> currentInputConnection?.commitText(emoji, 1) },
                onMoveCursor = { dx, dy -> handleMoveCursor(dx, dy) },
                onSwipePathCompleted = { path -> handleSwipeCompleted(path) },
                onSelectAll = { currentInputConnection?.performContextMenuAction(android.R.id.selectAll) },
                onSelectWord = { handleSelectWord() },
                onCut = { currentInputConnection?.performContextMenuAction(android.R.id.cut) },
                onCopy = { currentInputConnection?.performContextMenuAction(android.R.id.copy) },
                onPaste = { currentInputConnection?.performContextMenuAction(android.R.id.paste) },
                onUndo = { currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Z)) },
                onRedo = { currentInputConnection?.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_Y)) },
                onOpenSettings = {
                    val intent = Intent(this, MainActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                },
                onToggleLanguage = {
                    val nextLang = when (lang) {
                        "en" -> "es"
                        "es" -> "fr"
                        "fr" -> "de"
                        else -> "en"
                    }
                    prefs.setCurrentLanguage(nextLang)
                },
                onReportKeyPosition = { keyPos ->
                    keyPositionsMap.removeAll { it.char.lowercaseChar() == keyPos.char.lowercaseChar() }
                    keyPositionsMap.add(keyPos)
                }
            )
        }
        return composeView
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        if (info == null) return

        val variation = info.inputType and InputType.TYPE_MASK_VARIATION
        val clazz = info.inputType and InputType.TYPE_MASK_CLASS

        // Detect Privacy-Sensitive Password Fields
        isPasswordMode = (variation == InputType.TYPE_TEXT_VARIATION_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD ||
                variation == InputType.TYPE_TEXT_VARIATION_WEB_PASSWORD)

        // Detect Numeric Fields
        isNumericMode = (clazz == InputType.TYPE_CLASS_NUMBER ||
                clazz == InputType.TYPE_CLASS_PHONE ||
                clazz == InputType.TYPE_CLASS_DATETIME)

        currentWordBuffer = ""
        predictionResult = PredictionResult("", emptyList())
        autocorrectEngine.clearUndo()
    }

    private fun handleKeyChar(char: Char, rawX: Float, rawY: Float) {
        val ic = currentInputConnection ?: return

        // 1. Commit character to text field
        ic.commitText(char.toString(), 1)

        // 2. Calibrate adaptive touch hitboxes if key location exists
        val matchingKey = keyPositionsMap.find { it.char.lowercaseChar() == char.lowercaseChar() }
        if (matchingKey != null) {
            adaptiveHitboxEngine.learnTouchOffset(char, rawX, rawY, matchingKey)
        }

        // 3. Update current word buffer & trigger predictions
        currentWordBuffer += char

        // Check text snippet shortcut (e.g. "/email")
        if (currentWordBuffer.startsWith("/")) {
            serviceScope.launch {
                val expansion = clipboardEngine.checkSnippetExpansion(currentWordBuffer)
                if (expansion != null) {
                    ic.deleteSurroundingText(currentWordBuffer.length, 0)
                    ic.commitText(expansion, 1)
                    currentWordBuffer = ""
                }
            }
        }

        updatePredictions()
    }

    private fun handleBackspace() {
        val ic = currentInputConnection ?: return

        // Check if user is undoing an immediate autocorrect
        val currentText = ic.getTextBeforeCursor(30, 0)?.toString() ?: ""
        val reverted = autocorrectEngine.undoLastCorrection(currentText)

        if (reverted != null) {
            ic.deleteSurroundingText(currentText.length, 0)
            ic.commitText(reverted, 1)
            currentWordBuffer = reverted.split("\\s+".toRegex()).lastOrNull() ?: ""
            updatePredictions()
            return
        }

        if (currentWordBuffer.isNotEmpty()) {
            currentWordBuffer = currentWordBuffer.dropLast(1)
        }

        ic.deleteSurroundingText(1, 0)
        updatePredictions()
    }

    private fun handleSpace() {
        val ic = currentInputConnection ?: return
        val now = System.currentTimeMillis()

        // Double Space Period Insertion
        if (now - lastSpaceTime < 350 && currentWordBuffer.isEmpty()) {
            ic.deleteSurroundingText(1, 0)
            ic.commitText(". ", 1)
            lastSpaceTime = 0
            return
        }
        lastSpaceTime = now

        // Check Autocorrect Confidence
        if (!isPasswordMode && prefs.autocorrectEnabled.value && currentWordBuffer.isNotEmpty()) {
            val candidate = predictionResult.mainSuggestion
            val autoCorrection = autocorrectEngine.evaluateAutocorrect(
                typedWord = currentWordBuffer,
                candidateWords = listOf(candidate) + predictionResult.alternativeSuggestions,
                personalLearnedWords = predictionEngine.getPersonalWords(),
                lang = prefs.currentLanguage.value,
                sensitivity = prefs.autocorrectSensitivity.value,
                isPasswordOrCode = isPasswordMode
            )

            if (autoCorrection != null && autoCorrection != currentWordBuffer) {
                ic.deleteSurroundingText(currentWordBuffer.length, 0)
                ic.commitText("$autoCorrection ", 1)

                if (prefs.personalLearningEnabled.value) {
                    predictionEngine.learnInput(autoCorrection, null, prefs.currentLanguage.value)
                }
                currentWordBuffer = ""
                updatePredictions()
                return
            }
        }

        if (currentWordBuffer.isNotEmpty() && prefs.personalLearningEnabled.value) {
            predictionEngine.learnInput(currentWordBuffer, null, prefs.currentLanguage.value)
        }

        ic.commitText(" ", 1)
        currentWordBuffer = ""
        updatePredictions()
    }

    private fun handleEnter() {
        val ic = currentInputConnection ?: return
        val info = currentInputEditorInfo

        if (info != null && info.actionId != 0) {
            ic.performEditorAction(info.actionId)
        } else {
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER))
            ic.sendKeyEvent(KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER))
        }
        currentWordBuffer = ""
    }

    private fun handleSuggestionSelected(word: String) {
        val ic = currentInputConnection ?: return
        if (currentWordBuffer.isNotEmpty()) {
            ic.deleteSurroundingText(currentWordBuffer.length, 0)
        }
        ic.commitText("$word ", 1)

        if (prefs.personalLearningEnabled.value) {
            predictionEngine.learnInput(word, null, prefs.currentLanguage.value)
        }
        currentWordBuffer = ""
        updatePredictions()
    }

    private fun handleSwipeCompleted(path: List<TouchPoint>) {
        if (isPasswordMode) return
        val ic = currentInputConnection ?: return

        val candidates = swipeTypingEngine.decodeSwipePath(
            pathPoints = path,
            keyPositions = keyPositionsMap,
            lang = prefs.currentLanguage.value
        )

        if (candidates.isNotEmpty()) {
            val topWord = candidates.first()
            ic.commitText("$topWord ", 1)
            if (prefs.personalLearningEnabled.value) {
                predictionEngine.learnInput(topWord, null, prefs.currentLanguage.value)
            }
            currentWordBuffer = ""
            updatePredictions()
        }
    }

    private fun handleMoveCursor(dx: Int, dy: Int) {
        val ic = currentInputConnection ?: return
        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0) ?: return
        val currentPos = extracted.selectionStart

        val newPos = (currentPos + dx).coerceIn(0, extracted.text.length)
        ic.setSelection(newPos, newPos)
    }

    private fun handleSelectWord() {
        val ic = currentInputConnection ?: return
        val textBefore = ic.getTextBeforeCursor(50, 0)?.toString() ?: ""
        val textAfter = ic.getTextAfterCursor(50, 0)?.toString() ?: ""

        val wordBefore = textBefore.takeLastWhile { !it.isWhitespace() }
        val wordAfter = textAfter.takeWhile { !it.isWhitespace() }

        val start = textBefore.length - wordBefore.length
        val end = textBefore.length + wordAfter.length

        val extracted = ic.getExtractedText(ExtractedTextRequest(), 0) ?: return
        ic.setSelection(start.coerceAtLeast(0), end.coerceAtMost(extracted.text.length))
    }

    private fun updatePredictions() {
        if (isPasswordMode) {
            predictionResult = PredictionResult("", emptyList())
            return
        }

        val ic = currentInputConnection
        val fullTextBefore = ic?.getTextBeforeCursor(100, 0)?.toString() ?: ""
        val words = fullTextBefore.trim().split("\\s+".toRegex()).dropLast(1)

        serviceScope.launch {
            if (prefs.smartLocalModel.value && fullTextBefore.length > 15) {
                predictionResult = predictionEngine.getSmartPredictions(
                    fullSentenceContext = fullTextBefore,
                    currentWord = currentWordBuffer,
                    previousWords = words,
                    lang = prefs.currentLanguage.value,
                    mixedMode = prefs.mixedLanguageMode.value
                )
            } else {
                predictionResult = predictionEngine.getFastPredictions(
                    currentWord = currentWordBuffer,
                    previousWords = words,
                    lang = prefs.currentLanguage.value,
                    mixedMode = prefs.mixedLanguageMode.value,
                    enableNextWord = prefs.nextWordPrediction.value
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        store.clear()
        serviceScope.cancel()
    }
}
