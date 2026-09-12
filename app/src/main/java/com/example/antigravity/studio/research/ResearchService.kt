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
}
