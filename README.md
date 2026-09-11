# Antigravity Mobile 🚀

[![Build & Package Android APK](https://github.com/saileshkushwaha/antigravity-mobile/actions/workflows/build-apk.yml/badge.svg)](https://github.com/saileshkushwaha/antigravity-mobile/actions/workflows/build-apk.yml)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Jetpack_Compose-2024.09.00-4285F4.svg?logo=android)](https://developer.android.com/jetpack/compose)
[![Material 3](https://img.shields.io/badge/Material_3-1.3.0-00E5FF.svg)](https://m3.material.io)
[![Android Min SDK](https://img.shields.io/badge/Min_SDK-26-green.svg)](https://developer.android.com)
[![License](https://img.shields.io/badge/License-Apache_2.0-blue.svg)](LICENSE)

**Antigravity Mobile** brings the full power, signature dark cyber aesthetic, and multi-surface workflows of the **Google Antigravity Desktop App** to Android devices. Built natively with **Kotlin**, **Jetpack Compose**, and **Material 3**, it provides an enterprise-ready mobile IDE and autonomous agent execution studio.

---

## 📱 Look & Feel Overview

![Antigravity Mobile UI Overview](docs/images/antigravity_mobile_overview.jpg)

The interface is engineered around an authentic **Dark Studio Theme** (`#101216` canvas, `#1E2128` surfaces) accented with signature neon tones:
- **Electric Cyan** (`#00E5FF`): Agent streams, active links, and tool indicators.
- **Nebula Violet** (`#8B5CF6`): Planning mode, gateway selections, and primary actions.
- **Diff Emerald** (`#10B981`): Code additions and successful tool executions.
- **Warning Amber** (`#F59E0B`): Planning approvals, notifications, and cautions.
- **Error Ruby** (`#EF4444`): Guardrail blocks and test failures.

---

## 🏛 Architecture & Surface Layout

Antigravity Mobile preserves the complete 3-surface workflow of the desktop application:

```
+-----------------------------------------------------------------------------------------+
|                                 ANTIGRAVITY MOBILE STUDIO                               |
+--------------------------+------------------------------------+-------------------------+
| 1. Left Drawer           | 2. Central Chat Canvas             | 3. Auxiliary Inspector  |
| - Quantum Prism Brand    | - Gateway & Model Selector Chip    | (5-Tab Developer Pane)  |
| - Workspace Switcher     | - Live Agent Status Indicator      | - 🤖 Subagents          |
|   (repo, branch, rules)  | - Chain-of-Thought (CoT) Accordion | - ⚡ Background Tasks   |
| - Conversation Threads   | - Interactive Tool Execution Cards | - 📄 Artifacts Viewer   |
| - Scheduled Cron Tasks   | - Planning Mode Review Gate        | - 🔀 Git Diff Viewer    |
| - Skills & MCP Directory | - Subagent Spawn Cards             | - 💻 Interactive        |
| - Settings & Diagnostics | - Multiline Input + Slash Commands |    Terminal Console     |
+--------------------------+------------------------------------+-------------------------+
```

---

## 🌟 Core Features & Modules

### 1. Central Chat Canvas & Agent Stream
- **Real-Time Agent Status Pill**: Reflects agent operational state (`Idle`, `Thinking...`, `Running Tool: {name}`, `Review Required`, `Streaming`).
- **Chain of Thought (CoT)**: Collapsible reasoning accordion calculating elapsed thinking time down to the second, giving transparent visibility into the agent's strategy before executing actions.
- **Interactive Tool Execution Cards**:
  - Live visual cards for `run_command`, `write_to_file`, `view_file`, and `grep_search`.
  - Displays arguments, execution spinner, exit codes, execution time, and an expandable terminal output window.
- **Planning Mode Approval Gate**:
  - Automatically activates when the agent initiates complex architectural changes.
  - Presents the implementation plan directly in chat with one-tap **Approve & Execute** and **Reject** review buttons.
- **Subagent Spawn Cards**: Renders background worker agents with real-time lifecycle states (`RUNNING`, `DONE`) and execution logs.
- **Docked Input Bar**:
  - Auto-expanding multiline composer.
  - Quick popup for **Slash Commands**: `/goal`, `/schedule`, `/grill-me`, `/boost`, `/browser`, `/teamwork-preview`.
  - Quick popup for **Context Mentions**: `@files`, `@terminal`, `@mcp`, `@git`.
  - Dynamic Send / Abort generation button.

---

### 2. 5-Tab Developer Auxiliary Inspector

![Developer Auxiliary Inspector](docs/images/antigravity_developer_inspector.jpg)

1. **🤖 Subagents**:
   - Lists child agent instances running concurrently in isolated contexts.
   - Shows assigned role, task description, status badge, and execution logs.
2. **⚡ Background Tasks**:
   - Monitors asynchronous jobs (e.g. `./gradlew assembleDebug`, file watchers, timers).
   - Live streaming logs with one-tap task cancellation (`Kill`).
3. **📄 Artifacts**:
   - Renders markdown deliverables, implementation plans, and architecture docs.
   - Fully supports GitHub-style alert callouts (`[!NOTE]`, `[!TIP]`, `[!WARNING]`, `[!CAUTION]`).
4. **🔀 Git Diff Viewer**:
   - File-by-file inspection of modified, added, or deleted code.
   - Highlights added lines in emerald green and removed lines in ruby red with unified addition/deletion counters (`+28`, `-0`).
5. **💻 Interactive Terminal Console**:
   - Built-in UNIX shell emulator with persistent prompt (`$ `).
   - Built-in commands: `help`, `git status`, `git diff`, `tasks`, `subagents`, `clear`, and custom scripts.

---

### 3. Open Model Gateways & Free Models Catalog

![Select Model Gateway & Search](docs/images/antigravity_models_catalog.jpg)

Antigravity Mobile includes a universal gateway engine supporting direct connections to Google Gemini and OpenAI-compatible providers:

- **OpenRouter Gateway**:
  - Connects to top open-weights models.
  - Includes popular free models: `Llama 3.3 70B Instruct (Free)`, `DeepSeek R1 (Free)`, `Gemini 2.0 Flash Exp (Free)`, `Qwen 2.5 Coder 32B (Free)`, `Mistral 7B (Free)`, `Phi-3 Mini (Free)`.
- **Groq LPU Gateway**:
  - Ultra-high-speed inference on Groq's Language Processing Units.
  - Supported models: `Llama 3.3 70B Versatile`, `Llama 3.1 8B Instant`, `Mixtral 8x7B`, `Gemma 2 9B`.
- **Ollama / Local Gateway**:
  - Connects directly to local LAN or on-device instances (`http://10.0.2.2:11434/v1` for emulators or custom host IP).
  - 100% private, offline inference (`llama3.3:latest`, `qwen2.5-coder:latest`, `deepseek-r1:8b`).
- **Hugging Face Serverless Inference**:
  - Connects to Hugging Face Inference API for models such as `Llama 3.2 3B` and `Qwen 2.5 7B`.
- **Google Gemini API**:
  - Native direct integration with `Gemini 2.5 Flash`, `Gemini 2.5 Pro`, and `Gemini Ultra`.
- **Searchable Model Selection Dialog**:
  - Real-time instant search filter matching model names, IDs, and tags.
  - Category filter chips: `All Models`, `★ Free Models`, `OpenRouter`, `Groq`, `Google Gemini`, `Ollama Local`, `Hugging Face`.
  - Rich cards indicating provider, context window capacity, and bright `FREE` badges.

---

### 4. Enterprise Production-Grade Architecture

Antigravity Mobile is engineered with enterprise security, observability, and performance standards:

- **🛡 Enterprise Security Guardrails (`EnterpriseSecurityGuardrails.kt`)**:
  - **Destructive Command Denylist**: Automatically intercepts and blocks dangerous commands (e.g., `rm -rf /`, `mkfs`, `DROP DATABASE`, `format`, `dd if=/dev/zero`).
  - **Workspace Path Confinement**: Enforces workspace boundary containment, preventing directory traversal attacks (e.g., `../../windows/system32`).
  - **Credential Masking**: Automatically masks sensitive API keys and tokens in console output, transcripts, and logs.
- **📋 Enterprise Structured Audit Logging (`EnterpriseAuditLogger.kt`)**:
  - Emits real-time audit events categorized across `SECURITY_POLICY`, `TOOL_EXECUTION`, `GATEWAY_CALL`, and `AUTH`.
  - Built-in one-click **JSON Audit Report Export** for compliance and security auditing.
- **📡 Reactive Network Monitoring (`NetworkMonitor.kt`)**:
  - Real-time `ConnectivityManager` flow tracking `WIFI`, `CELLULAR`, and `OFFLINE` states.
  - Graceful agent queueing and automatic retry upon reconnection.
- **🩺 Runtime Enterprise Diagnostics Suite (`EnterpriseDiagnosticsDialog.kt`)**:
  - **JVM Heap Memory Monitor**: Displays live used, free, and total allocated memory with visual gauge bars.
  - **Hardware Metrics**: Real-time CPU core count and active JVM thread monitor.
  - **Security Posture Checklist**: Verifies Sandboxing, Command Guardrails, TLS 1.3 encryption, and ProGuard R8 obfuscation.
  - **Live Audit Stream**: Filterable log viewer with one-click export.
- **⚡ ProGuard / R8 Optimization (`app/proguard-rules.pro`)**:
  - Production shrinking and obfuscation rules preserving Kotlinx Serialization, OkHttp, Coroutines, and Compose runtimes.

---

### 5. Left Navigation Drawer & Workspace Management
- **Brand Header**: Displays the custom **Antigravity Levitating Prism** logo with version indicator and "PRO" tier badge.
- **Workspace Switcher**: Switch between local repositories, display active Git branch, and inspect project rules.
- **Thread Management**: Create new sessions, switch active conversations, and delete obsolete threads.
- **Scheduled Tasks Modal**: Configure recurring cron jobs (e.g., `*/15 * * * *`) and one-shot timers.
- **Skills & MCP Directory**: Inspect loaded agent skills (`android-cli`, `antigravity-guide`, `firebase-basics`, `bigquery-sql`) and connected MCP servers (`gemini-api-docs`, `terminal-controller`).
- **Settings & Credentials**: Configure API keys for Gemini, OpenRouter, Groq, or Ollama, adjust execution review policies (`request-review`, `always-proceed`, `strict`), and manage terminal sandboxing.

---

## 🛠 Tech Stack

| Layer | Technologies |
| :--- | :--- |
| **Language** | Kotlin 2.0.21 |
| **UI Framework** | Jetpack Compose (BOM 2024.09.00), Material 3 (1.3.0) |
| **Icons** | Material Icons Extended + Custom Antigravity Vector Drawables |
| **Networking** | OkHttp 4.12.0 (Gemini & OpenAI Gateway Clients) |
| **Serialization** | Kotlinx Serialization JSON 1.7.3 |
| **Asynchrony** | Kotlin Coroutines & StateFlow |
| **Architecture** | Unidirectional Data Flow (UDF) / MVVM with AntigravityAgentEngine |
| **Testing** | JUnit 4 (10 unit test suites covering security, gateways, state, and terminal) |
| **CI / CD** | GitHub Actions (JDK 17, Gradle Cache, Automated APK packaging) |

---

## 🚀 Building & Running

### Prerequisites
- Android Studio Ladybug / Koala or newer
- JDK 17
- Android SDK 34 (Android 14)

### Local Build Commands

```bash
# Clone the repository
git clone https://github.com/saileshkushwaha/antigravity-mobile.git
cd antigravity-mobile

# Run the complete test suite (10 test suites)
./gradlew test

# Assemble Debug APK
./gradlew assembleDebug

# Output APK path:
# app/build/outputs/apk/debug/app-debug.apk
```

---

## 🤖 GitHub Actions Automated CI/CD

Every push to the `main` branch or pull request automatically triggers the `.github/workflows/build-apk.yml` pipeline:

1. Checks out the repository.
2. Configures JDK 17 with Gradle dependency caching.
3. Executes unit tests (`./gradlew test`).
4. Compiles the APK (`./gradlew assembleDebug`).
5. Uploads the debug APK as an accessible artifact: **`Antigravity-Mobile-Debug-APK`**.

You can download the pre-compiled APK directly from the [GitHub Actions tab](https://github.com/saileshkushwaha/antigravity-mobile/actions).

---

## 📄 License

Distributed under the Apache License 2.0. See `LICENSE` for more information.
