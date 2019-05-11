package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.SentryTurret
import de.ddkfm.plan4ba.capture
import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.HibernateExamStat
import de.ddkfm.plan4ba.models.database.HibernateNotification
import de.ddkfm.plan4ba.models.database.HibernateUser
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/examstats")
class ExamStatController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allExams(@QueryParam("userId") userId : Int) : List<ExamStat>? {
        val where = if(userId != -1) "user_id = $userId" else "1=1"
        return inSession { it.list<HibernateExamStat>(where) }?.map { it.toModel() }
    }

    @GET
    @Path("/:id")
    fun getExam(@PathParam("id") id : Int) : ExamStat {
        val examStat = inSession { it.single<HibernateExamStat>(id) }
        return examStat?.toModel() ?: throw NotFound()
    }

    @PUT
    @Path("")
    fun createExamStat(examstat : ExamStat) : ExamStat {
        val user = inSession { it.single<HibernateUser>(examstat.userId) }
                ?: throw BadRequest("user does not exist")

        val hibernateExamStat = examstat
            .toHibernate<HibernateExamStat>()
            .apply {
                this.user = user
            }
        inSession { session ->
            session save hibernateExamStat
        }
        return hibernateExamStat.toModel()
    }

    @DELETE
    @Path("/:id")
    fun deleteExamStat(@PathParam("id") id: Int) : OK {
        if(id <= 0)
            throw BadRequest("id is required to delete exam stats")
        return inSession {session ->
            val transaction = session.beginTransaction()
            try {
                val affectedRows = session.createQuery("Delete From HibernateExamStat WHERE id = $id")
                    .executeUpdate()
                OK("affected rows: $affectedRows")
            } catch (e : Exception) {
                transaction.rollback()
                SentryTurret.log {
                    addTag("Hibernate", "")
                }.capture(e)
                throw InternalServerError("exam stat could not deleted")
            }

        } ?: throw InternalServerError("could not delete the examstat")
    }
}