package controller

import io.javalin.Context
import model.GithubRepository
import model.IssuePayload
import org.litote.kmongo.KMongo
import org.litote.kmongo.getCollection

class RepoController(issueController: IssueController)
{
    val mongo = KMongo.createClient()
    val faultyDB = mongo.getDatabase("Faulty")
    val reposDB = faultyDB.getCollection<GithubRepository>()

    fun new(context: Context)
    {

    }

    fun delete(context: Context)
    {

    }

    fun submitNewIssue(issuePayload: IssuePayload)
    {

    }
}