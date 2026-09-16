package com.example.antigravity.ui

import androidx.lifecycle.ViewModel
import com.example.antigravity.model.Conversation
import com.example.antigravity.model.ProjectWorkspace
import com.example.antigravity.ui.navigation.AntigravityAppScreen
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/**
 * ViewModel for AntigravityMainScreen.
 * Holds UI state that should survive configuration changes (rotation, multi-window).
 */
class AntigravityMainViewModel : ViewModel() {

    // Navigation state
    private val _currentScreen = MutableStateFlow(AntigravityAppScreen.CHAT)
    val currentScreen: StateFlow<AntigravityAppScreen> = _currentScreen.asStateFlow()

    private val _showLandingScreen = MutableStateFlow(true)
    val showLandingScreen: StateFlow<Boolean> = _showLandingScreen.asStateFlow()

    // Dialog visibility states
    data class DialogStates(
        val showSettings: Boolean = false,
        val showApiKeyCsv: Boolean = false,
        val showScheduledTasks: Boolean = false,
        val showModelSelection: Boolean = false,
        val showDiagnostics: Boolean = false,
        val showAbout: Boolean = false,
        val showChatPersona: Boolean = false,
        val showChatPrompt: Boolean = false,
        val showAddWorkspace: Boolean = false
    )

    private val _dialogs = MutableStateFlow(DialogStates())
    val dialogs: StateFlow<DialogStates> = _dialogs.asStateFlow()

    // Workspace creation form state
    data class WorkspaceFormState(
        val name: String = "",
        val path: String = "",
        val branch: String = "main",
        val githubOwner: String = "",
        val githubRepo: String = "",
        val githubUrl: String = "",
        val showDiscoveredRepos: Boolean = false,
        val folderInput: String = "",
        val isAddingFolderMode: Boolean = false
    )

    private val _wsForm = MutableStateFlow(WorkspaceFormState())
    val wsForm: StateFlow<WorkspaceFormState> = _wsForm.asStateFlow()

    // Chat input
    private val _inputText = MutableStateFlow("")
    val inputText: StateFlow<String> = _inputText.asStateFlow()

    // Error feedback
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun navigateTo(screen: AntigravityAppScreen) {
        _currentScreen.value = screen
    }

    fun setShowLandingScreen(show: Boolean) {
        _showLandingScreen.value = show
    }

    fun updateDialogs(update: (DialogStates) -> DialogStates) {
        _dialogs.update(update)
    }

    fun updateWsForm(update: (WorkspaceFormState) -> WorkspaceFormState) {
        _wsForm.update(update)
    }

    fun setInputText(text: String) {
        _inputText.value = text
    }

    fun showError(message: String) {
        _errorMessage.value = message
    }

    fun clearError() {
        _errorMessage.value = null
    }
}
