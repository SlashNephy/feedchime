package blue.starry.feedchime

import io.ktor.client.*
import io.ktor.client.features.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.http.*
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.transactions.TransactionManager
import java.sql.Connection

val FeedchimeConfig = Config.load()

val FeedchimeHttpClient by lazy {
    HttpClient {
        install(JsonFeature) {
            serializer = KotlinxSerializer()
        }

        defaultRequest {
            userAgent("feedchime (+https://github.com/SlashNephy/feedchime)")
        }
    }
}

val FeedchimeDatabase by lazy {
    TransactionManager.manager.defaultIsolationLevel = Connection.TRANSACTION_SERIALIZABLE

    Database.connect("jdbc:sqlite:data/database.db", "org.sqlite.JDBC")
}
