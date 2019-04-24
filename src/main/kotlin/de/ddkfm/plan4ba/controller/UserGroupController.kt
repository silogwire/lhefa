package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.SentryTurret
import de.ddkfm.plan4ba.capture
import de.ddkfm.plan4ba.models.*
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
        return groups?.map { it.toUserGroup() } ?: emptyList()
    }

    @GET
    @Path("/:id")
    fun getGroup(@PathParam("id") id: Int): UserGroup {
        var group = inSession { it.single<HibernateUserGroup>(id) }
        return group?.toUserGroup() ?: throw NotFound()
    }

    @PUT
    @Path("")
    fun createGroup(group: UserGroup): UserGroup {
        val existingGroups = inSession { it.list<HibernateUserGroup>("uid = '${group.uid}'") }
        if (existingGroups == null || existingGroups.isNotEmpty())
            throw AlreadyExists("Group already exists")
        inSession { session ->
            session save group.toHibernateUserGroup()
        }
        return group
    }
}