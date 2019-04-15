import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.toJson
import de.ddkfm.plan4ba.utils.toListModel
import de.ddkfm.plan4ba.utils.toModel
import kong.unirest.Unirest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserTest {
    val endpoint = "http://localhost:8080"
    init {
        DBService.start()
    }

    @Test
    fun getAllUsers() {
        val users = Unirest.get("$endpoint/users")
            .asString().getOrThrow().toListModel<User>()
        println(users)
        assertNotNull(users)
    }

    @Test
    fun getAllSingleUsers() {
        val users = Unirest.get("$endpoint/users")
            .asString().getOrThrow().toListModel<User>()
        assertNotNull(users)
        if(users == null)
            return
        for(user in users) {
            val singleUser = Unirest.get("$endpoint/users/${user.id}")
                .asString().getOrThrow().toModel<User>()
            assertNotNull(singleUser)
        }
    }

    @Test
    fun authenticate() {
        val passwordParam = PasswordParam("")
        val authenticated = Unirest.post("$endpoint/users/1/authenticate")
            .body(passwordParam.toJson())
            .asString()
        println(authenticated.body)
        assert(authenticated.status == 200 || authenticated.body.contains("Unauthorized"))
    }

    @Test
    fun createUser() {
        val user = User(0,
            randomString(7),
            randomString(32),
            "abcd",
            1,
            System.currentTimeMillis(),
            0)
        val response = Unirest.put("$endpoint/users")
            .body(user.toJson())
            .asString().getOrThrow().toModel<User>()
        println(response)
        assert(response != null)
    }

    @Test
    fun updateUser() {
        val user = User(3,
            randomString(7),
            randomString(32),
            "abcd",
            1,
            System.currentTimeMillis(),
            System.currentTimeMillis() + 100)
        val response = Unirest.post("$endpoint/users/3")
            .body(user.toJson())
            .asString().getOrThrow().toModel<User>()
        println(response)
        assert(response != null)
    }
    @Test
    fun getLinksFromUser() {
        val links = Unirest.get("$endpoint/users/1/links")
            .asString().getOrThrow().toListModel<Link>()
        println(links)
        assertNotNull(links)
    }
}