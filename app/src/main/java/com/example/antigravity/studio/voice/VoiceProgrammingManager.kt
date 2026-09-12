package com.example.antigravity.studio.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import java.util.Locale

enum class VoiceState {
    IDLE,
    LISTENING,
    PROCESSING,
    ERROR
}

data class VoiceCommandResult(
    val rawSpeech: String,
    val parsedAction: String,
    val isShortcut: Boolean
)

class VoiceProgrammingManager(private val context: Context?) {

    var currentState: VoiceState = VoiceState.IDLE
        private set

    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null
    private var isTtsReady = false

    var onStateChanged: ((VoiceState) -> Unit)? = null
    var onSpeechRecognized: ((VoiceCommandResult) -> Unit)? = null
    var onErrorOccurred: ((String) -> Unit)? = null

    init {
        initTts()
    }

    private fun initTts() {
        if (context == null) return
        try {
            textToSpeech = TextToSpeech(context) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    textToSpeech?.language = Locale.US
                    isTtsReady = true
                }
            }
        } catch (e: Throwable) {
            // In headless/unit test environments, TTS initialization may not be available
            isTtsReady = false
        }
    }

    /**
     * Parses spoken developer sentences into quick IDE slash commands or @codebase actions.
     */
    fun parseVoiceCommand(speech: String): VoiceCommandResult {
        val trimmed = speech.trim()
        val lower = trimmed.lowercase(Locale.ROOT)

        return when {
            lower.contains("show git diff") || lower.contains("review diff") || lower.contains("git diff") -> {
                VoiceCommandResult(trimmed, "/diff", true)
            }
            lower.contains("run test") || lower.contains("run unit test") || lower.contains("execute tests") -> {
                VoiceCommandResult(trimmed, "/test", true)
            }
            lower.contains("build app") || lower.contains("assemble debug") || lower.contains("build project") -> {
                VoiceCommandResult(trimmed, "/build", true)
            }
            lower.startsWith("search codebase for ") -> {
                val query = trimmed.substring("search codebase for ".length).trim()
                VoiceCommandResult(trimmed, "@codebase $query", true)
            }
            lower.startsWith("find in codebase ") -> {
                val query = trimmed.substring("find in codebase ".length).trim()
                VoiceCommandResult(trimmed, "@codebase $query", true)
            }
            lower.contains("rollback workspace") || lower.contains("undo swarm") -> {
                VoiceCommandResult(trimmed, "/rollback", true)
            }
            lower.contains("optimize prompt") -> {
                VoiceCommandResult(trimmed, "/optimize", true)
            }
            else -> {
                VoiceCommandResult(trimmed, trimmed, false)
            }
        }
    }

    /**
     * Starts native speech recognition if supported on this device.
     */
    fun startListening() {
        if (context == null) {
            updateState(VoiceState.ERROR)
            onErrorOccurred?.invoke("Speech recognizer unavailable in current context")
            return
        }

        try {
            if (!SpeechRecognizer.isRecognitionAvailable(context)) {
                updateState(VoiceState.ERROR)
                onErrorOccurred?.invoke("Speech recognition is not available on this device")
                return
            }

            stopListening()

            speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        updateState(VoiceState.LISTENING)
                    }

                    override fun onBeginningOfSpeech() {}
                    override fun onRmsChanged(rmsdB: Float) {}
                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        updateState(VoiceState.PROCESSING)
                    }

                    override fun onError(error: Int) {
                        updateState(VoiceState.ERROR)
                        val errorMsg = when (error) {
                            SpeechRecognizer.ERROR_AUDIO -> "Audio recording error"
                            SpeechRecognizer.ERROR_NO_MATCH -> "No speech detected"
                            SpeechRecognizer.ERROR_NETWORK -> "Network connection error"
                            SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required"
                            else -> "Speech recognition error ($error)"
                        }
                        onErrorOccurred?.invoke(errorMsg)
                    }

                    override fun onResults(results: Bundle?) {
                        updateState(VoiceState.IDLE)
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val spokenText = matches?.firstOrNull() ?: ""
                        if (spokenText.isNotBlank()) {
                            val parsed = parseVoiceCommand(spokenText)
                            onSpeechRecognized?.invoke(parsed)
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {}
                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })
            }

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.US.toLanguageTag())
                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false)
                putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
            }

            speechRecognizer?.startListening(intent)
        } catch (e: Throwable) {
            updateState(VoiceState.ERROR)
            onErrorOccurred?.invoke("Failed to start speech recognizer: ${e.message}")
        }
    }

    /**
     * Halts speech recognition.
     */
    fun stopListening() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Throwable) {}
        speechRecognizer = null
        updateState(VoiceState.IDLE)
    }

    /**
     * Synthesizes and speaks text responses back to the developer.
     */
    fun speakText(text: String) {
        if (isTtsReady && textToSpeech != null) {
            try {
                textToSpeech?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "antigravity_voice_output")
            } catch (_: Throwable) {}
        }
    }

    /**
     * Allows test suites and simulated input to exercise voice parsing without hardware.
     */
    fun simulateVoiceInput(spokenText: String): VoiceCommandResult {
        updateState(VoiceState.LISTENING)
        updateState(VoiceState.PROCESSING)
        val result = parseVoiceCommand(spokenText)
        updateState(VoiceState.IDLE)
        onSpeechRecognized?.invoke(result)
        return result
    }

    private fun updateState(newState: VoiceState) {
        currentState = newState
        onStateChanged?.invoke(newState)
    }

    fun release() {
        stopListening()
        try {
            textToSpeech?.stop()
            textToSpeech?.shutdown()
        } catch (_: Throwable) {}
        textToSpeech = null
        isTtsReady = false
    }
}
