package de.ddkfm.plan4ba

import com.fasterxml.jackson.databind.ObjectMapper
import de.ddkfm.plan4ba.controller.SwaggerParser
import de.ddkfm.plan4ba.models.BadRequest
import de.ddkfm.plan4ba.models.Config
import de.ddkfm.plan4ba.models.HttpStatus
import de.ddkfm.plan4ba.models.InternalServerError
import de.ddkfm.plan4ba.utils.HibernateUtils
import de.ddkfm.plan4ba.utils.getEnvOrDefault
import de.ddkfm.plan4ba.utils.mapDataTypes
import de.ddkfm.plan4ba.utils.toJson
import io.sentry.event.Event
import io.swagger.annotations.*
import org.apache.commons.io.IOUtils
import org.reflections.Reflections
import spark.Request
import spark.Response
import spark.Spark.*
import spark.debug.DebugScreen
import spark.kotlin.port
import java.lang.reflect.Method
import java.net.InetAddress
import javax.ws.rs.*

fun main(args : Array<String>) {
    port(System.getenv("HTTP_PORT")?.toIntOrNull() ?: 8080)

    var config = Config()
    config.buildFromEnv()
    println(config)
    HibernateUtils.setUp(config.database)
    var reflections = Reflections("de.ddkfm.plan4ba.controller")

    var controllers = reflections.getTypesAnnotatedWith(Api::class.java)
    for(controller in controllers) {
        path(controller.getAnnotation(Path::class.java).value) {
            var methods = controller.declaredMethods.filter { it.isAnnotationPresent(ApiOperation::class.java) }
            for(method in methods) {
                if(method.isAnnotationPresent(GET::class.java))
                    get(method.getAnnotation(Path::class.java).value) {req, resp -> invokeFunction(controller, method, req, resp)}

                if(method.isAnnotationPresent(POST::class.java))
                    post(method.getAnnotation(Path::class.java).value) {req, resp -> invokeFunction(controller, method, req, resp)}

                if(method.isAnnotationPresent(PUT::class.java))
                    put(method.getAnnotation(Path::class.java).value) {req, resp -> invokeFunction(controller, method, req, resp)}

                if(method.isAnnotationPresent(DELETE::class.java))
                    delete(method.getAnnotation(Path::class.java).value) {req, resp -> invokeFunction(controller, method, req, resp)}
            }
        }
    }
    if(getEnvOrDefault("ENABLE_SWAGGER", "false").toBoolean()) {
        var swaggerJson = SwaggerParser.getSwaggerJson("de.ddkfm.plan4ba.controller")

        get("/swagger") { _,_-> swaggerJson }

        get("/swagger/html") { _, resp ->
            IOUtils.copy(SwaggerParser.javaClass.getResourceAsStream("/index.html"), resp.raw().outputStream)
        }
    }
    DebugScreen.enableDebugScreen()
    val dsn = System.getenv("SENTRY_DSN")
    dsn?.let {
        println("DSN $dsn joined")
        SentryTurret.log {
            addTag("Service", "DBService")
        }.event {
            withMessage("DBService ${InetAddress.getLocalHost().hostName} joined")
            withLevel(Event.Level.INFO)
        }
    }
}

fun invokeFunction(controller : Class<*>, method : Method, req : Request, resp : Response) : Any {
    var instance = controller.getConstructor(Request::class.java, Response::class.java).newInstance(req, resp)
    resp.type("application/json")
    var args = mutableListOf<Any>()
    var bodyParam = method.parameters
            .filter { it.isAnnotationPresent(ApiParam::class.java) }
            .filter { !it.getAnnotation(ApiParam::class.java).hidden }
            .firstOrNull()
    var badRequest = false
    if(bodyParam != null) {
        if(req.body() == null) {
            badRequest = true
        }
        try {
            var bodyObject = jacksonObjectMapper().readValue(req.body(), bodyParam.type)
            args.add(bodyObject)
        } catch (e : Exception) {
            badRequest = true
        }
    }
    if(badRequest) {
        resp.status(400)
        return jacksonObjectMapper().writeValueAsString(BadRequest())
    } else {
        var implicitParams = method.annotations
                .filter { it is ApiImplicitParams || it is ApiImplicitParam }
                .flatMap {
                    if (it is ApiImplicitParams)
                        it.value.toList()
                    else
                            listOf(it)
                }
                .map { it as ApiImplicitParam }
                .map { param ->
                    var value =
                            if (param.paramType == "path") {
                                req.params(param.name)
                            } else {
                                req.queryParams(param.name)
                            } ?: ""
                    param to value
                }
                .filter { it.second != null }
                .map(::mapDataTypes)

        args.addAll(implicitParams)
        var invokeResult = try {
            method.invoke(instance, *args.toTypedArray())
        } catch (e : Exception) {
            SentryTurret.log {
                addExtra("params", implicitParams.toJson())
            }.capture(e)
            InternalServerError("a server error occured")
        }

        if (invokeResult is HttpStatus)
            resp.status(invokeResult.code)
        return jacksonObjectMapper().writeValueAsString(invokeResult)
    }
}

fun jacksonObjectMapper()  : ObjectMapper {
    var mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
    return mapper
}