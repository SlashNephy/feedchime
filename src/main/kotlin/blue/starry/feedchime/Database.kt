package blue.starry.feedchime

import org.jetbrains.exposed.sql.Table

object RssFeedHistories: Table() {
    val url = varchar("url", 256)
    val uri = varchar("guid", 256)
}
