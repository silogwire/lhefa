package de.ddkfm.stpapp.controller

import spark.Request
import spark.Response

open class ControllerInterface(
        var req : Request,
        var resp : Response
) {

}


