package blue.starry.feedchime

import kotlinx.coroutines.delay
import mu.KotlinLogging
import kotlin.time.seconds

private val logger = KotlinLogging.createFeedchimeLogger("feedchime")

suspend fun main() {
    logger.info { "Application started!" }
    
    require(FeedchimeConfig.interval >= 10) {
        "Too short interval passed. Please set it to 10 or greater value."
    }

    require(FeedchimeConfig.limit > 0) {
        "limit requires positive number (> 0)."
    }

    while (true) {
        FeedNotifier.check(FeedchimeConfig.channels)

        logger.trace { "Sleep ${FeedchimeConfig.interval.seconds}." }
        delay(FeedchimeConfig.interval.seconds)
    }
}
