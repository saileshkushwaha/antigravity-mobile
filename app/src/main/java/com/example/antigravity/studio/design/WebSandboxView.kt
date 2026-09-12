package com.example.antigravity.studio.design

import android.annotation.SuppressLint
import android.webkit.ConsoleMessage
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView

enum class SandboxViewport(val title: String, val widthDp: Int?, val heightDp: Int?) {
    MOBILE("Mobile (390×780)", 360, 680),
    TABLET("Tablet (600×800)", 540, 720),
    RESPONSIVE("Responsive Full", null, null)
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun WebSandboxView(
    modifier: Modifier = Modifier
) {
    var htmlContent by remember {
        mutableStateOf(
            """
<!DOCTYPE html>
<html>
<head>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <style>
        body {
            background-color: #0B0F19;
            color: #F8FAFC;
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
            margin: 0;
            padding: 20px;
            display: flex;
            flex-direction: column;
            align-items: center;
        }
        .card {
            background: #131C2E;
            border: 1px solid #00E5FF;
            border-radius: 16px;
            padding: 24px;
            width: 90%;
            max-width: 360px;
            box-shadow: 0 8px 32px rgba(0, 229, 255, 0.15);
        }
        h2 { color: #00E5FF; margin-top: 0; font-size: 1.25rem; }
        p { color: #94A3B8; font-size: 0.9rem; line-height: 1.5; }
        .counter-badge {
            display: inline-block;
            background: rgba(187, 134, 252, 0.2);
            color: #BB86FC;
            border: 1px solid #BB86FC;
            padding: 4px 12px;
            border-radius: 12px;
            font-weight: bold;
            font-size: 0.8rem;
            margin-bottom: 12px;
        }
        button {
            background: #00E5FF;
            color: #0B0F19;
            border: none;
            padding: 10px 18px;
            border-radius: 8px;
            font-weight: bold;
            cursor: pointer;
            width: 100%;
            margin-top: 12px;
            transition: transform 0.1s;
        }
        button:active { transform: scale(0.98); }
        .log-box {
            margin-top: 16px;
            font-family: monospace;
            font-size: 0.8rem;
            color: #10B981;
        }
    </style>
</head>
<body>
    <div class="card">
        <div class="counter-badge">HTML5/JS LIVE SANDBOX</div>
        <h2>Autonomous Web App</h2>
        <p>This interactive component is executing live JavaScript in a secure Android WebContainer sandbox with real-time console streaming.</p>
        <button onclick="incrementCount()">Click Interactive Button</button>
        <div class="log-box" id="outputLog">Interactions: 0</div>
    </div>

    <script>
        let count = 0;
        function incrementCount() {
            count++;
            document.getElementById('outputLog').innerText = 'Interactions: ' + count;
            console.log('Button clicked! Current counter = ' + count);
        }
        console.log('WebContainer Sandbox initialized successfully.');
    </script>
</body>
</html>
            """.trimIndent()
        )
    }

    var selectedViewport by remember { mutableStateOf(SandboxViewport.MOBILE) }
    var consoleLogs by remember { mutableStateOf(listOf<String>()) }
    var webViewRef by remember { mutableStateOf<WebView?>(null) }
    var showConsoleDrawer by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0B0F19))
            .padding(12.dp)
    ) {
        // Controls Row: Viewport Selector + Run/Reload + Console Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                SandboxViewport.values().forEach { vp ->
                    val isSelected = selectedViewport == vp
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = if (isSelected) Color(0xFF00E5FF).copy(alpha = 0.2f) else Color(0xFF131C2E),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (isSelected) Color(0xFF00E5FF) else Color(0xFF334155)
                        ),
                        modifier = Modifier.clip(RoundedCornerShape(6.dp))
                    ) {
                        TextButton(
                            onClick = { selectedViewport = vp },
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                            modifier = Modifier.height(28.dp)
                        ) {
                            Text(
                                vp.title.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color(0xFF00E5FF) else Color.LightGray
                            )
                        }
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                IconButton(
                    onClick = {
                        webViewRef?.loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                    },
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "Reload", tint = Color(0xFF00E5FF), modifier = Modifier.size(18.dp))
                }

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = Color(0xFF1E293B)
                ) {
                    TextButton(
                        onClick = { showConsoleDrawer = !showConsoleDrawer },
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                        modifier = Modifier.height(28.dp)
                    ) {
                        Icon(Icons.Default.Terminal, contentDescription = null, tint = Color(0xFF10B981), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Logs (${consoleLogs.size})", fontSize = 11.sp, color = Color(0xFF10B981), fontFamily = FontFamily.Monospace)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Central WebContainer Sandbox Canvas Frame
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            val frameModifier = when {
                selectedViewport.widthDp != null && selectedViewport.heightDp != null ->
                    Modifier
                        .width(selectedViewport.widthDp!!.dp)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(20.dp))
                        .border(2.dp, Color(0xFF00E5FF).copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                else ->
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(12.dp))
            }

            Surface(
                modifier = frameModifier,
                color = Color.Black
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            webViewClient = WebViewClient()
                            webChromeClient = object : WebChromeClient() {
                                override fun onConsoleMessage(cm: ConsoleMessage?): Boolean {
                                    cm?.let {
                                        val logEntry = "[${it.messageLevel()}] ${it.message()} (L${it.lineNumber()})"
                                        consoleLogs = consoleLogs + logEntry
                                    }
                                    return true
                                }
                            }
                            loadDataWithBaseURL(null, htmlContent, "text/html", "UTF-8", null)
                            webViewRef = this
                        }
                    },
                    update = { view ->
                        webViewRef = view
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Live Console Drawer
        if (showConsoleDrawer) {
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = Color(0xFF050811),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF10B981).copy(alpha = 0.4f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Column(modifier = Modifier.padding(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Live JavaScript Sandbox Console", fontWeight = FontWeight.Bold, color = Color(0xFF10B981), fontSize = 11.sp)
                        TextButton(
                            onClick = { consoleLogs = emptyList() },
                            contentPadding = PaddingValues(0.dp),
                            modifier = Modifier.height(20.dp)
                        ) {
                            Text("Clear", color = Color.LightGray, fontSize = 10.sp)
                        }
                    }
                    HorizontalDivider(color = Color(0xFF1E293B), thickness = 0.5.dp, modifier = Modifier.padding(vertical = 4.dp))
                    if (consoleLogs.isEmpty()) {
                        Text("No console logs yet. Interact with the sandbox above.", color = Color.Gray, fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    } else {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(consoleLogs) { log ->
                                Text(
                                    log,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace,
                                    color = if (log.contains("ERROR")) Color(0xFFEF4444) else Color(0xFF6EE7B7)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
