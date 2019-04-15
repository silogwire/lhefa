package de.ddkfm.plan4ba

import com.fasterxml.jackson.databind.ObjectMapper
import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.HibernateUtils
import de.ddkfm.plan4ba.utils.mapDataTypes
import de.ddkfm.plan4ba.utils.toJson
import io.sentry.event.Event
import org.reflections.Reflections
import spark.Request
import spark.Response
import spark.Spark.*
import spark.kotlin.port
import java.lang.reflect.InvocationTargetException
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

    var controllers = reflections.getTypesAnnotatedWith(Path::class.java)
    for(controller in controllers) {
        path(controller.getAnnotation(Path::class.java).value) {
            var methods = controller.declaredMethods.filter {
                    it.isAnnotationPresent(GET::class.java) ||
                    it.isAnnotationPresent(POST::class.java) ||
                    it.isAnnotationPresent(PUT::class.java) ||
                    it.isAnnotationPresent(DELETE::class.java)
            }
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
        .filter { !it.isAnnotationPresent(QueryParam::class.java) }
        .filter { !it.isAnnotationPresent(PathParam::class.java) }
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
        var params = method.parameters
            .map { parameter ->
                val pair = if(parameter.isAnnotationPresent(QueryParam::class.java)) {
                    req.queryParams(parameter.getAnnotation(QueryParam::class.java).value)
                } else if (parameter.isAnnotationPresent(PathParam::class.java)) {
                    req.params(parameter.getAnnotation(PathParam::class.java).value)
                } else NotFound()
                if(pair is NotFound)
                    null
                else
                    mapDataTypes(parameter.type!! to pair as String?)
            }
            .filterNotNull()
        args.addAll(params)
        var invokeResult = try {
            method.invoke(instance, *args.toTypedArray())
        } catch (e : InvocationTargetException) {
                if(e.cause is HttpStatus) {
                    if(e.cause is InternalServerError)
                        SentryTurret.log {}.capture(e)
                    e.cause
                } else {
                    SentryTurret.log {
                        addExtra("params", params.toJson())
                    }.capture(e)
                    InternalServerError("a server error occured")
                }
        }
        if(invokeResult is HttpStatus) {
            resp.status(invokeResult.code)
            return mapOf(
                "code" to invokeResult.code,
                "message" to invokeResult.message,
                "customMessage" to invokeResult.message
            ).toJson()
        }
        return jacksonObjectMapper().writeValueAsString(invokeResult)
    }
}

fun jacksonObjectMapper()  : ObjectMapper {
    var mapper = com.fasterxml.jackson.module.kotlin.jacksonObjectMapper()
    return mapper
}