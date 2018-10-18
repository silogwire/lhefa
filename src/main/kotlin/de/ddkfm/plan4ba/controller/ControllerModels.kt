package de.ddkfm.plan4ba.controller

import spark.Request
import spark.Response

open class ControllerInterface(
        var req : Request,
        var resp : Response
)


