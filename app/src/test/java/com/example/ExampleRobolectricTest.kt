package com.example

import android.content.Context
import android.content.res.Configuration
import android.text.InputType
import android.view.View
import android.view.inputmethod.EditorInfo
import androidx.test.core.app.ApplicationProvider
import com.example.keyboard.AetherKeyboardService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ServiceController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `verify app name resource`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("AetherKey", appName)
    }

    @Test
    fun `test full AetherKeyboardService IME lifecycle`() {
        // 1. Instantiate and onCreate
        val controller: ServiceController<AetherKeyboardService> =
            Robolectric.buildService(AetherKeyboardService::class.java)
        val service = controller.create().get()
        assertNotNull(service)

        // 2. Create input view
        val inputView: View = service.onCreateInputView()
        assertNotNull(inputView)

        // 3. Start Input on a normal text field
        val editorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_NORMAL
            imeOptions = EditorInfo.IME_ACTION_DONE
            actionId = EditorInfo.IME_ACTION_DONE
        }
        service.onStartInput(editorInfo, false)
        service.onStartInputView(editorInfo, false)
        service.onWindowShown()

        // 4. Configuration changes
        val config = Configuration().apply {
            orientation = Configuration.ORIENTATION_LANDSCAPE
        }
        service.onConfigurationChanged(config)

        // 5. Hide keyboard
        service.onWindowHidden()
        service.onFinishInputView(false)
        service.onFinishInput()

        // 6. Switch to another app / text field and reopen
        val emailEditorInfo = EditorInfo().apply {
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
            imeOptions = EditorInfo.IME_ACTION_GO
        }
        service.onStartInput(emailEditorInfo, false)
        service.onStartInputView(emailEditorInfo, false)
        service.onWindowShown()

        // 7. Destroy service
        controller.destroy()
        assertTrue(true)
    }

    @Test
    fun `test null and invalid editor info handling`() {
        val controller = Robolectric.buildService(AetherKeyboardService::class.java)
        val service = controller.create().get()

        // Null EditorInfo should be handled without any exception
        service.onStartInput(null, false)
        service.onStartInputView(null, false)
        service.onFinishInputView(true)

        // Invalid EditorInfo with zeroed fields
        val invalidInfo = EditorInfo()
        service.onStartInput(invalidInfo, true)
        service.onStartInputView(invalidInfo, true)

        controller.destroy()
        assertTrue(true)
    }
}
