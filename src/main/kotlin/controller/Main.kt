package controller

import io.javalin.ApiBuilder.*
import io.javalin.Javalin

//Constants
const val VERSION = "0.0.1"
const val PORT = 44537

fun main(args: Array<String>)
{
    val app = Javalin.create().apply{
        port(PORT)
    }.start()

    val repoController = RepoController()
    val apiController = APIController(repoController)

    //The root path
    app.routes{
        path("/event-handler")
        {
           post(apiController::eventHandler)
        }

        path("/")
        {
            get(""){ctx -> ctx.result ("Welcome to Faulty")}

            post("add-repo", repoController::new)
            post("delete-repo", repoController::delete)
        }
    }
}
