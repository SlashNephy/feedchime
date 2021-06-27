package blue.starry.feedchime

import com.rometools.rome.feed.synd.SyndFeed
import com.rometools.rome.io.SyndFeedInput
import io.ktor.client.request.*
import org.xml.sax.InputSource
import java.io.StringReader

object FeedParser {
    private val input = SyndFeedInput()

    suspend fun parse(url: String): SyndFeed {
        val content = FeedchimeHttpClient.use { client-> client.get<String>(url) }
        val source = InputSource(StringReader(content))

        return input.build(source)
    }
}
