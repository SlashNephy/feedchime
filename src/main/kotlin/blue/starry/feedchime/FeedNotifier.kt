package blue.starry.feedchime

import dev.kord.common.entity.DiscordEmbed
import dev.kord.common.entity.optional.optional
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.takeWhile
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jsoup.Jsoup
import java.time.ZonedDateTime

object FeedNotifier {
    init {
        transaction(FeedchimeDatabase) {
            SchemaUtils.create(RssFeedHistories)
        }
    }

    private val logger = KotlinLogging.createFeedchimeLogger("feedchime.notifier")

    suspend fun check(feeds: List<Config.Feed>) = coroutineScope {
        feeds.map {
            launch {
                checkEach(it)
            }
        }.joinAll()
    }

    private suspend fun checkEach(feed: Config.Feed) {
        val lastGuid = transaction(FeedchimeDatabase) {
            RssFeedHistories.select { RssFeedHistories.url eq feed.url }.firstOrNull()?.get(RssFeedHistories.guid)
        }
        var newGuid = ""

        FeedParser.parse(feed.url)
            // require title and guid field
            .takeWhile { it.title.isPresent }
            .takeWhile { it.guid.isPresent }
            // check last guid
            .takeWhile { it.guid.get() != lastGuid }
            // limit items
            .take(FeedchimeConfig.limit)
            // transform to type-safe object
            .map { item ->
                FeedItem(
                    title = item.title.get(),
                    description = item.description.orElseGet { null },
                    url = item.link.orElseGet { null },
                    author = item.author.orElseGet { null },
                    category = item.category.orElseGet { null },
                    time = runCatching {
                        item.pubDateZonedDateTime
                    }.map { optional ->
                        optional.orElseGet { null }
                    }.recoverCatching {
                        ZonedDateTime.parse(item.pubDate.get(), FeedItem.alternativeDateTimeFormat)
                    }.getOrNull(),
                    id = item.guid.get()
                )
            }
            .collectIndexed { i, it ->
                // only notify when lastGuid is present
                if (lastGuid != null) {
                    notify(it, feed)
                }

                // save first guid
                if (i == 0) {
                    newGuid = it.id
                }

                logger.trace { it }
            }

        // skip updating if new guid is null
        if (newGuid.isEmpty()) {
            return
        }

        transaction(FeedchimeDatabase) {
            // update if exists
            if (lastGuid != null) {
                RssFeedHistories.update({ RssFeedHistories.url eq feed.url }) {
                    it[guid] = newGuid
                }
            // insert if not exists
            } else {
                RssFeedHistories.insert {
                    it[this.url] = feed.url
                    it[guid] = newGuid
                }
            }
        }
    }

    private suspend fun notify(item: FeedItem, feed: Config.Feed) {
        if (feed.discordWebhookUrl != null) {
            notifyToDiscordWebhook(item, feed.discordWebhookUrl)
        }
    }

    private suspend fun notifyToDiscordWebhook(item: FeedItem, webhookUrl: String) {
        FeedchimeHttpClient.post<Unit>(webhookUrl) {
            contentType(ContentType.Application.Json)

            body = DiscordWebhookMessage(
                embeds = listOf(
                    DiscordEmbed(
                        title = item.title.optional(),
                        description = item.description?.let { Jsoup.parse(it).text() }.toOptionalAnyway(),
                        url = item.url.toOptionalAnyway()
                    )
                )
            )
        }
    }
}
