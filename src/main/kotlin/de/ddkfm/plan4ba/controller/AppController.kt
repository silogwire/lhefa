package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.HibernateAppChange
import de.ddkfm.plan4ba.models.database.HibernateAppVersion
import de.ddkfm.plan4ba.models.database.HibernateInfotext
import de.ddkfm.plan4ba.utils.HibernateUtils
import de.ddkfm.plan4ba.utils.inSession
import de.ddkfm.plan4ba.utils.single
import de.ddkfm.plan4ba.utils.list
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/app")
class AppController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allAppVersions() : List<AppVersion> {
        val versions = inSession { it.list<HibernateAppVersion>() } ?: emptyList()
        return versions.map { it.toModel() }
    }

    @GET
    @Path("/:id")
    fun getVersion(@PathParam("id") id : Int) : AppVersion {
        return inSession { it.single<HibernateAppVersion>(id) }
            ?.toModel()
            ?: throw NotFound()
    }

    @GET
    @Path("/:id/changes")
    fun getAppChanges(@PathParam("id") id : Int) : List<AppChange> {
        return inSession { it.list<HibernateAppChange>("appVersion = $id") }
            ?.map { it.toModel() }
            ?: emptyList()
    }
}

