package controller

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.javalin.Context
import org.kohsuke.github.GHDeploymentBuilder
import org.kohsuke.github.GitHubBuilder

//Github event constants
private const val PULL_REQUEST = "pull_request"
private const val DEPLOYMENT = "DEPLOYMENT"
private const val DEPLOYMENT_STATUS = "DEPLOYMENT_STATUS"

class APIController
{
    //Setup to read the incoming Github JSON
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val genericJSON = Types.newParameterizedType(Map::class.java, String::class.java, Object::class.java)
    private var adapter: JsonAdapter<Map<String, Any>> = moshi.adapter(genericJSON)

    /**
     * Route incoming webhooks depending on header and parse json
     */
    fun eventHandler(context: Context)
    {
        //Read incoming data from github
        val payload = context.body()
        val githubEvent = context.header("X-GITHUB-EVENT")
        val jsonMap = adapter.fromJson(payload)

        //Decide what to do with it
        when (githubEvent)
        {
            PULL_REQUEST -> pullRequestOperation(jsonMap)
            DEPLOYMENT -> processDeployment(jsonMap)
            DEPLOYMENT_STATUS -> updateDeploymentStatus(jsonMap)
        }
    }

    private fun pullRequestOperation(jsonObject: Map<String, Any>?)
    {
        val pullRequestInfo = jsonObject!!["pull_request"] as Map<String, Any>

        //Check if PR or issue is opened
        if (jsonObject["action"].toString() == "closed" && pullRequestInfo["merged"] as Boolean)
        {
            println("A pull request was merged! A deployment should start now...")

            //if it is call startDeployment()
            startDeployment(pullRequestInfo)
        }


    }

    private fun startDeployment(jsonObject: Map<String, Any>?)
    {
        val user = (jsonObject!!["user"] as Map<*, *>)["login"] as String
        val headMap = jsonObject["head"] as Map<*, *>

        val payloadMap = mapOf("environment" to "QA", "deploy_user" to user)
        val payload = adapter.toJson(payloadMap)

        val gitHub = GitHubBuilder.fromEnvironment().build()

        val repository = gitHub.getRepository(((
                headMap["repo"] as Map<*, *>)
                ["full_name"] as String))

        var deployment = GHDeploymentBuilder(repository, (headMap["sha"] as String))
                .description("Auto Deploy after merge")
                .autoMerge(false)
                .create()
    }

    private fun processDeployment(jsonObject: Map<String, Any>?)
    {

    }

    private fun updateDeploymentStatus(jsonObject: Map<String, Any>?)
    {

    }
}