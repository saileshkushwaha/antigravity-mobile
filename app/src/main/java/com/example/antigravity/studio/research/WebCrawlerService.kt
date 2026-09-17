package com.example.antigravity.studio.research

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URL
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

data class CrawledDocument(
    val url: String,
    val title: String,
    val markdownContent: String,
    val domain: String,
    val latencyMs: Long,
    val charCount: Int
)

class WebCrawlerService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .followRedirects(true)
        .addInterceptor(com.example.antigravity.studio.observability.NetworkTrafficInterceptor())
        .build()

    /**
     * Crawls an arbitrary technical documentation or developer webpage via real HTTP.
     * Strips boilerplate scripts and converts HTML structures into clean markdown.
     */
    suspend fun crawlUrl(targetUrl: String): Result<CrawledDocument> = withContext(Dispatchers.IO) {
        val startTime = System.currentTimeMillis()
        try {
            val normalizedUrl = if (!targetUrl.startsWith("http://") && !targetUrl.startsWith("https://")) {
                "https://$targetUrl"
            } else {
                targetUrl
            }

            val url = try { java.net.URL(normalizedUrl) } catch (_: Exception) {
                return@withContext Result.failure(Exception("Invalid URL: $normalizedUrl"))
            }
            val host = url.host?.lowercase() ?: return@withContext Result.failure(Exception("No host in URL"))
            if (host == "localhost" || host == "127.0.0.1" || host == "::1" || host == "0.0.0.0"
                || host.startsWith("10.") || host.startsWith("192.168.") || host.startsWith("172.")
                || host == "169.254.169.254" || host.endsWith(".local") || host.endsWith(".internal")) {
                return@withContext Result.failure(Exception("SSRF blocked: $host is a private/internal address"))
            }
            if (url.protocol != "https" && url.protocol != "http") {
                return@withContext Result.failure(Exception("SSRF blocked: protocol ${url.protocol} not allowed"))
            }

            val request = Request.Builder()
                .url(normalizedUrl)
                .header("User-Agent", "Mozilla/5.0 (Android; Antigravity-Agent/3.0; Mobile Developer Studio)")
                .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
                .get()
                .build()

            val response = client.newCall(request).execute()
            val latency = System.currentTimeMillis() - startTime

            if (!response.isSuccessful) {
                return@withContext Result.failure(Exception("HTTP ${response.code}: ${response.message}"))
            }

            val html = response.body?.string() ?: ""
            val title = extractTitle(html, normalizedUrl)
            val markdown = htmlToCleanMarkdown(html)
            val domain = try { URL(normalizedUrl).host } catch (e: Exception) { "web" }

            Result.success(
                CrawledDocument(
                    url = normalizedUrl,
                    title = title,
                    markdownContent = markdown,
                    domain = domain,
                    latencyMs = latency,
                    charCount = markdown.length
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    companion object {
        fun extractTitle(html: String, fallbackUrl: String): String {
            val titleMatcher = Pattern.compile("<title>(.*?)</title>", Pattern.CASE_INSENSITIVE or Pattern.DOTALL).matcher(html)
            return if (titleMatcher.find()) {
                titleMatcher.group(1)?.trim()?.replace(Regex("\\s+"), " ") ?: fallbackUrl
            } else {
                fallbackUrl
            }
        }

        /**
         * High-speed, robust HTML to Markdown converter using regex and DOM tag rules.
         */
        fun htmlToCleanMarkdown(html: String): String {
            var text = html

            // Remove comments
            text = text.replace(Regex("<!--.*?-->", RegexOption.DOT_MATCHES_ALL), "")

            // Remove script, style, svg, noscript, nav, header, footer blocks
            val multiLineOptions = setOf(RegexOption.DOT_MATCHES_ALL, RegexOption.IGNORE_CASE)
            text = text.replace(Regex("<script.*?</script>", multiLineOptions), "")
            text = text.replace(Regex("<style.*?</style>", multiLineOptions), "")
            text = text.replace(Regex("<svg.*?</svg>", multiLineOptions), "")
            text = text.replace(Regex("<nav.*?</nav>", multiLineOptions), "")
            text = text.replace(Regex("<footer.*?</footer>", multiLineOptions), "")

            // Headers
            for (i in 6 downTo 1) {
                val hashes = "#".repeat(i)
                text = text.replace(Regex("<h$i.*?>(.*?)</h$i>", multiLineOptions)) {
                    "\n\n$hashes ${it.groupValues[1].trim()}\n\n"
                }
            }

            // Code blocks & preformatted
            text = text.replace(Regex("<pre.*?><code.*?>(.*?)</code></pre>", multiLineOptions)) {
                "\n\n```\n${it.groupValues[1].trim()}\n```\n\n"
            }
            text = text.replace(Regex("<code.*?>(.*?)</code>", multiLineOptions)) {
                "`${it.groupValues[1].trim()}`"
            }

            // Links
            text = text.replace(Regex("<a\\s+[^>]*href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>", multiLineOptions)) {
                val url = it.groupValues[1].trim()
                val label = it.groupValues[2].trim()
                if (label.isNotEmpty()) "[$label]($url)" else url
            }

            // Paragraphs & Line Breaks
            text = text.replace(Regex("<br\\s*/?>", RegexOption.IGNORE_CASE), "\n")
            text = text.replace(Regex("<p.*?>(.*?)</p>", multiLineOptions)) {
                "\n\n${it.groupValues[1].trim()}\n\n"
            }

            // Lists
            text = text.replace(Regex("<li.*?>(.*?)</li>", multiLineOptions)) {
                "\n• ${it.groupValues[1].trim()}"
            }

            // Strip remaining HTML tags
            text = text.replace(Regex("<.*?>"), "").replace(Regex("[ \\t]{2,}"), " ")

            // Decode common HTML entities
            text = text
                .replace("&amp;", "&")
                .replace("&lt;", "<")
                .replace("&gt;", ">")
                .replace("&quot;", "\"")
                .replace("&#39;", "'")
                .replace("&nbsp;", " ")

            // Normalize excess empty lines
            return text
                .replace(Regex("\n[ \t]+"), "\n")
                .replace(Regex("\n{3,}"), "\n\n")
                .trim()
                .take(6000) // Keep top 6,000 characters for token efficiency
        }
    }
}
