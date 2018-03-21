package controller

import io.javalin.ApiBuilder.*
import io.javalin.Javalin

//Constants
const val VERSION = "0.0.1"
const val PORT = 4567

fun main(args: Array<String>)
{
    val app = Javalin.create().apply{
        port(PORT)
    }.start()

    val apiController = APIController()

    //The root path
    app.routes{
        path("/event-handler")
        {
           post(apiController::eventHandler)
        }
    }
}
