package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.*
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
    @Path("")
    fun allUsers() : Any? = HibernateUtils.doInHibernate { session ->
        val users = session.createQuery("From HibernateUser", HibernateUser::class.java)
                .list()
                .map { it.withoutPassword().toUser() }
        users
    }

    @GET
    @ApiOperation(value = "list all users by a given filter", notes = "return filtered users")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = User::class, responseContainer = "List")
    )
    @ApiImplicitParams(
            ApiImplicitParam(name = "field", paramType = "path", dataType = "string"),
            ApiImplicitParam(name = "operation", paramType = "path", dataType = "string"),
            ApiImplicitParam(name = "value", paramType = "path", dataType = "string")
    )
    @Path("/:field/:operation/:value")
    fun filteredUsers(@ApiParam(hidden = true) field : String,
                      @ApiParam(hidden = true) operation : String,
                      @ApiParam(hidden = true) value : String) : Any? {
        return HibernateUtils.doInHibernate {session ->
            val users = session.createQuery("From HibernateUser", HibernateUser::class.java)
                    .list()
                    .map { it.withoutPassword().toUser() }
            users
        }
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
            ApiResponse(code = 401, response = Unauthorized::class, message = "UNAUTHORIZED"),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    fun authenticate(@ApiParam passwordParam : PasswordParam,
                     @ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var user = session.find(HibernateUser::class.java, id)
            if(user == null)
                NotFound()
            else if(user.password == DigestUtils.sha512Hex(passwordParam.password))
                    user.withoutPassword().toUser()
            else
               Unauthorized()
        }
    }


    @PUT
    @ApiOperation(value = "create an user")
    @Path("")
    @ApiResponses(
            ApiResponse(code = 201, message = "User created", response = User::class),
            ApiResponse(code = 409, message = "User already exists", response = AlreadyExists::class),
            ApiResponse(code = 500, message = "Could not save the user", response = HttpStatus::class)
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
                    user
                } catch (e : Exception) {
                    e.printStackTrace()
                    session.transaction.rollback()
                    HttpStatus(500, "Could not save the user")
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
            ApiResponse(code = 500, message = "Could not edit the user", response = HttpStatus::class)
    )
    fun updateUser(@ApiParam user : User,
                   @ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            val existingUser = session.find(HibernateUser::class.java, id)
            if(existingUser == null)
                NotFound("User does not exist")
            existingUser.matriculationNumber = user.matriculationNumber
            existingUser.userHash = user.userHash
            existingUser.lastLogin = (user.lastLogin ?: 0L).toLocalDateTime()
            existingUser.userHash = user.userHash
            existingUser.hasUserSpecificCalendar = user.hasUserSpecificCalendar

            if(existingUser.group.id != user.groupId && user.groupId > 0) {
                val group = session.find(HibernateUserGroup::class.java, user.groupId)
                if(group == null)
                    BadRequest("Group with id ${user.id} does not exist")
            }
            try {
                session.doInTransaction {
                    it.persist(existingUser)
                }
                existingUser.toUser()
            } catch (e : Exception) {
                e.printStackTrace()
                HttpStatus(500, "Could not update the User")
            }
        }
    }
}