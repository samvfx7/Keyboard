package com.example.keyboard

import android.content.res.Configuration
import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
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
import com.example.keyboard.ui.MinimalKeyboardScreen

/**
 * Genuine Android InputMethodService implementation for AetherKey.
 * Adheres strictly to the Android IME lifecycle with robust lifecycle registration,
 * Compose ViewTree owners, and defensive error handling.
 */
class AetherKeyboardService : InputMethodService(),
    LifecycleOwner,
    ViewModelStoreOwner,
    SavedStateRegistryOwner {

    companion object {
        private const val TAG = "AetherKeyboardService"
    }

    private val lifecycleRegistry = LifecycleRegistry(this)
    private val store = ViewModelStore()
    private val savedStateRegistryController = SavedStateRegistryController.create(this)

    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = store
    override val savedStateRegistry: SavedStateRegistry get() = savedStateRegistryController.savedStateRegistry

    override fun onCreate() {
        Log.i(TAG, "onCreate: Initializing AetherKeyboardService")
        super.onCreate()
        try {
            savedStateRegistryController.performAttach()
            savedStateRegistryController.performRestore(null)
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            Log.d(TAG, "onCreate: Lifecycle initialized successfully to ON_CREATE")
        } catch (e: Exception) {
            Log.e(TAG, "onCreate: Exception during lifecycle initialization", e)
        }
    }

    override fun onCreateInputView(): View {
        Log.i(TAG, "onCreateInputView: Creating Compose input view")
        try {
            if (lifecycleRegistry.currentState < Lifecycle.State.CREATED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_CREATE)
            }
            if (lifecycleRegistry.currentState < Lifecycle.State.STARTED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }
            if (lifecycleRegistry.currentState < Lifecycle.State.RESUMED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onCreateInputView: Error setting lifecycle state", e)
        }

        val composeView = ComposeView(this).apply {
            // Keep composition alive across keyboard show/hide cycles
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setViewTreeLifecycleOwner(this@AetherKeyboardService)
            setViewTreeViewModelStoreOwner(this@AetherKeyboardService)
            setViewTreeSavedStateRegistryOwner(this@AetherKeyboardService)
        }

        composeView.setContent {
            MinimalKeyboardScreen(
                onKeyChar = { char -> handleKeyChar(char) },
                onBackspace = { handleBackspace() },
                onSpace = { handleSpace() },
                onEnter = { handleEnter() }
            )
        }
        return composeView
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        Log.d(TAG, "onStartInput: inputType=${attribute?.inputType}, restarting=$restarting")
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        Log.d(TAG, "onStartInputView: inputType=${info?.inputType}, imeOptions=${info?.imeOptions}, restarting=$restarting")
    }

    override fun onFinishInputView(finishingInput: Boolean) {
        super.onFinishInputView(finishingInput)
        Log.d(TAG, "onFinishInputView: finishingInput=$finishingInput")
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Log.d(TAG, "onFinishInput")
    }

    override fun onWindowShown() {
        super.onWindowShown()
        Log.d(TAG, "onWindowShown: Keyboard window is now visible")
        try {
            if (lifecycleRegistry.currentState < Lifecycle.State.STARTED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_START)
            }
            if (lifecycleRegistry.currentState < Lifecycle.State.RESUMED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onWindowShown: Error updating lifecycle", e)
        }
    }

    override fun onWindowHidden() {
        super.onWindowHidden()
        Log.d(TAG, "onWindowHidden: Keyboard window is hidden")
        try {
            if (lifecycleRegistry.currentState >= Lifecycle.State.RESUMED) {
                lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_PAUSE)
            }
        } catch (e: Exception) {
            Log.e(TAG, "onWindowHidden: Error updating lifecycle", e)
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.d(TAG, "onConfigurationChanged: orientation=${newConfig.orientation}")
    }

    private fun handleKeyChar(char: Char) {
        try {
            val ic = currentInputConnection
            if (ic == null) {
                Log.w(TAG, "handleKeyChar: currentInputConnection is null, cannot commit '$char'")
                return
            }
            Log.d(TAG, "handleKeyChar: Committing character '$char'")
            ic.commitText(char.toString(), 1)
        } catch (e: Exception) {
            Log.e(TAG, "handleKeyChar: Exception committing '$char'", e)
        }
    }

    private fun handleBackspace() {
        try {
            val ic = currentInputConnection
            if (ic == null) {
                Log.w(TAG, "handleBackspace: currentInputConnection is null")
                return
            }
            Log.d(TAG, "handleBackspace: Deleting character or active selection")
            val selectedText = ic.getSelectedText(0)
            if (!selectedText.isNullOrEmpty()) {
                ic.commitText("", 1)
            } else {
                val deleted = ic.deleteSurroundingText(1, 0)
                if (!deleted) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleBackspace: Exception during backspace", e)
            try {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_DEL)
            } catch (ignored: Exception) {}
        }
    }

    private fun handleSpace() {
        try {
            val ic = currentInputConnection
            if (ic == null) {
                Log.w(TAG, "handleSpace: currentInputConnection is null")
                return
            }
            Log.d(TAG, "handleSpace: Committing space")
            ic.commitText(" ", 1)
        } catch (e: Exception) {
            Log.e(TAG, "handleSpace: Exception committing space", e)
        }
    }

    private fun handleEnter() {
        try {
            val ic = currentInputConnection
            if (ic == null) {
                Log.w(TAG, "handleEnter: currentInputConnection is null")
                return
            }
            val info = currentInputEditorInfo
            val action = info?.imeOptions?.and(EditorInfo.IME_MASK_ACTION) ?: EditorInfo.IME_ACTION_NONE
            Log.d(TAG, "handleEnter: editor action=$action, actionId=${info?.actionId}")

            if (action != EditorInfo.IME_ACTION_NONE && action != EditorInfo.IME_ACTION_UNSPECIFIED) {
                val handled = ic.performEditorAction(action)
                if (!handled) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                }
            } else if (info != null && info.actionId != 0) {
                val handled = ic.performEditorAction(info.actionId)
                if (!handled) {
                    sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
                }
            } else {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            }
        } catch (e: Exception) {
            Log.e(TAG, "handleEnter: Exception sending enter", e)
            try {
                sendDownUpKeyEvents(KeyEvent.KEYCODE_ENTER)
            } catch (ignored: Exception) {}
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(TAG, "onDestroy: Destroying AetherKeyboardService")
        try {
            lifecycleRegistry.handleLifecycleEvent(Lifecycle.Event.ON_DESTROY)
            store.clear()
        } catch (e: Exception) {
            Log.e(TAG, "onDestroy: Exception in destroy lifecycle", e)
        }
    }
}
