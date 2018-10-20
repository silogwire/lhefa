package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.*
import io.swagger.annotations.*
import org.hibernate.Hibernate
import spark.Request
import spark.Response
import java.util.stream.Collectors
import javax.ws.rs.*

@Api(value = "/tokens", description = "all operations about tokens")
@Path("/tokens")
@Produces("application/json")
class TokenController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @ApiOperation(value = "list all tokens", notes = "return all tokens")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Token::class, responseContainer = "List")
    )
    @ApiImplicitParams(
            ApiImplicitParam(name = "userId", paramType = "query", dataType = "integer", required = false),
            ApiImplicitParam(name = "valid", paramType = "query", dataType = "boolean", required = false),
            ApiImplicitParam(name = "caldavToken", paramType = "query", dataType = "boolean", required = false)
    )

    @Path("")
    fun allTokens(@ApiParam(hidden = true) userId : Int,
                  @ApiParam(hidden = true) valid : Boolean,
                  @ApiParam(hidden = true) caldavToken : Boolean) : Any? = HibernateUtils.doInHibernate { session ->
        var where = "WHERE 1=1"
        if(userId != -1)
            where += "AND user_id = $userId"
        if(valid)
            where += "AND validTo > ${System.currentTimeMillis()}"
        if(caldavToken)
            where += "AND isCalDavToken = $caldavToken"
        session.createQuery("From HibernateToken $where", HibernateToken::class.java).list()
                .map { it.toToken() }
    }

    @GET
    @ApiOperation(value = "get a specific token", notes = "get a specific token")
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "string")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Token::class),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    @Path("/:id")
    fun getToken(@ApiParam(hidden = true) id : String) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var token = session.createQuery("From HibernateToken Where token = '$id'", HibernateToken::class.java).uniqueResultOptional()
            if(token.isPresent)
                token.get().toToken()
            else
                NotFound()

        }
    }

    @PUT
    @ApiOperation(value = "create a token")
    @Path("")
    @ApiResponses(
            ApiResponse(code = 201, message = "token created", response = Token::class),
            ApiResponse(code = 409, message = "token already exists", response = AlreadyExists::class),
            ApiResponse(code = 500, message = "Could not save the university", response = HttpStatus::class)
    )
    fun createToken(@ApiParam token : Token) : Any? {
        return HibernateUtils.doInHibernate { session ->
            val existingToken = session.find(HibernateToken::class.java, token.token)

            if(existingToken != null)
                AlreadyExists("token already exists")
            else {

                val user = session.find(HibernateUser::class.java, token.userId)
                if(user == null)
                    BadRequest("user does not exist")
                else {
                    val hibernateToken = HibernateToken(token.token, user, token.isCalDavToken, token.validTo)
                    try {
                        session.doInTransaction {
                            session.persist(hibernateToken)
                            hibernateToken.toToken()
                        }
                    } catch (e : Exception) {
                        e.printStackTrace()
                        HttpStatus(500, "Could not save the token")
                    }
                }
            }
        }
    }
}