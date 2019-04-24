package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.HibernateUtils
import de.ddkfm.plan4ba.utils.single
import de.ddkfm.plan4ba.utils.list
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/info")
class InfoController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun getAllInfos(@QueryParam("key") key : String) : List<Infotext> {
            val texts = HibernateUtils.doInHibernate { session ->
            val where = if(key.isNotEmpty()) "key = '$key'" else "1=1"
            val texts = session.list<HibernateInfotext>(where)
                ?.map { it.toInfotext() }
            texts
        }
        return texts ?: emptyList()
    }

    @GET
    @Path("/:id")
    fun getInfo(@PathParam("id") id : Int) : Infotext? {
        return HibernateUtils.doInHibernate { session ->
            val info = session.single<HibernateInfotext>(id)
            info?.toInfotext()
        } ?: throw NotFound()
    }
}

