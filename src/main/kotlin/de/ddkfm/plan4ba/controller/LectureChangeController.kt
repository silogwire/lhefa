package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.HibernateLecture
import de.ddkfm.plan4ba.models.database.HibernateLectureChange
import de.ddkfm.plan4ba.models.database.HibernateNotification
import de.ddkfm.plan4ba.utils.inSession
import de.ddkfm.plan4ba.utils.list
import de.ddkfm.plan4ba.utils.save
import de.ddkfm.plan4ba.utils.single
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/changes")
class LectureChangeController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allChanges(@QueryParam("notificationId") notificationId: Int): List<LectureChange>? {
        var where = "1=1"
        if (notificationId != -1)
            where += "AND notification_id = $notificationId"
        val changes = inSession { it.list<HibernateLectureChange>(where)?.map { it.toModel() } }
        return changes
    }

    @GET
    @Path("/:id")
    fun getLectureChange(@PathParam("id") id: Int,
                         @QueryParam("userId") userId: Int): LectureChange {
        val lecture = inSession { it.single<HibernateLectureChange>(id) }
            ?: throw NotFound("lecture change does not exist")
        return lecture.toModel()
    }

    @PUT
    @Path("")
    fun createChange(change: LectureChange): LectureChange {
        val existingChange = inSession { session ->
            session.list<HibernateLectureChange>("old_id = ${change.old} and new_id = ${change.new} and notification_id = ${change.notificationId}")
        }?.firstOrNull()
        if (existingChange != null)
            throw AlreadyExists("Lecture Change already exists")
        val notification = inSession { it.single<HibernateNotification>(change.notificationId) }
                ?: throw BadRequest("notification does not exist")
        val old = if(change.old != null) inSession { it.single<HibernateLecture>(change.old ?: -1) }
            else null
        val new = if(change.new != null) inSession { it.single<HibernateLecture>(change.new ?: -1) }
            else null
        val hibernateLectureChange = change.toHibernate<HibernateLectureChange>().apply {
            this.notification = notification
            this.old = old
            this.new = new
        }
        inSession { session ->
            session save hibernateLectureChange
        }
        return hibernateLectureChange.toModel()
    }
}