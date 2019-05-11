package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.HibernateLecture
import de.ddkfm.plan4ba.models.database.HibernateUser
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/lectures")
class LectureController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allLectures(@QueryParam("userId") userId : Int) : List<Lecture>? {
        return HibernateUtils.doInHibernate { session ->
            var where = "1=1"
            if(userId != -1)
                where += "AND user_id = $userId"
            val lectures = session.list<HibernateLecture>(where)?.map { it.toModel() }
            if(userId != -1) {
                val user = session.single<HibernateUser>(userId)
                if(user != null) {
                    user.lastLectureCall = System.currentTimeMillis()
                    session save user
                }
            }
            return@doInHibernate lectures
        }
    }

    @DELETE
    @Path("")
    fun deleteLectures(@QueryParam("userId") userId : Int) : OK? {
        return HibernateUtils.doInHibernate { session ->
            session.transaction {
                val affectedRows = session.createQuery("Delete From HibernateLecture WHERE user_id = $userId")
                    .executeUpdate()
                OK("affected rows: $affectedRows")
            }
        }
    }

    @GET
    @Path("/:id")
    fun getLecture(@PathParam("id") id : Int,
                   @QueryParam("userId") userId : Int) : Lecture? {
        var lecture = inSession { it.single<HibernateLecture>(id) }
        return if(lecture == null || (userId != -1 && lecture.user.id != userId))
            throw NotFound()
        else
            lecture.toModel()
    }

    @PUT
    @Path("")
    fun createLecture(lecture : Lecture) : Lecture? {
        val hibernateLecture = lecture.toHibernate<HibernateLecture>()
        inSession {session ->
            session save hibernateLecture
        }
        return hibernateLecture.toModel()
    }

    @POST
    @Path("/:id")
    fun updateLecture(lecture : Lecture,
                      @PathParam("id") id : Int) : Lecture? {
        val existingLecture = inSession { it.single<HibernateLecture>(id) }
        if(existingLecture == null)
            throw NotFound("Lecture does not exist")
        existingLecture.apply {
            allDay = lecture.allDay
            color = lecture.color
            description = lecture.description
            start = lecture.start
            end = lecture.end
            exam = lecture.exam
            instructor = lecture.instructor
            remarks = lecture.remarks
            room = lecture.room
            sroom = lecture.sroom
            title = lecture.title
            deprecated = lecture.deprecated
        }

        val user = inSession { it.single<HibernateUser>(lecture.userId) }
            ?: throw NotFound("user with id ${lecture.userId} does not exist")
        existingLecture.user = user

        inSession { session ->
            session update existingLecture
        }
        return existingLecture.toModel()
    }
}