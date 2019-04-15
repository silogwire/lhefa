package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
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
            val lectures = session.list<HibernateLecture>(where)?.map { it.toLecture() }
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
            lecture.toLecture()
    }

    @PUT
    @Path("")
    fun createLecture(lecture : Lecture) : Lecture? {
        val existingLecture = inSession { session ->
            session.list<HibernateLecture>(Where.and()
                .add("title" eq "'${lecture.title}'")
                .add("start" eq "${lecture.start}")
                .add("end" eq "${lecture.end}")
                .add("user.id" eq lecture.userId)
            )
        }?.firstOrNull()
        if(existingLecture != null)
            throw AlreadyExists("Lecture already exists")
        else {
            inSession {session ->
                session save lecture.toHibernateLecture()
            }
            val insertedLecture = inSession { it.list<HibernateLecture>(Where.and()
                .add("title" eq "'${lecture.title}'")
                .add("start" eq "${lecture.start}")
                .add("end" eq "${lecture.end}")
                //.add("user_id" eq lecture.userId)
            )
            }?.firstOrNull()
            if(insertedLecture == null)
                throw InternalServerError("could not save the lecture")
            return insertedLecture?.toLecture()
        }
    }

    @POST
    @Path("/:id")
    fun updateLecture(lecture : Lecture,
                      @PathParam("id") id : Int) : Lecture? {
        val existingLecture = inSession { it.single<HibernateLecture>(id) }
        if(existingLecture == null)
            throw NotFound("Lecture does not exist")
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

        val user = inSession { it.single<HibernateUser>(lecture.userId) }
        if(user == null)
            throw NotFound("user with id ${lecture.userId} does not exist")
        existingLecture.user = user

        inSession { session ->
            session update existingLecture
        }
        return existingLecture.toLecture()
    }
}