# Antigravity Mobile

[![Build & Package Android APK](https://github.com/saileshkushwaha/antigravity-mobile/actions/workflows/build-apk.yml/badge.svg)](https://github.com/saileshkushwaha/antigravity-mobile/actions/workflows/build-apk.yml)

**Antigravity Mobile** is an Android application that brings the full power and signature interface of the **Google Antigravity Desktop App** to Android devices, built with **Kotlin**, **Jetpack Compose**, and **Material 3**.

---

## Key Features & Surfaces

### 1. Left Navigation Drawer
- **Workspace Switcher**: Select and view active repository (`magical-bose [main]`), local directory path, and project-level rules.
- **Conversation Management**: Quick "New Conversation (+)" creation and history of active and archived threads.
- **Scheduled Tasks**: Monitor and configure recurring cron tasks (e.g. `*/15 * * * *`) and one-shot delayed timers.
- **Skills & MCP Directory**: Browse built-in Antigravity skills (`android-cli`, `antigravity-guide`, `firebase-basics`, etc.) and connected Model Context Protocol (MCP) servers.
- **Settings & Permissions**: Configure Gemini API credentials, choose models (`Gemini 2.5 Flash`, `Pro`, `Ultra`), select execution policies (`request-review`, `always-proceed`, `strict`), and toggle sandbox containers.

### 2. Central Chat Canvas
- **Dynamic Header**: Active model dropdown chip, live agent status pill (`Idle`, `Thinking...`, `Running Tool`, `Review Required`), and Auxiliary Inspector toggle.
- **Chain of Thought (CoT)**: Expandable thinking accordion with duration timer and internal reasoning stream.
- **Interactive Tool Execution Cards**: Visual cards for `run_command`, `write_to_file`, `view_file`, and `grep_search` with parameters, execution spinners, and expandable output preview.
- **Planning Mode Review Gate**: Interactive implementation plan card with **Approve & Execute** and **Reject** buttons.
- **Subagent Spawn Cards**: Real-time status cards for spawned subagents (`running`, `done`) with live actions.
- **Docked Input Bar**: Auto-expanding text input with `/` slash command popup (`/goal`, `/schedule`, `/grill-me`, `/boost`), `@` mentions popup (`@files`, `@terminal`, `@mcp`), file attachment pill, and dynamic Send/Stop generation toggle.

### 3. Auxiliary Pane (5-Tab Inspector)
1. **Subagents**: Active and completed child agents with lifecycle states and transcript drill-downs.
2. **Background Tasks**: Live background task runner with console logs and task termination (`Kill`).
3. **Artifacts**: Rendered markdown viewer for architecture plans and reports with GitHub-style callouts (`[!NOTE]`, `[!TIP]`, `[!WARNING]`).
4. **Git Diff Viewer**: Unified diff viewer showing changed files with addition/deletion metrics (`+28, -0`) and highlighted syntax lines.
5. **Interactive Terminal Console**: Built-in shell console (`$ `) with real-time command execution (`help`, `git status`, `tasks`, `subagents`, `clear`).

### 4. Dual Execution Engine
- **Live Gemini API Mode**: Communicates directly with Google's Gemini API via OkHttp when an API key is provided.
- **Autonomous Demo Mode**: Realistic simulation generating multi-step thinking, tool cards, file diffs, subagents, and background tasks offline without requiring an API key.

---

## Build & Verification

```bash
# Build debug APK
./gradlew assembleDebug

# Run unit tests
./gradlew test
```

Compiled APK binary: `app/build/outputs/apk/debug/app-debug.apk`
