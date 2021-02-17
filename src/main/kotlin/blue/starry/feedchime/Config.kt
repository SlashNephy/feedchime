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
    val feeds: List<Feed> = emptyList()
) {
    @Serializable
    data class Feed(
        val url: String,
        val discordWebhookUrl: String? = null
    )

    companion object {
        private val path = Paths.get("config.yml")

        fun load(): Config {
            val content = path.readText()
            return Yaml.default.decodeFromString(content)
        }
    }
}
