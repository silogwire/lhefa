import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.toJson
import de.ddkfm.plan4ba.utils.toListModel
import de.ddkfm.plan4ba.utils.toModel
import kong.unirest.Unirest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class UserGroupTest {
    val endpoint = "http://localhost:8080"
    init {
        DBService.start()
    }

    @Test
    fun allUserGroups() {
        val response = Unirest.get("$endpoint/groups")
            .asString().getOrThrow().toListModel<UserGroup>()
        println(response)
        assert(response != null)
    }

    @Test
    fun getSingleUserGroups() {
        val groups = Unirest.get("$endpoint/groups")
            .asString().getOrThrow().toListModel<UserGroup>()
        if(groups == null)
            return
        for(group in groups) {
            val usergroup = Unirest.get("$endpoint/groups/${group.id}")
                .asString().getOrThrow().toModel<UserGroup>()
            assert(usergroup != null)
        }
    }

    @Test
    fun createUserGroup() {
        val group = UserGroup(0, randomString(6), 1)
        val userGroup = Unirest.put("$endpoint/groups")
            .body(group.toJson())
            .asString().getOrThrow().toModel<UserGroup>()
        println(userGroup)
        assertNotNull(userGroup)
    }

}