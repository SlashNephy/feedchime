package blue.starry.feedchime

import kotlinx.coroutines.delay
import mu.KotlinLogging
import kotlin.time.Duration.Companion.seconds

private val logger = KotlinLogging.createFeedchimeLogger("feedchime")

suspend fun main() {
    logger.info { "Application started!" }

    val feeds = FeedchimeConfig.feeds
    require(feeds.isNotEmpty()) {
        "No feeds available. Exit..."
    }

    require(FeedchimeConfig.interval >= 10) {
        "Too short interval passed. Please set it to 10 or greater value."
    }

    require(FeedchimeConfig.limit > 0) {
        "limit requires positive number (> 0)."
    }

    while (true) {
        FeedNotifier.check(feeds)

        logger.trace { "Sleep ${FeedchimeConfig.interval.seconds}." }
        delay(FeedchimeConfig.interval.seconds)
    }
}
