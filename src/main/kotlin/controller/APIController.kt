package controller

import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import io.javalin.Context

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

    fun pullRequestOperation(jsonObject: Map<String, Any>?)
    {
        //Check if PR or issue is opened
        //if it is call startDeployment()
    }

    fun startDeployment(jsonObject: Map<String, Any>?)
    {

    }

    fun processDeployment(jsonObject: Map<String, Any>?)
    {

    }

    fun updateDeploymentStatus(jsonObject: Map<String, Any>?)
    {

    }
}