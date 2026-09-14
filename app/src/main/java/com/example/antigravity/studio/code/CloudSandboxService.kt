package com.example.antigravity.studio.code

import com.example.antigravity.enterprise.AuditCategory
import com.example.antigravity.enterprise.EnterpriseAuditLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

enum class SandboxRunnerType {
    DOCKER_CONTAINER,
    GITHUB_CODESPACES,
    SSH_REMOTE_RUNNER,
    LOCAL_FALLBACK
}

data class SandboxConfig(
    val runnerType: SandboxRunnerType = SandboxRunnerType.LOCAL_FALLBACK,
    val endpointUrl: String = "",
    val authToken: String = "",
    val containerImage: String = "gradle:8.5-jdk17",
    val workspaceMountPath: String = "/workspace",
    val timeoutSeconds: Int = 120
)

data class SandboxExecutionResult(
    val exitCode: Int,
    val stdout: String,
    val stderr: String,
    val durationMs: Long,
    val runnerType: SandboxRunnerType
)

class SandboxExecutionException(message: String, val result: SandboxExecutionResult) : Exception(message)

/**
 * Cloud Sandbox & Remote Execution Bridge.
 * Allows mobile developers to dispatch heavy compilation and test commands
 * (Gradle, NPM, Cargo, Docker) to remote cloud containers or local shell bridges.
 */
object CloudSandboxService {

    private val _config = MutableStateFlow(SandboxConfig())
    val config: StateFlow<SandboxConfig> = _config.asStateFlow()

    private val httpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .addInterceptor(com.example.antigravity.studio.observability.NetworkTrafficInterceptor())
        .build()

    fun updateConfig(newConfig: SandboxConfig) {
        _config.value = newConfig
        EnterpriseAuditLogger.log(
            category = AuditCategory.SDLC_OPERATION,
            action = "CONFIG_SANDBOX_RUNNER",
            details = "Updated cloud sandbox runner to ${newConfig.runnerType} (${newConfig.endpointUrl.take(30)})"
        )
    }

    suspend fun executeCommand(
        command: String,
        config: SandboxConfig = _config.value,
        workDir: File? = null,
        onOutputLine: ((String) -> Unit)? = null
    ): Result<SandboxExecutionResult> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()

        if (config.runnerType == SandboxRunnerType.LOCAL_FALLBACK || config.endpointUrl.isBlank()) {
            // Local fallback execution / terminal simulation
            return@withContext try {
                val lines = mutableListOf<String>()
                onOutputLine?.invoke("[Local Runner] $ $command")

                // Execute local process if feasible, or simulate controlled output
                val process = ProcessBuilder()
                    .directory(workDir)
                    .command(if (System.getProperty("os.name")?.contains("Windows", ignoreCase = true) == true) {
                        listOf("cmd.exe", "/c", command)
                    } else {
                        listOf("sh", "-c", command)
                    })
                    .redirectErrorStream(false)
                    .start()

                // Read stdout on a separate thread to prevent buffer deadlock
                val stdoutThread = Thread {
                    try {
                        val reader = process.inputStream.bufferedReader()
                        var line: String? = reader.readLine()
                        while (line != null) {
                            synchronized(lines) { lines.add(line) }
                            onOutputLine?.invoke(line)
                            line = reader.readLine()
                        }
                    } catch (_: Exception) {}
                }
                stdoutThread.isDaemon = true
                stdoutThread.start()

                // Read stderr on a separate thread
                val stderrThread = Thread {
                    try {
                        val reader = process.errorStream.bufferedReader()
                        var line: String? = reader.readLine()
                        while (line != null) {
                            synchronized(lines) { lines.add("[STDERR] $line") }
                            onOutputLine?.invoke("[STDERR] $line")
                            line = reader.readLine()
                        }
                    } catch (_: Exception) {}
                }
                stderrThread.isDaemon = true
                stderrThread.start()

                stdoutThread.join()
                stderrThread.join()
                val finished = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    process.waitFor(config.timeoutSeconds.toLong(), TimeUnit.SECONDS)
                } else {
                    process.waitFor()
                    true
                }
                if (!finished) {
                    process.destroyForcibly()
                    val duration = System.currentTimeMillis() - startTime
                    onOutputLine?.invoke("[Timed out after ${config.timeoutSeconds}s] Command terminated: $command")
                    return@withContext Result.failure(Exception("Command timed out after ${config.timeoutSeconds}s"))
                }

                val duration = System.currentTimeMillis() - startTime
                val exit = process.exitValue()
                Result.success(
                    SandboxExecutionResult(
                        exitCode = exit,
                        stdout = lines.joinToString("\n"),
                        stderr = if (exit != 0) "Process exited with code $exit" else "",
                        durationMs = duration,
                        runnerType = SandboxRunnerType.LOCAL_FALLBACK
                    )
                )
            } catch (e: Exception) {
                val duration = System.currentTimeMillis() - startTime
                val errorMsg = "Command execution failed: ${e.message ?: "Unknown error"}"
                onOutputLine?.invoke(errorMsg)
                Result.failure(SandboxExecutionException(errorMsg, SandboxExecutionResult(
                    exitCode = 1,
                    stdout = "",
                    stderr = errorMsg,
                    durationMs = duration,
                    runnerType = SandboxRunnerType.LOCAL_FALLBACK
                )))
            }
        }

        // Remote HTTP / Docker / Codespaces Bridge
        try {
            val payload = JSONObject().apply {
                put("command", command)
                put("runnerType", config.runnerType.name)
                put("containerImage", config.containerImage)
                put("workspaceMountPath", config.workspaceMountPath)
            }

            val requestBuilder = Request.Builder()
                .url(config.endpointUrl)
                .post(payload.toString().toRequestBody("application/json".toMediaType()))

            if (config.authToken.isNotBlank()) {
                requestBuilder.header("Authorization", "Bearer ${config.authToken}")
            }

            val resp = httpClient.newCall(requestBuilder.build()).execute()
            val duration = System.currentTimeMillis() - startTime

            if (resp.isSuccessful) {
                val respBody = resp.body?.string() ?: "{}"
                val json = JSONObject(respBody)
                val exitCode = json.optInt("exitCode", 0)
                val stdout = json.optString("stdout", "Remote command succeeded")
                val stderr = json.optString("stderr", "")

                stdout.lines().forEach { onOutputLine?.invoke(it) }

                Result.success(
                    SandboxExecutionResult(
                        exitCode = exitCode,
                        stdout = stdout,
                        stderr = stderr,
                        durationMs = duration,
                        runnerType = config.runnerType
                    )
                )
            } else {
                val err = "HTTP ${resp.code}: ${resp.body?.string()?.take(150)}"
                onOutputLine?.invoke("[Error] Remote sandbox failure: $err")
                Result.failure(Exception(err))
            }
        } catch (e: Exception) {
            val duration = System.currentTimeMillis() - startTime
            onOutputLine?.invoke("[Exception] ${e.message}")
            Result.failure(e)
        }
    }
}
