package de.ddkfm.stpapp.controller

import de.ddkfm.stpapp.models.*
import de.ddkfm.stpapp.utils.HibernateUtils
import de.ddkfm.stpapp.utils.doInTransaction
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
        session.createQuery("From User", User::class.java).list()
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
            var user = session.find(User::class.java, id)
            user?.withoutPassword() ?: NotFound()
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
            var user = session.find(User::class.java, id)
            if(user == null)
                NotFound()
            else if(user.password == DigestUtils.sha512Hex(passwordParam.password))
                    user.withoutPassword()
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
            var alreadyUsers = session.createQuery("From User Where matriculationNumber = '${user.matriculationNumber}' " +
                    "OR username = '${user.username}'", User::class.java)
                    .list()
            if(alreadyUsers.size > 0)
                AlreadyExists("User already exists")
            else {
                session.beginTransaction()
                try {
                    session.persist(user.generatePasswordHash().cleanID())
                    session.transaction.commit()
                    Created(customMessage = "User created")
                } catch (e : Exception) {
                    e.printStackTrace()
                    session.transaction.rollback()
                    HttpStatus(500, "Could not save the user")
                }
            }
        }
    }

}