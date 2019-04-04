package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.SentryTurret
import de.ddkfm.plan4ba.capture
import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.*
import io.swagger.annotations.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Api(value = "/groups", description = "all operations about groups")
@Path("/groups")
@Produces("application/json")
class UserGroupController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @ApiOperation(value = "list all groups", notes = "return all groups")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = UserGroup::class, responseContainer = "List")
    )
    @ApiImplicitParam(name = "uid", paramType = "query", required = false)
    @Path("")
    fun allGroups(@ApiParam(hidden = true) uid : String) : Any? = HibernateUtils.doInHibernate { session ->
        var where = "WHERE 1=1"
        if(!uid.isEmpty())
            where += "AND uid = '$uid'"
        val groups = session.createQuery("From HibernateUserGroup $where", HibernateUserGroup::class.java)
                .list()
                .map { it.toUserGroup() }
        groups
    }

    @GET
    @ApiOperation(value = "get a specific group", notes = "get a specific group")
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = UserGroup::class),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    @Path("/:id")
    fun getGroup(@ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var group = session.find(HibernateUserGroup::class.java, id)
            group?.toUserGroup() ?: NotFound()
        }
    }

    @PUT
    @ApiOperation(value = "create a group")
    @Path("")
    @ApiResponses(
            ApiResponse(code = 201, message = "Group created", response = UserGroup::class),
            ApiResponse(code = 409, message = "Group already exists", response = AlreadyExists::class),
            ApiResponse(code = 500, message = "Could not save the group", response = HttpStatus::class)
    )
    fun createGroup(@ApiParam group : UserGroup) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var alreadyGroups = session.createQuery("From HibernateUserGroup Where uid = '${group.uid}'",
                    HibernateUserGroup::class.java)
                    .list()

            if(alreadyGroups.size > 0)
                AlreadyExists("Group already exists")
            else {
                val university = session.find(HibernateUniversity::class.java, group.universityId)
                if(university == null)
                    BadRequest("University does not exist")
                val hibernateGroup = HibernateUserGroup(0, group.uid, university)
                try {
                    session.doInTransaction {
                        it.persist(hibernateGroup)
                    }
                    hibernateGroup.toUserGroup()
                } catch (e : Exception) {
                    SentryTurret.log {
                        addTag("Hibernate", "")
                        addTag("createGroup", "")
                    }.capture(e)
                    HttpStatus(500, "Could not save the group")
                }
            }
        }
    }

    @POST
    @ApiOperation(value = "updates an group")
    @Path("/:id")
    @ApiImplicitParams(
            ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    )
    @ApiResponses(
            ApiResponse(code = 200, message = "Group updated", response = UserGroup::class),
            ApiResponse(code = 500, message = "Could not edit the Group", response = HttpStatus::class)
    )
    fun updateGroup(@ApiParam group : UserGroup,
                   @ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate returnValue@{ session ->
            val existingGroup = session.find(HibernateUserGroup::class.java, id)
                    ?: return@returnValue NotFound("Group does not exist")

            existingGroup.uid = group.uid
            if(group.universityId != existingGroup.university.id && group.universityId > 0) {
                val university = session.find(HibernateUniversity::class.java, group.universityId)
                        ?: return@returnValue BadRequest("University with id ${group.universityId} does not exist")
                existingGroup.university = university
            }

            try {
                session.doInTransaction {
                    it.persist(existingGroup)
                }
                existingGroup.toUserGroup()
            } catch (e: Exception) {
                SentryTurret.log {
                    addTag("Hibernate", "")
                    addTag("updateGroup", "")
                }.capture(e)
                HttpStatus(500, "Could not update the Group")
            }
        }
    }
}