import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.toJson
import de.ddkfm.plan4ba.utils.toListModel
import de.ddkfm.plan4ba.utils.toModel
import kong.unirest.Unirest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import kotlin.test.assertNotNull

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class NotificationTest {
    val endpoint = "http://localhost:8080"
    init {
        DBService.start()
    }

    @Test
    fun getAllNotications() {
        val notifications = Unirest.get("$endpoint/notifications")
            .asString().getOrThrow().toListModel<Notification>()
        println(notifications)
        assertNotNull(notifications)
    }

    @Test
    fun getNotificationsByUserId() {
        val notifications = Unirest.get("$endpoint/notifications?userId=1")
            .asString().getOrThrow().toListModel<Notification>()
        println(notifications)
        assertNotNull(notifications)
    }

    @Test
    fun getAllSingleUsers() {
        val notifications = Unirest.get("$endpoint/notifications")
            .asString().getOrThrow().toListModel<Notification>()
        assertNotNull(notifications)
        if(notifications == null)
            return
        for(notification in notifications) {
            val singleNot = Unirest.get("$endpoint/notifications/${notification.id}")
                .asString().getOrThrow().toModel<Notification>()
            assertNotNull(singleNot)
        }
    }

    @Test
    fun createNotification() {
        val notification = Notification(0, randomString(), randomString(), randomString(), 1)
        val response = Unirest.put("$endpoint/notifications")
            .body(notification.toJson())
            .asString().getOrThrow().toModel<Notification>()
        assertNotNull(response)
    }

    @Test
    fun deleteNotification() {
        val response = Unirest.delete("$endpoint/notifications/2")
            .asString().getOrThrow().toModel<OK>()
        println(response)
        assert(response != null)
    }
}