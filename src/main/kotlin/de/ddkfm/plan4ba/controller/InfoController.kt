package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.HibernateUtils
import io.swagger.annotations.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Api(value = "/info", description = "all operations about info texts")
@Path("/info")
@Produces("application/json")
class InfoController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @ApiOperation(value = "list all infotexts", notes = "return all infotexts")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Infotext::class, responseContainer = "List")
    )
    @ApiImplicitParam(name = "key", paramType = "query", required = false)
    @Path("")
    fun getAllInfos(@ApiParam(hidden = true) key : String) : Any? = HibernateUtils.doInHibernate { session ->
        val where = if(!key.isNullOrEmpty()) "Where key = '$key'" else ""
        val texts = session.createQuery("From HibernateInfotext $where", HibernateInfotext::class.java)
                .list()
                .map(HibernateInfotext::toInfotext)
        texts
    }

    @GET
    @ApiOperation(value = "get a specific infotext", notes = "get a specific infotext")
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Infotext::class),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    @Path("/:id")
    fun getInfo(@ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var uni = session.find(HibernateInfotext::class.java, id)
            uni?.toInfotext() ?: NotFound()
        }
    }
}

