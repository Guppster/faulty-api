package controller

import model.GithubRepository
import model.Issue
import mu.KLogging

class AnalysisController
{
    companion object: KLogging()

    //TODO: Return valuable output
    fun processIssue(repo: GithubRepository, issue: Issue): String
    {
        //If the repo is tiny we cant analyze it yet. Wait for more data
        if(repo.issues.size < 10)
        {
            return "Need to gather more data to provide analysis"
        }

        //Update the state of repo object to include all available information for analysis
        updateRepo(repo)

        // Run algorithm to find highest to lowest correlation between issues token
        // and repo expanded tokens

        // Return back something that can be seen from the Github UI issue screen
        return "Success"
    }

    //TODO(Not Implemented)
    fun updateRepo(repo: GithubRepository): String
    {
        // Verify if latest repo is at same commit last RSF was generated in DB
        // If not, regenerate RSF for latest commit

        // Verify if all issues are up to date in DB
        // If not, extract tokens from new issues

        // Create token to file relationships

        // Expand token set

        return "TODO"
    }
}