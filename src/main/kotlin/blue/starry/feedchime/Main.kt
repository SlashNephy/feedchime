package blue.starry.feedchime

import kotlinx.coroutines.delay
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.seconds

suspend fun main() {
    val logger = KotlinLogging.createFeedchimeLogger("feedchime")
    logger.info { "Application started!" }

    require(FeedchimeConfig.interval >= 10) {
        "Too short interval passed. Please set it to 10 or greater value."
    }

    require(FeedchimeConfig.limit > 0) {
        "limit requires positive number (> 0)."
    }

    while (true) {
        FeedNotifier.check(FeedchimeConfig.channels)

        logger.trace { "Sleep ${Duration.seconds(FeedchimeConfig.interval)}." }
        delay(Duration.seconds(FeedchimeConfig.interval))
    }
}
