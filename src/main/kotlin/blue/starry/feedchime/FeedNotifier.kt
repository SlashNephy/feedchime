package blue.starry.feedchime

import com.rometools.rome.feed.synd.SyndEntry
import com.rometools.rome.feed.synd.SyndFeed
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CancellationException
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

    suspend fun check(channels: List<Config.Channel>) = coroutineScope {
        channels.flatMap { channel ->
            channel.feeds.map { channel to it } 
        }.map { (channel, feed) ->
            launch {
                checkEach(channel, feed)
            }
        }.joinAll()
    }

    private suspend fun checkEach(channel: Config.Channel, config: Config.Feed) {
        val (lastArticleUrl, lastArticleTime) = transaction(FeedchimeDatabase) {
            RssFeedHistories.select { RssFeedHistories.feedUrl eq config.url }.firstOrNull().let {
                it?.get(RssFeedHistories.articleUrl) to it?.get(RssFeedHistories.articleTime)
            }
        }
        var newArticleUrl: String? = null
        var newArticleTime: Long? = null
        
        val feed = try {
            FeedParser.parse(config.url)
        } catch (e: CancellationException) {
            return
        } catch (e: Throwable) {
            logger.error(e) { "Failed to parse feed ($config)" }
            return
        }

        feed.entries.asSequence()
            // require title and uri field
            .filter { it.title != null }
            .filter { it.uri != null }
            // check lastArticleUrl, newArticleTime
            .takeWhile { lastArticleUrl == null || it.uri != lastArticleUrl }
            .takeWhile { lastArticleTime == null || it.publishedDate.time > lastArticleTime }
            // limit items
            .take(FeedchimeConfig.limit)
            .forEachIndexed { i, entry ->
                logger.trace { entry }

                // only notify when lastArticleUrl is present, and check filter
                if (lastArticleUrl != null
                    && config.filter.titles.none { it !in entry.title }
                    && config.filter.ignoreTitles.none { it in entry.title }
                ) {
                    notify(feed, entry, channel, config)
                }

                // save as newArticleUrl, newArticleTime
                if (i == 0) {
                    newArticleUrl = entry.uri
                    newArticleTime = entry.publishedDate?.time
                }
            }

        // skip updating if newArticleUrl or newArticleTime is default
        if (newArticleUrl.isNullOrEmpty() || newArticleTime == null) {
            return
        }

        transaction(FeedchimeDatabase) {
            // update if exists
            if (lastArticleUrl != null) {
                RssFeedHistories.update({ RssFeedHistories.feedUrl eq config.url }) {
                    it[articleUrl] = newArticleUrl!!
                    it[articleTime] = newArticleTime!!
                }
            // insert if not exists
            } else {
                RssFeedHistories.insert {
                    it[feedUrl] = config.url
                    it[articleUrl] = newArticleUrl!!
                    it[articleTime] = newArticleTime!!
                }
            }
        }
    }

    private suspend fun notify(feed: SyndFeed, entry: SyndEntry, channel: Config.Channel, config: Config.Feed) {
        try {
            notifyToDiscordWebhook(feed, entry, channel.discordWebhookUrl, config.name, config.avatarUrl)
        } catch (e: ClientRequestException) {
            logger.error(e) { "Failed to send webhook. ($config)\nEntry = $entry" }
        }
    }

    private suspend fun notifyToDiscordWebhook(feed: SyndFeed, entry: SyndEntry, webhookUrl: String, name: String?, avatarUrl: String?) {
        FeedchimeHttpClient.post<Unit>(webhookUrl) {
            contentType(ContentType.Application.Json)

            body = DiscordWebhookMessage(
                username = name ?: feed.title,
                avatarUrl = avatarUrl ?: feed.image?.url,
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
                            Jsoup.parse(it.value).text()
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
