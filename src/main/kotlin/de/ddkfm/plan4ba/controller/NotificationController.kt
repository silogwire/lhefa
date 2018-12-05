package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.*
import io.swagger.annotations.*
import org.hibernate.Hibernate
import spark.Request
import spark.Response
import java.util.stream.Collectors
import javax.ws.rs.*

@Api(value = "/notifications", description = "all operations about notifications")
@Path("/notifications")
@Produces("application/json")
class NotificationController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @ApiOperation(value = "list all notifications", notes = "return all notifications")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Notification::class, responseContainer = "List")
    )
    @ApiImplicitParams(
            ApiImplicitParam(name = "userId", paramType = "query", dataType = "integer", required = false)
    )

    @Path("")
    fun allNotifications(@ApiParam(hidden = true) userId : Int) : Any? = HibernateUtils.doInHibernate { session ->
        var where = "WHERE 1=1"
        if(userId != -1)
            where += "AND user_id = $userId"
        session.createQuery("From HibernateNotification $where", HibernateNotification::class.java).list()
                .map { it.toNotification() }
    }

    @GET
    @ApiOperation(value = "get a specific notification", notes = "get a specific notification")
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Notification::class),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    @Path("/:id")
    fun getNotifcation(@ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var notification = session.find(HibernateNotification::class.java, id)
            notification?.toNotification() ?: NotFound()

        }
    }

    @PUT
    @ApiOperation(value = "create a notification")
    @Path("")
    @ApiResponses(
            ApiResponse(code = 201, message = "notification created", response = Notification::class),
            ApiResponse(code = 409, message = "notification already exists", response = AlreadyExists::class),
            ApiResponse(code = 500, message = "Could not save the notification", response = HttpStatus::class)
    )
    fun createNotification(@ApiParam notification : Notification) : Any? {
        return HibernateUtils.doInHibernate { session ->
            val existingNotification = session.createQuery("From HibernateNotification Where user_id = ${notification.userId} AND label = '${notification.label}'",HibernateNotification::class.java).list().firstOrNull()

            if(existingNotification != null)
                AlreadyExists("notification already exists")
            else {

                val user = session.find(HibernateUser::class.java, notification.userId)
                if(user == null)
                    BadRequest("user does not exist")
                else {
                    val hibernateNotification = HibernateNotification(0, notification.label, notification.description,  /*notification.type, notification.viewed, notification.data?.toJson(),*/ user)
                    try {
                        session.doInTransaction {
                            session.persist(hibernateNotification)
                            hibernateNotification.toNotification()
                        }
                    } catch (e : Exception) {
                        e.printStackTrace()
                        HttpStatus(500, "Could not save the token")
                    }
                }
            }
        }
    }

    @DELETE
    @ApiOperation(value = "delete a notification")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = OK::class)
    )
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @Path("/:id")
    fun deleteNotification(@ApiParam(hidden = true) id: Int) : Any? = HibernateUtils.doInHibernate { session ->
        session.doInTransaction {
            val affectedRows = session.createQuery("Delete From HibernateNotification WHERE id = $id")
                    .executeUpdate()
            OK("affected rows: $affectedRows")
        }

    }
}