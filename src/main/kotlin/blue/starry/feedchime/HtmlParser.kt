package blue.starry.feedchime

import io.ktor.client.request.get
import io.ktor.http.Url
import org.jsoup.Jsoup
import org.jsoup.nodes.Element

object HtmlParser {
    suspend fun parse(url: String): Result? {
        val html = runCatching {
            FeedchimeHttpClient.get<String>(url)
        }.getOrNull() ?: return null
        val document = Jsoup.parse(html)
        val head = document.head() ?: return null
        
        return Result(
            thumbnailUrl = head.let {
                it.selectFirst("meta[property=\"og:image\"]")
                    ?: it.selectFirst("meta[name=\"twitter:image\"]")
                    ?: it.selectFirst("meta[name=\"thumbnail\"]")
            }?.attr("content")?.resolveRelativeUrl(url),
            faviconUrl = head.let {
                it.extractFaviconUrl()
                    ?: "/favicon.ico"
            }.resolveRelativeUrl(url),
            description = head.let {
                it.selectFirst("meta[property=\"og:description\"]")
                    ?: it.selectFirst("meta[name=\"twitter:description\"]")
                    ?: it.selectFirst("meta[name=\"description\"]")
            }?.attr("content")?.resolveRelativeUrl(url)
        )
    }
    
    private fun String.resolveRelativeUrl(htmlUrl: String): String {
        return when {
            startsWith("https://") || startsWith("http://") -> {
                this
            }
            startsWith('/') -> {
                val url = Url(htmlUrl)
                "${url.protocol.name}://${url.host}$this"
            }
            else -> {
                val url = Url(htmlUrl)
                val lastPath = url.encodedPath.substringBeforeLast('/')
                "${url.protocol.name}://${url.host}$lastPath/$this"
            }
        }
    }
    
    private fun Element.extractFaviconUrl(): String? {
        val sizesRegex = "^(\\d+)x(\\d)$".toRegex()
        
        val icon = select("link[rel=\"apple-touch-icon\"]").maxByOrNull { 
            val sizes = it.attr("sizes") ?: return null
            val match = sizesRegex.matchEntire(sizes) ?: return null
            
            val (a, b) = match.groupValues.map { s -> s.toInt() }
            a * b
        } ?: return null
        
        return icon.attr("href")
    }
    
    data class Result(
        val thumbnailUrl: String?,
        val faviconUrl: String,
        val description: String?
    )
}
