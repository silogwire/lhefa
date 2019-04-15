package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Path("/tokens")
class TokenController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @Path("")
    fun allTokens(@QueryParam("userId") userId : Int,
                  @QueryParam("valid") valid : Boolean,
                  @QueryParam("caldavToken") caldavToken : Boolean,
                  @QueryParam("refreshToken") refreshToken : Boolean) : List<Token>? {
        var where = Where.and()
            .add("isCalDavToken" eq caldavToken)
            .add("isRefreshToken" eq refreshToken)
        if(userId != -1)
            where = where.add("user.id" eq userId)
        if(valid)
            where = where.add("validTo" gt System.currentTimeMillis())
        return inSession { it.list<HibernateToken>(where) }?.map { it.toToken() }
    }

    @GET
    @Path("/:id")
    fun getToken(@PathParam("id") id : String) : Token? {
        val token = inSession { it.list<HibernateToken>("token='$id'") }?.firstOrNull()
        if(token == null)
            throw NotFound()
        return token.toToken()
    }

    @PUT
    @Path("")
    fun createToken(token : Token) : Token? {
        val existingToken = inSession { it.list<HibernateToken>("token='${token.token}'") }?.firstOrNull()
        if(existingToken != null)
            throw AlreadyExists("token already exists")
        val user = inSession { it.single<HibernateUser>(token.userId) }
        if(user == null)
            throw BadRequest("user does not exist")
        val hibernateToken = HibernateToken(token.token, user, token.isCalDavToken, token.isRefreshToken, token.validTo)
        inSession { session -> session save hibernateToken }
        return hibernateToken.toToken()
    }
}