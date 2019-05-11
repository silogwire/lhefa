package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.SentryTurret
import de.ddkfm.plan4ba.capture
import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.*
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/reminders")
class ReminderController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allReminders(@QueryParam("userId") userId : Int) : List<Reminder> {
        val where = if(userId != -1) "user_id = $userId" else "1=1"
        return inSession { it.list<HibernateReminder>(where) }?.map { it.toModel() } ?: emptyList()
    }

    @GET
    @Path("/:id")
    fun getReminder(@PathParam("id") id : Int) : Reminder {
        var reminder = inSession { it.single<HibernateReminder>(id) }
        return reminder?.toModel() ?: throw NotFound()
    }

    @PUT
    @Path("")
    fun createResult(reminder: Reminder) : Reminder {
        val user = inSession { it.single<HibernateUser>(reminder.userId) }
                ?: throw BadRequest("user stat does not exist")

        val hibernateResult = reminder
            .toHibernate<HibernateReminder>()
            .apply {
                this.user = user
            }
        inSession { session ->
            session save hibernateResult
        }
        return hibernateResult.toModel()
    }

    @DELETE
    @Path("/:id")
    fun deleteReminder(@PathParam("id") reminderId : Int) : OK {
        val reminder = inSession { it.single<HibernateReminder>(reminderId) }
            ?: throw NotFound("reminder not found")
        val hqlScripts = listOf(
            "DELETE From HibernateUpcoming Where reminder_id = ${reminder.id}",
            "DELETE From HibernateLatestExamResult Where reminder_id = ${reminder.id}",
            "DELETE From HibernateReminder Where id = ${reminder.id}"
        )
        return inSession { session ->
            val transaction = session.beginTransaction()
            try {
                val sumRowsDeleted = hqlScripts
                    .map(session::createQuery)
                    .map { it.executeUpdate() }
                    .sum()
                OK("reminder was deleted")
            } catch(e : Exception) {
                transaction.rollback()
                SentryTurret.log {
                    addTag("Hibernate", "")
                }.capture(e)
                throw InternalServerError("reminder could not deleted")
            }
        } ?: throw InternalServerError("reminder could not deleted")
    }
}