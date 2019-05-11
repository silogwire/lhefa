package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.*
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/upcoming")
class UpcomingController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allUpcomings(@QueryParam("reminderId") reminderId : Int) : List<Upcoming>? {
        val where = if(reminderId != -1) "reminder_id = $reminderId" else "1=1"
        return inSession { it.list<HibernateUpcoming>(where) }?.map { it.toModel() }
    }

    @GET
    @Path("/:id")
    fun getUpcoming(@PathParam("id") id : Int) : Upcoming {
        var examStat = inSession { it.single<HibernateUpcoming>(id) }
        return examStat?.toModel() ?: throw NotFound()
    }

    @PUT
    @Path("")
    fun createResult(upcoming: Upcoming) : Upcoming{
        val reminder = inSession { it.single<HibernateReminder>(upcoming.reminderId) }
                ?: throw BadRequest("reminder stat does not exist")

        val hibernateResult = upcoming
            .toHibernate<HibernateUpcoming>()
            .apply {
                this.reminder = reminder
            }
        inSession { session ->
            session save hibernateResult
        }
        return hibernateResult.toModel()
    }
}