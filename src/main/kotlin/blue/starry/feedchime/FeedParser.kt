package blue.starry.feedchime

import com.apptastic.rssreader.RssReader
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.utils.io.jvm.javaio.*
import kotlinx.coroutines.flow.flow

object FeedParser {
    private val reader = RssReader()

    suspend fun parse(url: String) = flow {
        FeedchimeHttpClient.get<HttpStatement>(url).execute {
            val stream = it.content.toInputStream()

            for (feed in reader.read(stream)) {
                emit(feed)
            }
        }
    }
}
