package blue.starry.feedchime

import dev.kord.common.entity.DiscordEmbed
import dev.kord.common.entity.optional.Optional
import dev.kord.common.entity.optional.optional
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DiscordWebhookMessage(
    val content: String? = null,
    val username: String? = null,
    @SerialName("avatar_url") val avatarUrl: String? = null,
    val embeds: List<DiscordEmbed> = emptyList()
)

fun <T: Any> T?.toOptionalAnyway(): Optional<T> {
    return this?.optional() ?: Optional.Missing()
}
