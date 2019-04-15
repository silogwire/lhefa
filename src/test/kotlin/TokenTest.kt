import de.ddkfm.plan4ba.models.Lecture
import de.ddkfm.plan4ba.models.Token
import de.ddkfm.plan4ba.utils.toJson
import de.ddkfm.plan4ba.utils.toListModel
import de.ddkfm.plan4ba.utils.toModel
import kong.unirest.Unirest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class TokenTest {
    val endpoint = "http://localhost:8080"
    init {
        DBService.start()
    }

    @Test
    fun allTokens() {
        val response = Unirest.get("$endpoint/tokens")
            .asString().getOrThrow().toListModel<Token>()
        println(response)
        assert(response != null)
    }

    @Test
    fun getAllValidTokens() {
        val response = Unirest.get("$endpoint/tokens?valid=true&caldavToken=true&refreshToken=false")
            .asString().getOrThrow().toListModel<Token>()
        println(response)
        assert(response != null)
    }

    @Test
    fun getAllCaldavTokens() {
        val response = Unirest.get("$endpoint/tokens?caldavToken=true")
            .asString().getOrThrow().toListModel<Token>()
        println(response)
        assert(response != null)
    }

    @Test
    fun getAllRefreshTokens() {
        val response = Unirest.get("$endpoint/tokens?refreshToken=true")
            .asString().getOrThrow().toListModel<Token>()
        println(response)
        assert(response != null)
    }

    @Test
    fun createRefreshToken() {
        val token = Token(UUID.randomUUID().toString().replace("-", ""), 1, false,System.currentTimeMillis(), true)
        val response = Unirest.put("$endpoint/tokens")
            .body(token.toJson())
            .asString().getOrThrow()
            .toModel<Token>()
        println(response)
        assert(response != null && response.isRefreshToken)
    }

    @Test
    fun createCaldavToken() {
        val token = Token(UUID.randomUUID().toString().replace("-", ""), 1, true,System.currentTimeMillis(), false)
        val response = Unirest.put("$endpoint/tokens")
            .body(token.toJson())
            .asString().getOrThrow()
            .toModel<Token>()
        println(response)
        assert(response != null && response.isCalDavToken)
    }

    @Test
    fun createShortToken() {
        val token = Token(UUID.randomUUID().toString().replace("-", ""), 1, false,System.currentTimeMillis(), false)
        val response = Unirest.put("$endpoint/tokens")
            .body(token.toJson())
            .asString().getOrThrow()
            .toModel<Token>()
        println(response)
        assert(response != null && !(response.isCalDavToken &&response.isRefreshToken))
    }
}