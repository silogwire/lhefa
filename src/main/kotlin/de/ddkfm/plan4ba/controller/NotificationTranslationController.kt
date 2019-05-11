package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.models.database.*
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/translations")
class NotificationTranslationController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allTranslations(@QueryParam("type") type: String, @QueryParam("language") language : String): List<NotificationTranslation> {
        var where = "1=1"
        if (type.isNotEmpty())
            where += "AND type = '$type'"
        if(language.isNotEmpty())
            where += "AND language = '$language'"
        val translations = inSession { it.list<HibernateNotificationTranslation>(where)?.map { it.toModel() } }
        return translations ?: emptyList()
    }

    @GET
    @Path("/:id")
    fun getNotifcationTranslation(@PathParam("id") id: Int): NotificationTranslation {
        val translation = inSession { it.single<HibernateNotificationTranslation>(id) }
            ?: throw NotFound("translation does not exist")
        return translation.toModel()
    }
}