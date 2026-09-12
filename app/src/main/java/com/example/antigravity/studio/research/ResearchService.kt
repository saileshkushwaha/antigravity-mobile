package com.example.antigravity.studio.research

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

data class ResearchPaper(
    val id: String,
    val title: String,
    val authors: List<String>,
    val abstractText: String,
    val publishedDate: String,
    val source: String, // "arXiv" or "PubMed"
    val url: String,
    val doi: String? = null,
    val journal: String? = null
)

class ResearchService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .build()

    /**
     * Searches arXiv via their official Atom API. Real-time scientific preprints.
     */
    suspend fun searchArxiv(query: String, maxResults: Int = 10): Result<List<ResearchPaper>> =
        withContext(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
                val url = "https://export.arxiv.org/api/query?search_query=all:$encodedQuery&start=0&max_results=$maxResults&sortBy=relevance&sortOrder=descending"

                val request = Request.Builder()
                    .url(url)
                    .header("User-Agent", "Antigravity-Research-Studio/1.0 (Mobile AI Agent Studio)")
                    .get()
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext Result.failure(Exception("arXiv query failed: HTTP ${response.code}"))
                }

                val xmlBody = response.body?.string() ?: ""
                val papers = parseArxivXml(xmlBody)
                Result.success(papers)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    /**
     * Searches NCBI PubMed via official E-utilities REST APIs (esearch + esummary).
     */
    suspend fun searchPubMed(query: String, maxResults: Int = 10): Result<List<ResearchPaper>> =
        withContext(Dispatchers.IO) {
            try {
                val encodedQuery = URLEncoder.encode(query.trim(), "UTF-8")
                // Step 1: E-Search to retrieve PMIDs
                val searchUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?db=pubmed&term=$encodedQuery&retmode=json&retmax=$maxResults"
                val searchReq = Request.Builder()
                    .url(searchUrl)
                    .header("User-Agent", "Antigravity-Research-Studio/1.0")
                    .get()
                    .build()

                val searchResp = client.newCall(searchReq).execute()
                if (!searchResp.isSuccessful) {
                    return@withContext Result.failure(Exception("PubMed search failed: HTTP ${searchResp.code}"))
                }

                val searchJson = JSONObject(searchResp.body?.string() ?: "{}")
                val idListObj = searchJson.optJSONObject("esearchresult")?.optJSONArray("idlist")
                if (idListObj == null || idListObj.length() == 0) {
                    return@withContext Result.success(emptyList())
                }

                val uids = mutableListOf<String>()
                for (i in 0 until idListObj.length()) {
                    uids.add(idListObj.getString(i))
                }

                // Step 2: E-Summary to retrieve paper metadata
                val summaryUrl = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=pubmed&id=${uids.joinToString(",")}&retmode=json"
                val summaryReq = Request.Builder()
                    .url(summaryUrl)
                    .header("User-Agent", "Antigravity-Research-Studio/1.0")
                    .get()
                    .build()

                val summaryResp = client.newCall(summaryReq).execute()
                if (!summaryResp.isSuccessful) {
                    return@withContext Result.failure(Exception("PubMed summary failed: HTTP ${summaryResp.code}"))
                }

                val summaryJson = JSONObject(summaryResp.body?.string() ?: "{}")
                val resultObj = summaryJson.optJSONObject("result") ?: JSONObject()

                val papers = mutableListOf<ResearchPaper>()
                for (uid in uids) {
                    val paperObj = resultObj.optJSONObject(uid) ?: continue
                    val title = paperObj.optString("title", "Untitled").replace("&lt;b&gt;", "").replace("&lt;/b&gt;", "")
                    val pubDate = paperObj.optString("pubdate", "Unknown date")
                    val sourceJournal = paperObj.optString("source", "NCBI PubMed")

                    val authorsList = mutableListOf<String>()
                    val authorsArr = paperObj.optJSONArray("authors")
                    if (authorsArr != null) {
                        for (a in 0 until authorsArr.length()) {
                            val authorName = authorsArr.getJSONObject(a).optString("name")
                            if (authorName.isNotBlank()) authorsList.add(authorName)
                        }
                    }

                    var doiStr: String? = null
                    val articleIdsArr = paperObj.optJSONArray("articleids")
                    if (articleIdsArr != null) {
                        for (idx in 0 until articleIdsArr.length()) {
                            val idObj = articleIdsArr.getJSONObject(idx)
                            if (idObj.optString("idtype") == "doi") {
                                doiStr = idObj.optString("value")
                                break
                            }
                        }
                    }

                    papers.add(
                        ResearchPaper(
                            id = uid,
                            title = title,
                            authors = authorsList,
                            abstractText = "Published in $sourceJournal. PubMed ID: $uid" + (doiStr?.let { " | DOI: $it" } ?: ""),
                            publishedDate = pubDate,
                            source = "PubMed",
                            url = "https://pubmed.ncbi.nlm.nih.gov/$uid/",
                            doi = doiStr,
                            journal = sourceJournal
                        )
                    )
                }

                Result.success(papers)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    private fun parseArxivXml(xml: String): List<ResearchPaper> {
        val papers = mutableListOf<ResearchPaper>()
        try {
            val factory = XmlPullParserFactory.newInstance()
            factory.isNamespaceAware = true
            val parser = factory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            var inEntry = false
            var currentTag = ""

            var id = ""
            var title = ""
            var summary = ""
            var published = ""
            val authors = mutableListOf<String>()
            var inAuthor = false

            while (eventType != XmlPullParser.END_DOCUMENT) {
                val tagName = parser.name ?: ""
                when (eventType) {
                    XmlPullParser.START_TAG -> {
                        currentTag = tagName
                        if (tagName.equals("entry", ignoreCase = true)) {
                            inEntry = true
                            id = ""
                            title = ""
                            summary = ""
                            published = ""
                            authors.clear()
                        } else if (inEntry && tagName.equals("author", ignoreCase = true)) {
                            inAuthor = true
                        }
                    }
                    XmlPullParser.TEXT -> {
                        if (inEntry) {
                            val text = parser.text?.trim() ?: ""
                            if (text.isNotBlank()) {
                                when (currentTag.lowercase()) {
                                    "id" -> if (id.isEmpty()) id = text
                                    "title" -> title += if (title.isEmpty()) text else " $text"
                                    "summary" -> summary += if (summary.isEmpty()) text else " $text"
                                    "published" -> if (published.isEmpty()) published = text.take(10)
                                    "name" -> if (inAuthor) authors.add(text)
                                }
                            }
                        }
                    }
                    XmlPullParser.END_TAG -> {
                        if (tagName.equals("author", ignoreCase = true)) {
                            inAuthor = false
                        } else if (tagName.equals("entry", ignoreCase = true)) {
                            if (title.isNotBlank()) {
                                papers.add(
                                    ResearchPaper(
                                        id = id.substringAfterLast("/"),
                                        title = title.replace("\n", " ").trim(),
                                        authors = authors.toList(),
                                        abstractText = summary.replace("\n", " ").trim(),
                                        publishedDate = published,
                                        source = "arXiv",
                                        url = id
                                    )
                                )
                            }
                            inEntry = false
                        }
                        currentTag = ""
                    }
                }
                eventType = parser.next()
            }
        } catch (e: Exception) {
            // Return whatever papers were successfully parsed
        }
        return papers
    }

    /**
     * Downloads and extracts readable text from scientific papers (arXiv PDFs / PMC articles).
     */
    suspend fun extractPdfFullText(paper: ResearchPaper): Result<String> = withContext(Dispatchers.IO) {
        try {
            val pdfUrl = when {
                paper.source.equals("arXiv", ignoreCase = true) -> {
                    val cleanId = paper.id.substringAfterLast("/")
                    "https://arxiv.org/pdf/$cleanId.pdf"
                }
                paper.url.endsWith(".pdf", ignoreCase = true) -> paper.url
                else -> paper.url
            }

            val req = Request.Builder()
                .url(pdfUrl)
                .header("User-Agent", "Mozilla/5.0 (Linux; Android) Antigravity-Research-Studio/1.0")
                .get()
                .build()

            val resp = client.newCall(req).execute()
            if (!resp.isSuccessful) {
                return@withContext Result.failure(Exception("PDF fetch failed: HTTP ${resp.code} ($pdfUrl)"))
            }

            val bytes = resp.body?.bytes() ?: return@withContext Result.failure(Exception("Empty PDF response"))
            val extracted = parsePdfStreamText(bytes)
            Result.success(extracted)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extracts text streams and decompresses zlib/FlateDecode streams from PDF byte array.
     */
    fun parsePdfStreamText(bytes: ByteArray): String {
        val content = StringBuilder()
        val textString = String(bytes, Charsets.ISO_8859_1)

        val streamRegex = Regex("""stream\r?\n([\s\S]*?)\r?\nendstream""")
        val matches = streamRegex.findAll(textString)

        for (match in matches) {
            val rawStream = match.groupValues[1]
            var decompressed = ""

            try {
                val streamBytes = rawStream.toByteArray(Charsets.ISO_8859_1)
                val inflater = java.util.zip.Inflater()
                inflater.setInput(streamBytes)
                val buffer = ByteArray(4096)
                val outStream = java.io.ByteArrayOutputStream()
                while (!inflater.finished()) {
                    val count = inflater.inflate(buffer)
                    if (count == 0) break
                    outStream.write(buffer, 0, count)
                }
                inflater.end()
                decompressed = outStream.toString("UTF-8")
            } catch (_: Exception) {
                decompressed = rawStream
            }

            // Extract (text) Tj and [(text)] TJ operators
            val tjRegex = Regex("""\((.*?)\)\s*Tj""")
            val bracketTjRegex = Regex("""\[(.*?)\]\s*TJ""")

            tjRegex.findAll(decompressed).forEach { tjMatch ->
                val segment = tjMatch.groupValues[1]
                    .replace("\\(", "(")
                    .replace("\\)", ")")
                    .replace("\\n", "\n")
                if (segment.isNotBlank()) content.append(segment).append(" ")
            }

            bracketTjRegex.findAll(decompressed).forEach { bMatch ->
                val inner = bMatch.groupValues[1]
                val subTj = Regex("""\((.*?)\)""").findAll(inner)
                subTj.forEach { sub ->
                    content.append(sub.groupValues[1]).append(" ")
                }
            }
        }

        val extracted = content.toString().trim()
        return if (extracted.length > 50) {
            extracted.replace("""\s+""".toRegex(), " ")
        } else {
            val asciiRegex = Regex("""[A-Za-z0-9,.:;'"\-\s]{40,}""")
            val asciiBlocks = asciiRegex.findAll(textString).map { it.value.trim() }.filter { it.length > 50 }.take(20).toList()
            if (asciiBlocks.isNotEmpty()) {
                asciiBlocks.joinToString("\n\n")
            } else {
                "PDF structure parsed. Document stream contains vector/scanned raster graphics."
            }
        }
    }

    /**
     * Downloads PDF, parses full text, and persists to the SQLite research_documents table.
     */
    suspend fun downloadAndIndexPaper(
        paper: ResearchPaper,
        sqlEngine: com.example.antigravity.studio.analytics.AnalyticsSqlEngine
    ): Result<com.example.antigravity.studio.analytics.ResearchDocRecord> = withContext(Dispatchers.IO) {
        val extractRes = extractPdfFullText(paper)
        val fullText = extractRes.getOrDefault(paper.abstractText)
        val now = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.US).format(java.util.Date())
        val doc = com.example.antigravity.studio.analytics.ResearchDocRecord(
            id = paper.id,
            title = paper.title,
            authors = paper.authors.joinToString(", "),
            source = paper.source,
            url = paper.url,
            abstractText = paper.abstractText,
            fullText = fullText,
            extractedAt = now
        )
        sqlEngine.saveResearchDocument(doc)
        Result.success(doc)
    }
}
