package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
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
        return inSession { it.list<HibernateNotification>(where) }?.map { it.toNotification() }
    }

    @GET
    @Path("/:id")
    fun getNotifcation(@PathParam("id") id : Int) : Notification {
        var notification = inSession { it.single<HibernateNotification>(id) }
        return notification?.toNotification() ?: throw NotFound()
    }

    @PUT
    @Path("")
    fun createNotification(notification : Notification) : Notification {
        val existingNotification = inSession {
            it.list<HibernateNotification>("user_id = ${notification.userId} AND label = '${notification.label}'")
        }?.firstOrNull()
        if(existingNotification != null)
            throw AlreadyExists("notification already exists")
        else {
            val user = inSession { it.single<HibernateUser>(notification.userId) } ?: throw BadRequest("user does not exist")
            val hibernateNotification = HibernateNotification(0,
                notification.label,
                notification.description,
                notification.type,
                user
            )
            inSession { session ->
                session save hibernateNotification
            }
            return hibernateNotification.toNotification()

        }
    }

    @DELETE
    @Path("/:id")
    fun deleteNotification(@PathParam("id") id: Int) : OK {
        return inSession {
            it.transaction {session ->
                val affectedRows = session.createQuery("Delete From HibernateNotification WHERE id = $id")
                    .executeUpdate()
                OK("affected rows: $affectedRows")
            }
        } ?: throw InternalServerError("could not delete the notication")
    }
}