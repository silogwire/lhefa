package de.ddkfm.stpapp.models

open class HttpStatus(
        var code : Int,
        var message : String
)
data class NotFound(var customMessage : String = "Not Found") : HttpStatus(code = 404, message = customMessage)
data class Unauthorized(var customMessage : String = "Unauthorized") : HttpStatus(code = 401, message = customMessage)
data class BadRequest(var customMessage : String = "Bad Request") : HttpStatus(code = 400, message = customMessage)
data class AlreadyExists(var customMessage : String = "Already Exists") : HttpStatus(code = 409, message = customMessage)
data class Created(var customMessage: String = "Created") : HttpStatus(code = 201, message = customMessage)

data class PasswordParam(
        var password : String
)