package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.*
import io.swagger.annotations.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Api(value = "/lectures", description = "all operations about lectures")
@Path("/lectures")
@Produces("application/json")
class LectureController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @ApiOperation(value = "list all lectures", notes = "return all lectures")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Lecture::class, responseContainer = "List")
    )
    @ApiImplicitParam(name = "userId", paramType = "query", dataType = "integer", required = false)
    @Path("")
    fun allLectures(@ApiParam(hidden = true) userId : Int) : Any? = HibernateUtils.doInHibernate { session ->
        var where = "WHERE 1=1"
        if(userId != -1)
            where += "AND user_id = $userId"
        val lectures = session.createQuery("From HibernateLecture $where", HibernateLecture::class.java)
                .list()
                .map { it.toLecture() }
        if(userId != -1) {
            val transaction = session.beginTransaction()
            try {
                val user = session.find(HibernateUser::class.java, userId)
                if(user != null) {
                    user.lastLectureCall = System.currentTimeMillis()
                    session.saveOrUpdate(user)
                }
                transaction.commit()
            } catch (e : Exception) {
                transaction.rollback()
                println("Fehler in allLectures: " + e)
            }
        }
        lectures
    }

    @DELETE
    @ApiOperation(value = "delete all lectures by userId")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = OK::class)
    )
    @ApiImplicitParam(name = "userId", paramType = "query", dataType = "integer")
    @Path("")
    fun deleteLectures(@ApiParam(hidden = true) userId : Int) : Any? = HibernateUtils.doInHibernate { session ->
        session.doInTransaction {
            val affectedRows = session.createQuery("Delete From HibernateLecture WHERE user_id = $userId")
                    .executeUpdate()
            OK("affected rows: $affectedRows")
        }

    }

    @GET
    @ApiOperation(value = "get a specific lecture", notes = "get a specific lecture")
    @ApiImplicitParams(
            ApiImplicitParam(name = "id", paramType = "path", dataType = "integer"),
            ApiImplicitParam(name = "userId", paramType = "query", dataType = "integer")
    )
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Lecture::class),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    @Path("/:id")
    fun getLecture(@ApiParam(hidden = true) id : Int,
                   @ApiParam(hidden = true) userId : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var lecture = session.find(HibernateLecture::class.java, id)
            if(lecture == null || (userId != -1 && lecture.user.id != userId))
                NotFound()
            else
                lecture.toLecture()
        }
    }

    @PUT
    @ApiOperation(value = "create a lecture")
    @Path("")
    @ApiResponses(
            ApiResponse(code = 201, message = "lecture created", response = Lecture::class),
            ApiResponse(code = 409, message = "lecture already exists", response = AlreadyExists::class),
            ApiResponse(code = 500, message = "Could not save the lecture", response = HttpStatus::class)
    )
    fun createLecture(@ApiParam lecture : Lecture) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var userSQL = ""
            if(lecture.userId != null && lecture.userId!! > 0) {
                val user = session.find(HibernateUser::class.java, lecture.userId)
                if(user != null) {
                    userSQL = "AND user_id = ${user.id}"
                }
            }

            var existingLecture = session.createQuery("From HibernateLecture l Where l.title = '${lecture.title}'" +
                    " AND l.start = ${lecture.start} AND l.end = ${lecture.end}" +
                    " $userSQL", HibernateLecture::class.java).uniqueResultOptional()


            if(existingLecture.isPresent)
                AlreadyExists("Lecture already exists")
            else {
                session.beginTransaction()
                try {

                    session.persist(lecture.toHibernateLecture())
                    session.transaction.commit()
                    var insertedLecture = session.createQuery("From HibernateLecture l Where l.title = '${lecture.title}'" +
                            " AND l.start = ${lecture.start} AND l.end = ${lecture.end}" +
                            " $userSQL", HibernateLecture::class.java).uniqueResult()
                    insertedLecture.toLecture()
                } catch (e : Exception) {
                    e.printStackTrace()
                    session.transaction.rollback()
                    HttpStatus(500, "Could not save the lecture")
                }
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
                e.printStackTrace()
                BadRequest("Could not update the lecture")
            }

        }
    }
}