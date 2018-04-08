package controller

import io.javalin.ApiBuilder.*
import io.javalin.Javalin
import mu.KotlinLogging

//Constants
const val VERSION = "0.0.1"
const val PORT = 44537

private val logger = KotlinLogging.logger {}

fun main(args: Array<String>)
{
    val app = Javalin.create().apply{
        port(PORT)
    }.start()

    logger.info { "Starting on port $PORT" }

    val analysisController = AnalysisController()
    val repoController = RepoController(analysisController)
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
