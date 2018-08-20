package de.ddkfm.stpapp

import com.google.gson.Gson
import de.ddkfm.stpapp.controller.SwaggerParser
import de.ddkfm.stpapp.models.BadRequest
import de.ddkfm.stpapp.models.Config
import de.ddkfm.stpapp.models.HttpStatus
import de.ddkfm.stpapp.utils.HibernateUtils
import de.ddkfm.stpapp.utils.mapDataTypes
import io.swagger.annotations.*
import org.reflections.Reflections
import spark.Request
import spark.Response
import spark.Spark.*
import spark.debug.DebugScreen
import spark.kotlin.port
import spark.utils.IOUtils
import java.io.File
import java.lang.reflect.Method
import javax.ws.rs.*


fun main(args : Array<String>) {
    port(8080)

    var config = Gson().fromJson<Config>(File("./config.json").readText(), Config::class.java)
    HibernateUtils.setUp(config.database)
    var reflections = Reflections("de.ddkfm.stpapp.controller")

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

    var swaggerJson = SwaggerParser.getSwaggerJson("de.ddkfm.stpapp.controller");
    get("/swagger", { req, res ->
        swaggerJson
    });
    get("/swagger/html") {req, resp ->
        IOUtils.copy(SwaggerParser.javaClass.getResourceAsStream("/index.html"), resp.raw().outputStream)
    }

    DebugScreen.enableDebugScreen()
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
            var bodyObject = Gson().fromJson(req.body(), bodyParam.type)
            args.add(bodyObject)
        } catch (e : Exception) {
            badRequest = true
        }
    }
    if(badRequest) {
        resp.status(400)
        return Gson().toJson(BadRequest())
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
                            }
                    param to value
                }
                .filter { it.second != null }
                .map(::mapDataTypes)

        args.addAll(implicitParams)

        var invokeResult = method.invoke(instance, *args.toTypedArray())
        if (invokeResult is HttpStatus)
            resp.status(invokeResult.code)
        return Gson().toJson(invokeResult)
    }
}