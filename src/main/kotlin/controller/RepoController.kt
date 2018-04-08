package controller

import com.squareup.moshi.KotlinJsonAdapterFactory
import com.squareup.moshi.Moshi
import com.squareup.moshi.Rfc3339DateJsonAdapter
import io.javalin.Context
import model.GithubRepository
import model.IssuePayload
import mu.KLogging
import mu.KotlinLogging
import org.litote.kmongo.*
import java.util.*

class RepoController(val analysisController: AnalysisController)
{
    companion object: KLogging()

    val mongo = KMongo.createClient()
    val faultyDB = mongo.getDatabase("Faulty")
    val reposDB = faultyDB.getCollection<GithubRepository>()

    //Setup to read the incoming requests from Web UI
    private val moshi = Moshi
            .Builder()
            .add(KotlinJsonAdapterFactory())
            .add(Date::class.java, Rfc3339DateJsonAdapter().nullSafe())
            .build()

    private var repoAdapter = moshi.adapter(GithubRepository::class.java)

    fun new(context: Context)
    {
        val repo = repoAdapter.fromJson(context.body())

        //TODO: do some validation obviously
        reposDB.insertOne(repo)
    }

    fun delete(context: Context)
    {
        //TODO(not implemented)
    }

    fun submitNewIssue(issuePayload: IssuePayload)
    {
        //look for the repository name and add a Issue to its issues field
        val fetchedRepo = reposDB.findOne("{name: ${issuePayload.repository.name.json}}")

        fetchedRepo!!.issues.add(issuePayload.issue)
        reposDB.replaceOne(fetchedRepo)

        //Trigger a new analysis of the process
        analysisController.processIssue(fetchedRepo, issuePayload.issue)
    }
}