package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.AppVersion
import de.ddkfm.plan4ba.models.NotFound
import de.ddkfm.plan4ba.models.database.HibernateAppVersion
import de.ddkfm.plan4ba.utils.inSession
import de.ddkfm.plan4ba.utils.list
import de.ddkfm.plan4ba.utils.single
import spark.Request
import spark.Response
import javax.ws.rs.GET
import javax.ws.rs.Path
import javax.ws.rs.PathParam

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
}

