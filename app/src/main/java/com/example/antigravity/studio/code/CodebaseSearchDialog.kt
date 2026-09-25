package com.example.antigravity.studio.code

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.antigravity.theme.AntigravityColors
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CodebaseSearchDialog(
    workspaceDir: File,
    onSelectResult: (File, Int) -> Unit,
    onDismiss: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var searchResults by remember { mutableStateOf<List<CodebaseSearchResult>>(emptyList()) }
    var indexSummary by remember { mutableStateOf<IndexSummary?>(null) }
    var isIndexing by remember { mutableStateOf(false) }

    // Run semantic search when query changes (on IO to avoid ANR)
    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            searchResults = kotlinx.coroutines.withContext(kotlinx.coroutines.Dispatchers.IO) {
                CodebaseSemanticIndexer.search(searchQuery, workspaceDir)
            }
        } else {
            searchResults = emptyList()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF0D1321),
            border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.5f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = AntigravityColors.NeonViolet.copy(alpha = 0.2f)
                        ) {
                            Text(
                                "@codebase",
                                color = AntigravityColors.NeonViolet,
                                fontWeight = FontWeight.Bold,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            "Semantic Code Search",
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 16.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = AntigravityColors.TextSecondary)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Index Status & Re-index Action
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (indexSummary != null) {
                            "Indexed ${indexSummary!!.symbolsCount} symbols in ${indexSummary!!.indexedFilesCount} files"
                        } else {
                            "AST Semantic Indexer Active"
                        },
                        fontSize = 11.sp,
                        color = Color(0xFF94A3B8)
                    )

                    TextButton(
                        onClick = {
                            isIndexing = true
                            val summary = CodebaseSemanticIndexer.indexWorkspace(workspaceDir)
                            indexSummary = summary
                            isIndexing = false
                            if (searchQuery.isNotBlank()) {
                                searchResults = CodebaseSemanticIndexer.search(searchQuery, workspaceDir)
                            }
                        },
                        enabled = !isIndexing,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Icon(Icons.Default.Refresh, contentDescription = null, tint = AntigravityColors.NeonViolet, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (isIndexing) "Indexing..." else "Re-index", color = AntigravityColors.NeonViolet, fontSize = 11.sp)
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text("Search functions, classes, schemas, or endpoints...", color = AntigravityColors.TextMuted, fontSize = 13.sp)
                    },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null, tint = AntigravityColors.NeonViolet)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Clear", tint = AntigravityColors.TextSecondary)
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AntigravityColors.NeonViolet,
                        unfocusedBorderColor = AntigravityColors.CardBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Results List
                if (searchQuery.isBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.FindInPage,
                                contentDescription = null,
                                tint = Color(0xFF475569),
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Type any query to search the codebase",
                                color = Color(0xFF94A3B8),
                                fontSize = 13.sp
                            )
                            Text(
                                "Uses AST chunking, symbol matching & TF vector scoring",
                                color = Color(0xFF64748B),
                                fontSize = 11.sp
                            )
                        }
                    }
                } else if (searchResults.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            "No code symbols matched \"$searchQuery\"",
                            color = Color(0xFF94A3B8),
                            fontSize = 13.sp
                        )
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(searchResults) { result ->
                            val sym = result.symbol
                            val matchPercent = (result.similarityScore * 100).toInt()

                            Card(
                                shape = RoundedCornerShape(10.dp),
                                colors = CardDefaults.cardColors(containerColor = Color(0xFF131C2E)),
                                border = androidx.compose.foundation.BorderStroke(1.dp, AntigravityColors.NeonViolet.copy(alpha = 0.3f)),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(10.dp))
                                    .clickable {
                                        val targetFile = File(workspaceDir, sym.relativePath)
                                        if (targetFile.exists()) {
                                            onSelectResult(targetFile, sym.lineNumber)
                                            onDismiss()
                                        }
                                    }
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Surface(
                                                shape = RoundedCornerShape(4.dp),
                                                color = when (sym.type) {
                                                    SymbolType.CLASS, SymbolType.INTERFACE -> Color(0xFF7F52FF).copy(alpha = 0.25f)
                                                    SymbolType.FUNCTION -> Color(0xFF00E5FF).copy(alpha = 0.25f)
                                                    SymbolType.SCHEMA -> Color(0xFF10B981).copy(alpha = 0.25f)
                                                    SymbolType.ENDPOINT -> Color(0xFFF59E0B).copy(alpha = 0.25f)
                                                    else -> Color(0xFF64748B).copy(alpha = 0.25f)
                                                }
                                            ) {
                                                Text(
                                                    sym.type.name,
                                                    color = when (sym.type) {
                                                        SymbolType.CLASS, SymbolType.INTERFACE -> Color(0xFFA78BFA)
                                                        SymbolType.FUNCTION -> Color(0xFF38BDF8)
                                                        SymbolType.SCHEMA -> Color(0xFF34D399)
                                                        SymbolType.ENDPOINT -> Color(0xFFFBBF24)
                                                        else -> Color(0xFF94A3B8)
                                                    },
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                sym.name,
                                                fontWeight = FontWeight.Bold,
                                                color = Color.White,
                                                fontFamily = FontFamily.Monospace,
                                                fontSize = 13.sp
                                            )
                                        }

                                        Surface(
                                            shape = RoundedCornerShape(10.dp),
                                            color = Color(0xFF10B981).copy(alpha = 0.15f)
                                        ) {
                                            Text(
                                                "$matchPercent% match",
                                                color = Color(0xFF10B981),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        "${sym.relativePath}:${sym.lineNumber}",
                                        color = AntigravityColors.TextSecondary,
                                        fontSize = 11.sp,
                                        fontFamily = FontFamily.Monospace
                                    )

                                    Spacer(modifier = Modifier.height(6.dp))
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = Color(0xFF0A0E17),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            result.highlightSnippet,
                                            color = Color(0xFFCBD5E1),
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 11.sp,
                                            modifier = Modifier.padding(8.dp)
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
