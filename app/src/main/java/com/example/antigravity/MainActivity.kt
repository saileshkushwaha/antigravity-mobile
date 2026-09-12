package com.example.antigravity

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.fragment.app.FragmentActivity
import com.example.antigravity.data.AppRepository
import com.example.antigravity.engine.AntigravityAgentEngine
import com.example.antigravity.theme.AntigravityColors
import com.example.antigravity.theme.AntigravityTheme
import com.example.antigravity.ui.AntigravityMainScreen

class MainActivity : FragmentActivity() {

    private val repository by lazy { AppRepository() }
    private var isAppLocked by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        
        repository.init(applicationContext)

        if (repository.settings.value.biometricLockEnabled) {
            isAppLocked = true
        }

        setContent {
            val scope = rememberCoroutineScope()
            val agentEngine = remember { AntigravityAgentEngine(repository, scope) }

            AntigravityTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeDrawingPadding(),
                    color = AntigravityColors.BackgroundDark
                ) {
                    AntigravityMainScreen(
                        repository = repository,
                        agentEngine = agentEngine,
                        fragmentActivity = this@MainActivity,
                        isBiometricLocked = isAppLocked,
                        onBiometricUnlock = { isAppLocked = false }
                    )
                }
            }
        }
    }

    override fun onRestart() {
        super.onRestart()
        val settings = repository.settings.value
        if (settings.biometricLockEnabled && settings.requireBiometricOnResume) {
            isAppLocked = true
        }
    }
}
