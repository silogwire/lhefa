package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.SentryTurret
import de.ddkfm.plan4ba.capture
import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.user
import de.ddkfm.plan4ba.utils.*
import io.swagger.annotations.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Api(value = "/examstats", description = "all operations abount the examstats of a user")
@Path("/examstats")
@Produces("application/json")
class ExamStatsController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @ApiOperation(value = "list all examstats", notes = "return all examstats")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = ExamStats::class, responseContainer = "List")
    )
    @ApiImplicitParam(name = "userId", paramType = "query", dataType = "integer", required = false)
    @Path("")
    fun allLectures(@ApiParam(hidden = true) userId : Int) : Any? = HibernateUtils.doInHibernate { session ->
        var where = "WHERE 1=1"
        if(userId != -1)
            where += "AND user_id = $userId"
        val stats = session.createQuery("From HibernateExamStats $where", HibernateExamStats::class.java)
                .list()
                .map { it.toExamStats() }
        stats
    }

    @DELETE
    @ApiOperation(value = "delete all examstats by userId")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = OK::class)
    )
    @ApiImplicitParam(name = "userId", paramType = "query", dataType = "integer")
    @Path("")
    fun deleteLectures(@ApiParam(hidden = true) userId : Int) : Any? = HibernateUtils.doInHibernate { session ->
        session.doInTransaction {
            val affectedRows = session.createQuery("Delete From HibernateExamStats WHERE user_id = $userId")
                    .executeUpdate()
            OK("affected rows: $affectedRows")
        }

    }

    @GET
    @ApiOperation(value = "get a specific exam stat", notes = "get a specific exam stat")
    @ApiImplicitParams(
            ApiImplicitParam(name = "id", paramType = "path", dataType = "integer"),
            ApiImplicitParam(name = "userId", paramType = "query", dataType = "integer")
    )
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = ExamStats::class),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    @Path("/:id")
    fun getLecture(@ApiParam(hidden = true) id : Int,
                   @ApiParam(hidden = true) userId : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var examstat = session.find(HibernateExamStats::class.java, id)
            if(examstat == null || (userId != -1 && examstat.user.id != userId))
                NotFound()
            else
                examstat.toExamStats()
        }
    }

    @PUT
    @ApiOperation(value = "create a exam stat")
    @Path("")
    @ApiResponses(
            ApiResponse(code = 201, message = "exam stat created", response = ExamStats::class),
            ApiResponse(code = 409, message = "exam stat already exists", response = AlreadyExists::class),
            ApiResponse(code = 500, message = "Could not save the exam stat", response = HttpStatus::class)
    )
    fun createLecture(@ApiParam examStat : ExamStats) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var userSQL = ""
            if(examStat.userId != null && examStat.userId!! > 0) {
                val user = session.find(HibernateExamStats::class.java, examStat.userId)
                if(user != null) {
                    userSQL = "AND user_id = ${user.id}"
                }
            }
            session.beginTransaction()
            try {

                session.persist(examStat.toHibernateExamStat())
                session.transaction.commit()
                /*
                var insertedLecture = session.createQuery("From HibernateLecture l Where l.title = '${lecture.title}'" +
                        " AND l.start = ${lecture.start} AND l.end = ${lecture.end}" +
                        " $userSQL", HibernateLecture::class.java).uniqueResult()
                insertedLecture.toLecture()
                */
            } catch (e : Exception) {
                SentryTurret.log {
                    addTag("Hibernate", "")
                    addTag("createLecture", "")
                    user(username = examStat.userId.toString())
                }
                session.transaction.rollback()
                HttpStatus(500, "Could not save the lecture")
            }
        }
    }

    @POST
    @ApiOperation(value = "updates an Lecture")
    @Path("/:id")
    @ApiImplicitParams(
            ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    )
    @ApiResponses(
            ApiResponse(code = 200, message = "lecture updated", response = Lecture::class),
            ApiResponse(code = 500, message = "Could not edit the Lecture", response = HttpStatus::class)
    )
    fun updateLecture(@ApiParam lecture : Lecture,
                      @ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            val existingLecture = session.find(HibernateLecture::class.java, id)
            if(existingLecture == null)
                NotFound("Lecture does not exist")

            existingLecture.allDay = lecture.allDay
            existingLecture.color = lecture.color
            existingLecture.description = lecture.description
            existingLecture.start = lecture.start
            existingLecture.end = lecture.end
            existingLecture.exam = lecture.exam
            existingLecture.instructor = lecture.instructor
            existingLecture.remarks = lecture.remarks
            existingLecture.room = lecture.room
            existingLecture.sroom = lecture.sroom
            existingLecture.title = lecture.title
            if(lecture.userId != null || lecture.userId!! > 0) {
                val user = session.find(HibernateUser::class.java, lecture.userId)
                existingLecture.user = user
            }
            try {
                session.persist(existingLecture)
                existingLecture.toLecture()
            } catch (e : Exception) {
                SentryTurret.log {
                    addTag("Hibernate", "")
                    addTag("updateLecture", "")
                    user(username = lecture.userId.toString())
                }.capture(e)
                BadRequest("Could not update the lecture")
            }

        }
    }
}