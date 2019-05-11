package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.SentryTurret
import de.ddkfm.plan4ba.capture
import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.*
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/latest")
class LatestExamResultController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allResults(@QueryParam("reminderId") userId : Int) : List<LatestExamResult>? {
        val where = if(userId != -1) "reminder_id = $userId" else "1=1"
        return inSession { it.list<HibernateLatestExamResult>(where) }?.map { it.toModel() }
    }

    @GET
    @Path("/:id")
    fun getResult(@PathParam("id") id : Int) : LatestExamResult {
        var examStat = inSession { it.single<HibernateLatestExamResult>(id) }
        return examStat?.toModel() ?: throw NotFound()
    }

    @PUT
    @Path("")
    fun createResult(result : LatestExamResult) : LatestExamResult{
        val reminder = inSession { it.single<HibernateReminder>(result.reminderId) }
                ?: throw BadRequest("reminder stat does not exist")

        val hibernateResult = result
            .toHibernate<HibernateLatestExamResult>()
            .apply {
                this.reminder = reminder
            }
        inSession { session ->
            session save hibernateResult
        }
        return hibernateResult.toModel()
    }
}