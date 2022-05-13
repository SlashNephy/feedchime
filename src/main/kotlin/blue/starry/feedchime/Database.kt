package blue.starry.feedchime

import org.jetbrains.exposed.sql.Table

object RssFeedHistories: Table() {
    val feedUrl = varchar("feed_url", 256)
    val articleUrl = varchar("article_url", 256)
    val articleTime = long("article_time")
}
