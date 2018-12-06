package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.HibernateUtils
import de.ddkfm.plan4ba.utils.doInTransaction
import de.ddkfm.plan4ba.utils.toHibernateUser
import io.swagger.annotations.*
import org.apache.commons.codec.digest.DigestUtils
import spark.Request
import spark.Response
import javax.ws.rs.*

@Api(value = "/users", description = "all operations about users")
@Path("/users")
@Produces("application/json")
class UserController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @ApiOperation(value = "list all users", notes = "return all users")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = User::class, responseContainer = "List")
    )
    @ApiImplicitParam(name = "matriculationNumber", paramType = "query", required = false)
    @Path("")
    fun allUsers(@ApiParam(hidden = true) matriculationNumber : String) : Any? = HibernateUtils.doInHibernate { session ->
        var where = "WHERE 1=1"
        if(!matriculationNumber.isEmpty())
            where += "AND matriculationNumber = '$matriculationNumber'"
        session.createQuery("From HibernateUser $where", HibernateUser::class.java)
                .list()
                .map { it.withoutPassword().toUser() }
    }

    @GET
    @ApiOperation(value = "get a specific user", notes = "get a specific user")
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = User::class),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    @Path("/:id")
    fun getUser(@ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var user = session.find(HibernateUser::class.java, id)
            user?.withoutPassword()?.toUser() ?: NotFound()
        }
    }

    @POST
    @ApiOperation(value = "authenticate User", notes = "authenticate users against his internal passwordhash")
    @Path("/:id/authenticate")
    @ApiImplicitParams(
            ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    )
    @ApiResponses(
            ApiResponse(code = 200, message = "login successfull", response = User::class),
            ApiResponse(code = 401, response = Unauthorized::class, message = "Unauthorized"),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    fun authenticate(@ApiParam passwordParam : PasswordParam,
                     @ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var user = session.find(HibernateUser::class.java, id)
            when {
                user == null -> NotFound()
                user.password == DigestUtils.sha512Hex(passwordParam.password) -> user.withoutPassword().toUser()
                else -> Unauthorized()
            }
        }
    }


    @PUT
    @ApiOperation(value = "create an user")
    @Path("")
    @ApiResponses(
            ApiResponse(code = 201, message = "User created", response = User::class),
            ApiResponse(code = 409, message = "User already exists", response = AlreadyExists::class),
            ApiResponse(code = 500, message = "Could not save the user", response = InternalServerError::class)
    )
    fun createUser(@ApiParam user : User) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var alreadyUsers = session.createQuery("From HibernateUser Where matriculationNumber = '${user.matriculationNumber}'", HibernateUser::class.java)
                    .list()

            if(alreadyUsers.size > 0)
                AlreadyExists("User already exists")
            else {
                session.beginTransaction()
                try {

                    session.persist(user.toHibernateUser().generatePasswordHash().cleanID())
                    session.transaction.commit()
                    session.createQuery("From HibernateUser Where matriculationNumber = '${user.matriculationNumber}'", HibernateUser::class.java).list()
                            .first().toUser()
                } catch (e : Exception) {
                    e.printStackTrace()
                    session.transaction.rollback()
                    InternalServerError("Could not save the user")
                }
            }
        }
    }

    @POST
    @ApiOperation(value = "updates an user")
    @Path("/:id")
    @ApiImplicitParams(
            ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    )
    @ApiResponses(
            ApiResponse(code = 200, message = "User updated", response = User::class),
            ApiResponse(code = 400, message = "Group with id {integer} does not exist", response = BadRequest::class),
            ApiResponse(code = 404, message = "User does not exist", response = NotFound::class),
            ApiResponse(code = 500, message = "Could not update the user", response = InternalServerError::class)
    )
    fun updateUser(@ApiParam user : User,
                   @ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate returnValue@{ session ->
            val existingUser = session.find(HibernateUser::class.java, id)
                    ?: return@returnValue NotFound("User does not exist")

            existingUser.matriculationNumber = user.matriculationNumber
            existingUser.userHash = user.userHash
            existingUser.userHash = user.userHash
            if(!user.password.isNullOrEmpty())
                existingUser.password = DigestUtils.sha512Hex(user.password)
            existingUser.lastLectureCall = user.lastLectureCall
            existingUser.lastLecturePolling = user.lastLecturePolling

            if(existingUser.group.id != user.groupId && user.groupId > 0) {
                val group = session.find(HibernateUserGroup::class.java, user.groupId)
                        ?: return@returnValue BadRequest("Group with id ${user.id} does not exist")
            }
            try {
                session.doInTransaction {
                    it.persist(existingUser)
                }
                existingUser.toUser()
            } catch (e : Exception) {
                e.printStackTrace()
                InternalServerError("Could not update the User")
            }
        }
    }

    @DELETE
    @ApiOperation(value = "delete any user data")
    @Path("/:id")
    @ApiImplicitParams(
            ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    )
    @ApiResponses(
            ApiResponse(code = 200, message = "User updated", response = OK::class),
            ApiResponse(code = 400, message = "Group with id {integer} does not exist", response = BadRequest::class),
            ApiResponse(code = 404, message = "User does not exist", response = NotFound::class),
            ApiResponse(code = 500, message = "Could not delete userdata", response = InternalServerError::class)
    )
    fun deleteUserData(@ApiParam passwordParam : PasswordParam,
                       @ApiParam(hidden = true) id : Int) : Any? {
        val authenticated = this.authenticate(passwordParam, id)
        if(authenticated is User) {
            val hqlScripts = listOf(
                    "DELETE From HibernateToken Where user_id = $id",
                    "DELETE From HibernateLecture Where user_id = $id",
                    "DELETE From HibernateUser Where id = $id"
            )
            return HibernateUtils.doInHibernate { session ->
                session.doInTransaction {trans ->
                    val sumRowsDeleted = hqlScripts
                            .map(trans::createQuery)
                            .map { it.executeUpdate() }
                            .sum()
                    OK("userdata was deleted")
                }
            }
        } else {
            return authenticated
        }
    }



    @GET
    @ApiOperation(value = "list all links by a given user", notes = "return all links from group and university")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Link::class, responseContainer = "List"),
            ApiResponse(code = 404, response = NotFound::class, message = "University Not Found")
    )
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @Path("/:id/links")
    fun getLinksByGroupAndUniversity (@ApiParam(hidden = true) id : Int) : Any? = HibernateUtils.doInHibernate { session ->
        val user = session.find(HibernateUser::class.java, id)
        if(user == null)
            NotFound()
        else {
            val group = user.group
            val university = group.university
            val links = session.createQuery("From HibernateLink Where university_id = ${university.id} OR group_id = ${group.id}", HibernateLink::class.java).list()
            links.map { it.toLink() }
        }
    }
}