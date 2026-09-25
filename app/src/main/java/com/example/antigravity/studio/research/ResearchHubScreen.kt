package com.example.antigravity.studio.research

import android.content.Intent
import android.net.Uri
import androidx.core.net.toUri
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.antigravity.studio.code.CodeStudioManager
import kotlinx.coroutines.launch
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResearchHubScreen(
    activeWorkspaceDir: File,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val researchService = remember { ResearchService() }
    val crawlerService = remember { WebCrawlerService() }

    var searchQuery by remember { mutableStateOf("") }
    var selectedSourceIndex by remember { mutableIntStateOf(0) } // 0: All, 1: arXiv, 2: PubMed, 3: Web Crawler
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var papers by remember { mutableStateOf<List<ResearchPaper>>(emptyList()) }
    var matrixPapers by remember { mutableStateOf<Set<ResearchPaper>>(emptySet()) }
    var showMatrixDialog by remember { mutableStateOf(false) }
    var expandedPaperId by remember { mutableStateOf<String?>(null) }
    
    val sqlEngine = remember(activeWorkspaceDir) {
        com.example.antigravity.studio.analytics.AnalyticsSqlEngine(context, activeWorkspaceDir)
    }
    var isExtractingText by remember { mutableStateOf(false) }
    var showFullTextDialog by remember { mutableStateOf(false) }
    var selectedPaperDoc by remember { mutableStateOf<com.example.antigravity.studio.analytics.ResearchDocRecord?>(null) }

    val quickChips = listOf(
        "Agentic AI", "Chain of Thought", "https://developer.android.com", "CRISPR Cas9", "RAG Embeddings"
    )

    fun performSearch(queryText: String) {
        if (queryText.isBlank()) {
            errorMessage = null
            papers = emptyList()
            return
        }
        isLoading = true
        errorMessage = null
        coroutineScope.launch {
            val combinedList = mutableListOf<ResearchPaper>()
            var anySuccess = false
            var failureMsg: String? = null

            val isUrl = queryText.startsWith("http://", ignoreCase = true) ||
                    queryText.startsWith("https://", ignoreCase = true) ||
                    queryText.endsWith(".com", ignoreCase = true) ||
                    queryText.endsWith(".org", ignoreCase = true) ||
                    queryText.endsWith(".dev", ignoreCase = true)

            if (selectedSourceIndex == 3 || (selectedSourceIndex == 0 && isUrl)) {
                val crawlRes = crawlerService.crawlUrl(queryText)
                crawlRes.onSuccess { doc ->
                    combinedList.add(
                        ResearchPaper(
                            id = doc.domain + "-" + System.currentTimeMillis(),
                            title = doc.title,
                            authors = listOf("Source: ${doc.domain}"),
                            abstractText = doc.markdownContent,
                            publishedDate = "Live Crawl (${doc.latencyMs}ms)",
                            source = "Web Crawler",
                            url = doc.url
                        )
                    )
                    anySuccess = true
                }.onFailure {
                    failureMsg = it.message
                }
            }

            if (selectedSourceIndex == 0 || selectedSourceIndex == 1) {
                val arxivRes = researchService.searchArxiv(queryText)
                arxivRes.onSuccess {
                    combinedList.addAll(it)
                    anySuccess = true
                }.onFailure {
                    if (failureMsg == null) failureMsg = it.message
                }
            }

            if (selectedSourceIndex == 0 || selectedSourceIndex == 2) {
                val pubmedRes = researchService.searchPubMed(queryText)
                pubmedRes.onSuccess {
                    combinedList.addAll(it)
                    anySuccess = true
                }.onFailure {
                    if (failureMsg == null) failureMsg = it.message
                }
            }

            isLoading = false
            if (anySuccess) {
                papers = combinedList.sortedByDescending { paper ->
                    runCatching {
                        java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US)
                            .parse(paper.publishedDate.take(10))?.time ?: 0L
                    }.getOrDefault(0L)
                }
            } else {
                errorMessage = failureMsg ?: "No papers or documents found"
            }
        }
    }

    // Initial search on first composition
    LaunchedEffect(Unit) {
        performSearch(searchQuery)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.MenuBook,
                            contentDescription = "Research Hub",
                            tint = Color(0xFF38BDF8),
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column {
                            Text(
                                "Deep Research Hub",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            Text(
                                "Real-time arXiv & PubMed Engine",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF94A3B8)
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                actions = {
                    BadgedBox(
                        badge = {
                            if (matrixPapers.isNotEmpty()) {
                                Badge(containerColor = Color(0xFF38BDF8)) {
                                    Text(matrixPapers.size.toString(), color = Color.Black)
                                }
                            }
                        }
                    ) {
                        IconButton(onClick = { showMatrixDialog = true }) {
                            Icon(
                                Icons.AutoMirrored.Filled.CompareArrows,
                                contentDescription = "Citation Matrix",
                                tint = if (matrixPapers.isNotEmpty()) Color(0xFF38BDF8) else Color.White
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0F172A)
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(Color(0xFF0B0F19))
                .padding(16.dp)
        ) {
            // Search Omnibar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("Query scientific literature (title, keywords, author)") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = {
                    Icon(Icons.Default.Search, contentDescription = null, tint = Color(0xFF38BDF8))
                },
                trailingIcon = {
                    IconButton(onClick = { performSearch(searchQuery) }) {
                        Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Search", tint = Color(0xFF38BDF8))
                    }
                },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF38BDF8),
                    unfocusedBorderColor = Color(0xFF334155),
                    focusedContainerColor = Color(0xFF131C2E),
                    unfocusedContainerColor = Color(0xFF131C2E),
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Source Selector Tabs
            PrimaryTabRow(
                selectedTabIndex = selectedSourceIndex,
                containerColor = Color(0xFF131C2E),
                contentColor = Color(0xFF38BDF8),
                modifier = Modifier.clip(RoundedCornerShape(8.dp))
            ) {
                listOf("All Sources", "arXiv", "PubMed", "Web & Docs Crawler").forEachIndexed { index, title ->
                    Tab(
                        selected = selectedSourceIndex == index,
                        onClick = {
                            selectedSourceIndex = index
                            performSearch(searchQuery)
                        },
                        text = {
                            Text(
                                title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedSourceIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedSourceIndex == index) Color(0xFF38BDF8) else Color.LightGray
                            )
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Quick Chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(quickChips) { chipText ->
                    FilterChip(
                        selected = searchQuery == chipText,
                        onClick = {
                            searchQuery = chipText
                            performSearch(chipText)
                        },
                        label = { Text(chipText, fontSize = 12.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = Color(0xFF38BDF8).copy(alpha = 0.2f),
                            selectedLabelColor = Color(0xFF38BDF8),
                            containerColor = Color(0xFF1E293B),
                            labelColor = Color.LightGray
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Status or Results
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(color = Color(0xFF38BDF8))
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            "Querying live scientific endpoints (arXiv & PubMed)...",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }
                }
            } else if (errorMessage != null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(Icons.Default.Warning, contentDescription = null, tint = Color(0xFFEF4444), modifier = Modifier.size(40.dp))
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            "Literature Query Error",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            errorMessage ?: "Unknown error",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { performSearch(searchQuery) },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                        ) {
                            Text("Retry Search", color = Color.Black, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (papers.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No papers found for \"$searchQuery\"", color = Color.Gray)
                }
            } else {
                Text(
                    "${papers.size} Real Scientific Papers Retrieved",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color(0xFF94A3B8)
                )
                Spacer(modifier = Modifier.height(6.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(papers, key = { it.id }) { paper ->
                        val isExpanded = expandedPaperId == paper.id
                        val isInMatrix = matrixPapers.contains(paper)

                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                            border = androidx.compose.foundation.BorderStroke(
                                1.dp,
                                if (isInMatrix) Color(0xFF38BDF8) else Color(0xFF1E293B)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                // Source & Date Row
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val (badgeBg, badgeBorder, badgeText) = when (paper.source) {
                                        "arXiv" -> Triple(Color(0xFFB91C1C).copy(alpha = 0.2f), Color(0xFFEF4444), Color(0xFFFCA5A5))
                                        "PubMed" -> Triple(Color(0xFF0284C7).copy(alpha = 0.2f), Color(0xFF38BDF8), Color(0xFF7DD3FC))
                                        else -> Triple(Color(0xFF064E3B).copy(alpha = 0.2f), Color(0xFF10B981), Color(0xFF6EE7B7))
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = badgeBg,
                                        border = androidx.compose.foundation.BorderStroke(1.dp, badgeBorder)
                                    ) {
                                        Text(
                                            paper.source.uppercase(),
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall,
                                            fontWeight = FontWeight.Bold,
                                            color = badgeText
                                        )
                                    }

                                    Text(
                                        paper.publishedDate,
                                        style = MaterialTheme.typography.labelSmall,
                                        color = Color(0xFF64748B)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Paper Title
                                Text(
                                    text = paper.title,
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                // Authors
                                if (paper.authors.isNotEmpty()) {
                                    Text(
                                        text = paper.authors.take(4).joinToString(", ") + if (paper.authors.size > 4) " et al." else "",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = Color(0xFF94A3B8),
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Spacer(modifier = Modifier.height(6.dp))
                                }

                                // Abstract (Collapsible)
                                Text(
                                    text = paper.abstractText,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(0xFFCBD5E1),
                                    maxLines = if (isExpanded) Int.MAX_VALUE else 3,
                                    overflow = TextOverflow.Ellipsis,
                                    lineHeight = 18.sp
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Action Buttons
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    TextButton(
                                        onClick = {
                                            expandedPaperId = if (isExpanded) null else paper.id
                                        }
                                    ) {
                                        Text(
                                            if (isExpanded) "Show Less" else "Read Abstract",
                                            fontSize = 12.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        IconButton(
                                            onClick = {
                                                coroutineScope.launch {
                                                    isExtractingText = true
                                                    val res = researchService.downloadAndIndexPaper(paper, sqlEngine)
                                                    isExtractingText = false
                                                    res.onSuccess { doc ->
                                                        selectedPaperDoc = doc
                                                        showFullTextDialog = true
                                                    }.onFailure { err ->
                                                        Toast.makeText(context, "PDF Extraction failed: ${err.message}", Toast.LENGTH_LONG).show()
                                                    }
                                                }
                                            }
                                        ) {
                                            if (isExtractingText) {
                                                CircularProgressIndicator(
                                                    modifier = Modifier.size(16.dp),
                                                    strokeWidth = 2.dp,
                                                    color = Color(0xFF10B981)
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Default.Description,
                                                    contentDescription = "Extract Full Text",
                                                    tint = Color(0xFF10B981)
                                                )
                                            }
                                        }

                                        IconButton(
                                            onClick = {
                                                matrixPapers = if (isInMatrix) {
                                                    matrixPapers - paper
                                                } else {
                                                    matrixPapers + paper
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = if (isInMatrix) Icons.Default.BookmarkAdded else Icons.Default.BookmarkBorder,
                                                contentDescription = "Bookmark",
                                                tint = if (isInMatrix) Color(0xFF38BDF8) else Color.LightGray
                                            )
                                        }

                                        IconButton(
                                            onClick = {
                                                try {
                                                    val intent = Intent(Intent.ACTION_VIEW, paper.url.toUri())
                                                    context.startActivity(intent)
                                                } catch (e: Exception) {
                                                    Toast.makeText(context, "Could not open URL", Toast.LENGTH_SHORT).show()
                                                }
                                            }
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.OpenInNew,
                                                contentDescription = "Open External Link",
                                                tint = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Comparative Citation Matrix Dialog
    if (showMatrixDialog) {
        AlertDialog(
            onDismissRequest = { showMatrixDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.CompareArrows, contentDescription = null, tint = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Comparative Literature Matrix", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(350.dp)
                ) {
                    if (matrixPapers.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                "No papers selected yet.\nBookmark papers to compare them side-by-side.",
                                color = Color.Gray,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(matrixPapers.toList()) { p ->
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = Color(0xFF0F172A),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Column(modifier = Modifier.padding(10.dp)) {
                                        Text(p.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 13.sp)
                                        Text(
                                            "${p.source} • ${p.publishedDate} • ${p.authors.firstOrNull() ?: "Unknown"}",
                                            fontSize = 11.sp,
                                            color = Color(0xFF38BDF8)
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            p.abstractText.take(150) + "...",
                                            fontSize = 11.sp,
                                            color = Color.LightGray
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (matrixPapers.isNotEmpty()) {
                            val sb = StringBuilder()
                            sb.append("# Scientific Literature Review Matrix\n\n")
                            sb.append("Generated by Antigravity Autonomous Research Studio\n\n")
                            sb.append("| Paper Title | Source | Published | Authors | Link |\n")
                            sb.append("|---|---|---|---|---|\n")
                            matrixPapers.forEach { p ->
                                val auth = p.authors.take(2).joinToString(", ")
                                sb.append("| ${p.title.replace("|", "/")} | ${p.source} | ${p.publishedDate} | $auth | [Link](${p.url}) |\n")
                            }
                            sb.append("\n## Executive Abstracts\n\n")
                            matrixPapers.forEach { p ->
                                sb.append("### ${p.title}\n")
                                sb.append("- **Source**: ${p.source} (${p.publishedDate})\n")
                                sb.append("- **URL**: ${p.url}\n\n")
                                sb.append("${p.abstractText}\n\n---\n\n")
                            }
                            val targetFile = File(activeWorkspaceDir, "literature_review.md")
                            val ok = CodeStudioManager.saveFileContent(targetFile, sb.toString())
                            val msg = if (ok) "Exported matrix to ${targetFile.name}!" else "Failed to export"
                            Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                            showMatrixDialog = false
                        } else {
                            Toast.makeText(context, "Select at least 1 paper first", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF38BDF8))
                ) {
                    Text("Export to Workspace", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showMatrixDialog = false }) {
                    Text("Close", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }

    // Full-Text Literature Extraction Dialog
    if (showFullTextDialog && selectedPaperDoc != null) {
        val doc = selectedPaperDoc!!
        AlertDialog(
            onDismissRequest = { showFullTextDialog = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.AutoMirrored.Filled.Article, contentDescription = null, tint = Color(0xFF10B981))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Deep Literature Full-Text", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    Text(doc.title, fontWeight = FontWeight.Bold, color = Color.White, fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("${doc.source} • Authors: ${doc.authors}", fontSize = 11.sp, color = Color(0xFF38BDF8))
                    Spacer(modifier = Modifier.height(8.dp))
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Column(
                            modifier = Modifier
                                .padding(12.dp)
                                .verticalScroll(androidx.compose.foundation.rememberScrollState())
                        ) {
                            Text(
                                "Extracted Full-Text (${doc.fullText.length} characters):",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF10B981)
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                doc.fullText,
                                fontSize = 12.sp,
                                color = Color.LightGray,
                                fontFamily = FontFamily.Monospace,
                                lineHeight = 18.sp
                            )
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as? android.content.ClipboardManager
                        clipboard?.setPrimaryClip(android.content.ClipData.newPlainText("Paper Full Text", doc.fullText))
                        Toast.makeText(context, "Full text copied to clipboard!", Toast.LENGTH_SHORT).show()
                        showFullTextDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981))
                ) {
                    Text("Copy Text", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showFullTextDialog = false }) {
                    Text("Close", color = Color.LightGray)
                }
            },
            containerColor = Color(0xFF1E293B)
        )
    }
}
