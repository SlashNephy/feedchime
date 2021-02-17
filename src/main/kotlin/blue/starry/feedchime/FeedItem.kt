package blue.starry.feedchime

import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

data class FeedItem(
    val title: String,
    val description: String?,
    val url: String?,
    val author: String?,
    val category: String?,
    val time: ZonedDateTime?,
    val id: String
) {
    companion object {
        val alternativeDateTimeFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssX")!!
    }
}
