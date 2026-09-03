package com.example.keyboard.engine

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import com.example.data.db.AetherDao
import com.example.data.db.ClipboardEntity
import com.example.data.db.SnippetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class ClipboardManagerEngine(
    private val context: Context,
    private val dao: AetherDao,
    private val scope: CoroutineScope
) {

    private val sysClipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager

    val clipboardHistory: Flow<List<ClipboardEntity>> = dao.getClipboardHistory()
    val snippets: Flow<List<SnippetEntity>> = dao.getAllSnippets()

    init {
        // Setup Primary Clip Listener
        sysClipboardManager?.addPrimaryClipChangedListener {
            captureSystemClip()
        }
    }

    fun captureSystemClip() {
        try {
            val clip = sysClipboardManager?.primaryClip
            if (clip != null && clip.itemCount > 0) {
                val text = clip.getItemAt(0).text?.toString()
                if (!text.isNullOrBlank() && !isSensitiveData(text)) {
                    addClip(text)
                }
            }
        } catch (e: Exception) {
            // Permission or security exception
        }
    }

    fun addClip(text: String) {
        if (text.isBlank()) return
        scope.launch(Dispatchers.IO) {
            dao.insertClipboardItem(ClipboardEntity(text = text.trim()))
        }
    }

    fun deleteClip(id: Long) {
        scope.launch(Dispatchers.IO) {
            dao.deleteClipboardItem(id)
        }
    }

    fun clearAllUnpinned() {
        scope.launch(Dispatchers.IO) {
            dao.clearUnpinnedClipboard()
        }
    }

    fun togglePin(item: ClipboardEntity) {
        scope.launch(Dispatchers.IO) {
            dao.insertClipboardItem(item.copy(isPinned = !item.isPinned))
        }
    }

    suspend fun checkSnippetExpansion(text: String): String? {
        if (!text.startsWith("/")) return null
        return dao.getSnippetExpansion(text.trim())
    }

    fun saveSnippet(shortcut: String, expansion: String) {
        val cleanShortcut = if (shortcut.startsWith("/")) shortcut else "/$shortcut"
        scope.launch(Dispatchers.IO) {
            dao.insertSnippet(SnippetEntity(shortcut = cleanShortcut, expansion = expansion))
        }
    }

    fun deleteSnippet(shortcut: String) {
        scope.launch(Dispatchers.IO) {
            dao.deleteSnippet(shortcut)
        }
    }

    fun copyToSystemClipboard(text: String) {
        try {
            val clip = ClipData.newPlainText("AetherKey Clip", text)
            sysClipboardManager?.setPrimaryClip(clip)
        } catch (e: Exception) {
            //
        }
    }

    /**
     * Filters out sensitive inputs like passwords, OTPs, or credit card numbers.
     */
    private fun isSensitiveData(text: String): Boolean {
        // OTP numeric match
        if (text.matches("^[0-9]{4,8}$".toRegex())) return true
        // Credit card 16 digit pattern
        if (text.replace("\\s+".toRegex(), "").matches("^[0-9]{13,19}$".toRegex())) return true
        return false
    }

    fun runAutoExpiration(expireMinutes: Int) {
        if (expireMinutes <= 0) return
        val cutoff = System.currentTimeMillis() - (expireMinutes * 60 * 1000L)
        scope.launch(Dispatchers.IO) {
            dao.expireOldClipboard(cutoff)
        }
    }
}
