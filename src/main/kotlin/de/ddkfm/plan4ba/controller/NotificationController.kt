package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.SentryTurret
import de.ddkfm.plan4ba.capture
import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.HibernateLectureChange
import de.ddkfm.plan4ba.models.database.HibernateNotification
import de.ddkfm.plan4ba.models.database.HibernateUser
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/notifications")
class NotificationController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allNotifications(@QueryParam("userId") userId : Int) : List<Notification>? {
        val where = if(userId != -1) "user_id = $userId" else "1=1"
        return inSession { it.list<HibernateNotification>(where) }?.map { it.toModel() }
    }

    @GET
    @Path("/:id")
    fun getNotifcation(@PathParam("id") id : Int) : Notification {
        var notification = inSession { it.single<HibernateNotification>(id) }
        return notification?.toModel() ?: throw NotFound()
    }

    @PUT
    @Path("")
    fun createNotification(notification : Notification) : Notification {
        val user = inSession { it.single<HibernateUser>(notification.userId) }
            ?: throw BadRequest("user does not exist")
        val hibernateNotification = notification
            .toHibernate<HibernateNotification>()
            .apply {
                this.user = user
            }
        inSession { session ->
            session save hibernateNotification
        }
        return hibernateNotification.toModel()
    }

    @DELETE
    @Path("/:id")
    fun deleteNotification(@PathParam("id") id: Int) : OK {
        val notification = inSession { it.single<HibernateNotification>(id) }
            ?: throw BadRequest("notification does not exit")
        val changes = inSession { it.list<HibernateLectureChange>("notification_id = ${notification.id}") } ?: emptyList()
        val changeIds = changes.map { it.id }
            .joinToString(prefix = "(", postfix = ")", separator = ",")
        val lectureIds = changes
            .map { it.old?.id }
            .filterNotNull()
            .distinct()
            .joinToString(prefix = "(", postfix = ")", separator = ",")
        val hqlScripts = mutableListOf<String>()
        if(changeIds != "()")
            hqlScripts.add("DELETE From HibernateLectureChange Where id in $changeIds")
        if(lectureIds != "()")
            hqlScripts.add("DELETE From HibernateLecture Where id in $lectureIds")
        hqlScripts.add("DELETE From HibernateNotification Where id = ${notification.id}")
        return inSession { session ->
            val transaction = session.beginTransaction()
            try {
                val sumRowsDeleted = hqlScripts
                    .map { println(it);it }
                    .map(session::createQuery)
                    .map { it.executeUpdate() }
                    .sum()
                OK("notification was deleted")
            } catch(e : Exception) {
                transaction.rollback()
                SentryTurret.log {
                    addTag("Hibernate", "")
                }.capture(e)
                throw InternalServerError("notification could not deleted")
            }
        } ?: throw InternalServerError("notification could not deleted")
    }
}