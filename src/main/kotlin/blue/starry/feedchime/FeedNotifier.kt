package blue.starry.feedchime

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import mu.KotlinLogging
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import org.jsoup.Jsoup
import java.time.ZoneOffset

object FeedNotifier {
    init {
        transaction(FeedchimeDatabase) {
            SchemaUtils.create(RssFeedHistories)
        }
    }

    private val logger = KotlinLogging.createFeedchimeLogger("feedchime.notifier")

    suspend fun check(feeds: List<Config.Feed>) = coroutineScope {
        for (it in feeds) {
            checkEach(it)
        }
    }

    private suspend fun checkEach(config: Config.Feed) {
        val lastUri = transaction(FeedchimeDatabase) {
            RssFeedHistories.select { RssFeedHistories.url eq config.url }.firstOrNull()?.get(RssFeedHistories.uri)
        }
        var newUri = ""

        val feed = FeedParser.parse(config.url)
        feed.entries.asSequence()
            // require title and uri field
            .filter { it.title != null }
            .filter { it.uri != null }
            // check last link
            .takeWhile { it.uri != lastUri }
            // limit items
            .take(FeedchimeConfig.limit)
            .forEachIndexed { i, entry ->
                logger.trace { entry }

                // only notify when lastLink is present and check filter
                if (lastUri != null
                    && config.filter.titles.none { it !in entry.title }
                    && config.filter.ignoreTitles.none { it in entry.title }
                ) {
                    notify(feed, entry, config)
                }

                // save first uri
                if (i == 0) {
                    newUri = entry.uri
                }
            }

        // skip updating if new uri is null
        if (newUri.isEmpty()) {
            return
        }

        transaction(FeedchimeDatabase) {
            // update if exists
            if (lastUri != null) {
                RssFeedHistories.update({ RssFeedHistories.url eq config.url }) {
                    it[uri] = newUri
                }
            // insert if not exists
            } else {
                RssFeedHistories.insert {
                    it[url] = config.url
                    it[uri] = newUri
                }
            }
        }
    }

    private suspend fun notify(feed: SyndFeed, entry: SyndEntry, config: Config.Feed) {
        if (config.discordWebhookUrl != null) {
            notifyToDiscordWebhook(feed, entry, config.discordWebhookUrl)
        }
    }

    private suspend fun notifyToDiscordWebhook(feed: SyndFeed, entry: SyndEntry, webhookUrl: String) {
        FeedchimeHttpClient.post<Unit>(webhookUrl) {
            contentType(ContentType.Application.Json)

            body = DiscordWebhookMessage(
                embeds = listOf(
                    DiscordEmbed(
                        title = entry.titleEx.let {
                            if (it.type == "html") {
                                Jsoup.parse(it.value).text()
                            } else {
                                it.value
                            }
                        },
                        description = entry.contents.plus(entry.description).filterNotNull().joinToString("\n") {
                            if (it.type == "html") {
                                Jsoup.parse(it.value).text()
                            } else {
                                it.value
                            }
                        },
                        url = entry.link,
                        author = entry.authors.firstOrNull()?.let {
                            DiscordEmbed.Author(
                                name = it.name,
                                url = it.uri
                            )
                        },
                        footer = DiscordEmbed.Footer(
                            text = feed.title,
                            iconUrl = feed.image?.url
                        ),
                        thumbnail = entry.foreignMarkup.find { it.qualifiedName == "media:thumbnail" }?.let {
                            DiscordEmbed.Thumbnail(
                                url = it.getAttributeValue("url"),
                                height = it.getAttributeValue("height")?.toIntOrNull(),
                                width = it.getAttributeValue("width")?.toIntOrNull()
                            )
                        },
                        timestamp = (entry.updatedDate ?: entry.publishedDate)?.toInstant()?.atOffset(ZoneOffset.UTC)?.toZonedDateTime()
                    )
                )
            )
        }
    }
}
