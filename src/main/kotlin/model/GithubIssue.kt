package model

import com.squareup.moshi.Json
import org.bson.codecs.pojo.annotations.BsonId
import java.util.Date

enum class State {
  @Json(name = "open") OPEN,
  @Json(name = "closed") CLOSED
}

data class Label(
    val url: String,
    val id: Long,
    val name: String,
    val color: String = "46DF1B"
)

data class Milestone(
    val url: String,
    val id: Long,
    val title: String,
    val creator: User,
    @Json(name = "open_issues") val openCount: Long = 0L,
    @Json(name = "closed_issues") val closedCount: Long = 0L,
    @Json(name = "created_at") val createdAt: Date,
    @Json(name = "due_on") val dueOn: Date?
)

data class User(val url: String, val login: String)

data class Issue(
        val url: String,
        @BsonId val id: Long,
        val number: Long,
        val title: String,
        val labels: List<Label> = listOf(),
        val milestone: Milestone?,
        val assignees: List<User> = listOf(),
        val state: State,
        val comments: Long = 0L,
        @Json(name = "created_at") val createdAt: Date,
        @Json(name = "closed_at") val closedAt: Date?,
        val body: String = ""
)

data class IssuePayload(
        val issue: Issue,
        val action: String,
        val repository: GithubRepositoryRawPayload,
        val sender: GithubSender
)


