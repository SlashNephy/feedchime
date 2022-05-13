package blue.starry.feedchime

import com.charleskorn.kaml.Yaml
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.JsonNull.content
import java.nio.file.Paths
import kotlin.io.path.exists
import kotlin.io.path.readText

@Serializable
data class Config(
    val interval: Int = 3600,
    val limit: Int = 1,
    val logLevel: String? = null,
    val userAgent: String = "feedchime (+https://github.com/SlashNephy/feedchime)",
    val channels: List<Channel> = emptyList()
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
        val filter: Filter = Filter(),
        val extensions: Set<Extension> = emptySet()
    ) {
        @Serializable
        enum class Extension {
            @SerialName("hatena:bookmark")
            HatenaBookmark
        }
    }

    @Serializable
    data class Filter(
        val titles: List<String> = emptyList(),
        val ignoreTitles: List<String> = emptyList()
    )

    companion object {
        private val path = Paths.get("config.yml")

        fun load(): Config {
            if (!path.exists()) {
                return Config()
            }

            val content = path.readText()
            return Yaml.default.decodeFromString(content)
        }
    }
}
