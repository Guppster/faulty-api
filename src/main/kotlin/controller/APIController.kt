package controller

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import io.javalin.Context
import model.IssuePayload
import org.kohsuke.github.GHDeploymentBuilder
import org.kohsuke.github.GHDeploymentState.PENDING
import org.kohsuke.github.GHDeploymentState.SUCCESS
import org.kohsuke.github.GHDeploymentStatusBuilder
import org.kohsuke.github.GitHubBuilder
import java.util.*

//Github event constants
private const val PULL_REQUEST = "pull_request"
private const val ISSUE_OPENED = "issues"
private const val DEPLOYMENT = "DEPLOYMENT"
private const val DEPLOYMENT_STATUS = "DEPLOYMENT_STATUS"

typealias jsonMap = Map<*, *>

class APIController(val repoController: RepoController)
{
    //Setup to read the incoming Github JSON
    private val moshi = Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()

    private var issueAdapter = moshi.adapter(IssuePayload::class.java).lenient()

    /**
     * Route incoming webhooks depending on header and parse json
     */
    fun eventHandler(context: Context)
    {
        //Read incoming data from github
        val payload = context.formParam("payload")
        val githubEvent = context.header("X-GITHUB-EVENT")

        //Decide what to do with it
        when (githubEvent)
        {
            ISSUE_OPENED -> issueHandler(payload!!)
            //PULL_REQUEST -> pullRequestOperation(jsonMap!!)
            //DEPLOYMENT -> processDeployment(jsonMap!!)
            //DEPLOYMENT_STATUS -> updateDeploymentStatus(jsonMap!!)
        }
    }

    private fun issueHandler(payload: String)
    {
        val issue = issueAdapter.fromJson(payload)
        repoController.submitNewIssue(issue!!)
    }

    private fun pullRequestOperation(jsonObject: jsonMap)
    {
        val pullRequestInfo = jsonObject["pull_request"] as jsonMap

        //Check if PR or issue is opened
        if (jsonObject["action"].toString() == "closed" && pullRequestInfo["merged"] as Boolean)
        {
            println("A pull request was merged! A deployment should start now...")

            //if it is call startDeployment()
            startDeployment(pullRequestInfo)
        }
    }

    private fun startDeployment(jsonObject: jsonMap)
    {
        val user = (jsonObject["user"] as jsonMap)["login"] as String
        val headMap = jsonObject["head"] as jsonMap

        val payloadMap = mapOf("environment" to "QA", "deploy_user" to user)
        //val payload = adapter.toJson(payloadMap)

        val gitHub = GitHubBuilder.fromEnvironment().build()

        val repository = gitHub.getRepository(((headMap["repo"] as jsonMap)["full_name"] as String))

        var deployment = GHDeploymentBuilder(repository,
                (headMap["sha"] as String)).description("Auto Deploy after merge").autoMerge(false).create()
    }

    private fun processDeployment(jsonObject: jsonMap)
    {
        val deploymentMap = jsonObject["deployment"] as jsonMap
        val payloadString = deploymentMap["payload"] as String

        //val payload = adapter.fromJson(payloadString)
        val payload = mapOf<String, String>("hi" to "hi")

        println("Processing ${deploymentMap["description"] as String} for ${payload!!["deploy_user"] as String} to ${payload["environment"] as String}")

        Thread.sleep(2000L)

        val gitHub = GitHubBuilder.fromEnvironment().build()

        val repository = gitHub.getRepository(((jsonObject["repository"] as jsonMap)["full_name"] as String))

        var deploymentStatusInitial = GHDeploymentStatusBuilder(repository,
                deploymentMap["id"] as Int,
                PENDING).create()

        Thread.sleep(5000L)

        var deploymentStatusFinal = GHDeploymentStatusBuilder(repository, deploymentMap["id"] as Int, SUCCESS).create()
    }

    private fun updateDeploymentStatus(jsonObject: jsonMap)
    {
        val deploymentMap = jsonObject["deployment"] as jsonMap
        val deploymentStatus = jsonObject["deployment"] as jsonMap

        println("Deployment status for ${deploymentMap["id"] as String} is ${deploymentStatus["state"] as String}")
    }
}