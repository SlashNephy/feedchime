package blue.starry.feedchime

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import java.nio.file.Paths
import kotlin.io.path.readText

@Serializable
data class Config(
    val interval: Int = 3600,
    val limit: Int = 1,
    val logLevel: String? = null,
    val userAgent: String = "feedchime (+https://github.com/SlashNephy/feedchime)",
    val channels: List<Channel>
) {
    @Serializable
    data class Channel(
        val feeds: List<Feed>,
        val discordWebhookUrl: String
    )

    @Serializable
    data class Feed(
        val url: String,
        val name: String? = null,
        val avatarUrl: String? = null,
        val filter: Filter = Filter()
    )

    @Serializable
    data class Filter(
        val titles: List<String> = emptyList(),
        val ignoreTitles: List<String> = emptyList()
    )

    companion object {
        private val path = Paths.get("config.yml")

        fun load(): Config {
            val content = path.readText()
            return Yaml.default.decodeFromString(content)
        }
    }
}
