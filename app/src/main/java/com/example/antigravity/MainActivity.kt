package com.example.antigravity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.example.antigravity.data.AppRepository
import com.example.antigravity.engine.AntigravityAgentEngine
import com.example.antigravity.theme.AntigravityColors
import com.example.antigravity.theme.AntigravityTheme
import com.example.antigravity.ui.AntigravityMainScreen

class MainActivity : ComponentActivity() {

    private val repository by lazy { AppRepository() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

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
                        agentEngine = agentEngine
                    )
                }
            }
        }
    }
}
