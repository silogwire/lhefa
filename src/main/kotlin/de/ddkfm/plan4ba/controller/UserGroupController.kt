package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.HibernateUserGroup
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/groups")
class UserGroupController(req: Request, resp: Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allGroups(
        @QueryParam("uid") uid: String
    ): List<UserGroup> {
        val uidString = if(uid.isNotEmpty()) "uid = '$uid'" else "1=1"
        val groups = inSession { it.list<HibernateUserGroup>(uidString) }
        return groups?.map { it.toModel() } ?: emptyList()
    }

    @GET
    @Path("/:id")
    fun getGroup(@PathParam("id") id: Int): UserGroup {
        val group = inSession { it.single<HibernateUserGroup>(id) }
        return group?.toModel() ?: throw NotFound()
    }

    @PUT
    @Path("")
    fun createGroup(group: UserGroup): UserGroup {
        val existingGroups = inSession { it.list<HibernateUserGroup>("uid = '${group.uid}'") }
        if (existingGroups == null || existingGroups.isNotEmpty())
            throw AlreadyExists("Group already exists")
        inSession { session ->
            session save group.toHibernate<HibernateUserGroup>()
        }
        return group
    }
}