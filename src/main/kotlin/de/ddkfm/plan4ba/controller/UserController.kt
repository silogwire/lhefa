package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.*
import org.apache.commons.codec.digest.DigestUtils
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/users")
class UserController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allUsers(@QueryParam("matriculationNumber") matriculationNumber : String) : List<User>? {
        val where = if(!matriculationNumber.isEmpty())
            "matriculationNumber = '$matriculationNumber'"
        else
            "1=1"
        return inSession { it.list<HibernateUser>(where) }?.map { it.withoutPassword().toUser() }
    }

    @GET
    @Path("/:id")
    fun getUser(@PathParam("id") id : Int) : User {
        val user = inSession { it.single<HibernateUser>(id) } ?: throw NotFound()
        return user.withoutPassword().toUser()
    }

    @POST
    @Path("/:id/authenticate")
    fun authenticate(passwordParam : PasswordParam,
                     @PathParam("id") id : Int) : User? {
        val user = inSession { it.single<HibernateUser>(id) } ?: throw NotFound()
        if(user.password == DigestUtils.sha512Hex(passwordParam.password))
            return user.withoutPassword().toUser()
        else
            throw Unauthorized()
    }


    @PUT
    @Path("")
    fun createUser(user : User) : User {
        val alreadyExists = inSession { it.list<HibernateUser>("matriculationNumber = '${user.matriculationNumber}'") }?.firstOrNull()
        if(alreadyExists != null)
            throw AlreadyExists("User already exists")
        val hibernateUser = user.toHibernateUser().generatePasswordHash().cleanID()
        inSession { session ->
            session save hibernateUser
        }
        return hibernateUser.withoutPassword().toUser();
    }

    @POST
    @Path("/:id")
    fun updateUser(user : User,
                   @PathParam("id") id : Int) : User {
        val existingUser = inSession { it.single<HibernateUser>(id) } ?: throw NotFound("User does not exist")

        existingUser.matriculationNumber = user.matriculationNumber
        existingUser.userHash = user.userHash
        existingUser.userHash = user.userHash
        if(!user.password.isEmpty())
            existingUser.password = DigestUtils.sha512Hex(user.password)
        existingUser.lastLectureCall = user.lastLectureCall
        existingUser.lastLecturePolling = user.lastLecturePolling

        if(existingUser.group.id != user.groupId && user.groupId > 0) {
            val group = inSession { it.single<HibernateUserGroup>(user.groupId) } ?: throw BadRequest("Group with id ${user.id} does not exist")
            existingUser.group = group
        }
        inSession { session ->
            session update existingUser
        }
        return existingUser.toUser()
    }

    @DELETE
    @Path("/:id")
    fun deleteUserData(passwordParam : PasswordParam,
                       @PathParam("id") id : Int) : OK {
         val authenticated = this.authenticate(passwordParam, id)
         if(authenticated == null)
             throw Unauthorized()
        val hqlScripts = listOf(
            "DELETE From HibernateNotification Where user_id = $id",
            "DELETE From HibernateToken Where user_id = $id",
            "DELETE From HibernateLecture Where user_id = $id",
            "DELETE From HibernateExamStats Where id = $id",
            "DELETE From HibernateUser Where id = $id"
        )
        return inSession {
            it.transaction { session ->
                val sumRowsDeleted = hqlScripts
                    .map(session::createQuery)
                    .map { it.executeUpdate() }
                    .sum()
                OK("userdata was deleted")
            }
        } ?: throw InternalServerError("userdata could not deleted")
    }



    @GET
    @Path("/:id/links")
    fun getLinksByGroupAndUniversity (@PathParam("id") id : Int) : List<Link> {

        val user = inSession { it.single<HibernateUser>(id) } ?: throw NotFound()
        val group = user.group
        val university = group.university
        val links = inSession { it.list<HibernateLink>("university_id = ${university.id} OR group_id = ${group.id}") }
        return links?.map { it.toLink() } ?: emptyList()
    }
}