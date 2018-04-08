package model

data class GithubRepository(
        val _id: org.bson.types.ObjectId?,
        val name: String,
        val url: String,
        val owner: String
)
{
    val issues = mutableListOf<Issue>()
}