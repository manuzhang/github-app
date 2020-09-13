package io.github.manuzhang.graphql

import scala.util.Try

object GetStarredRepos extends GraphQlApp {
  override def run(): Unit = {
    val query = os.read(os.pwd / "src" / "main" / "graphql" / "get-starred-repos.graphql")

    var hasNext = true
    var cursor = ""
    val outputPath = os.pwd / "starred_repos.csv"
    while(hasNext) {
      val values = runV4Post(query,
        ujson.Obj("cursor" -> cursor).toString()
      ).obj("user").obj("starredRepositories")
      values.obj("nodes").arr.foreach { repo =>
        val name = repo.obj("nameWithOwner").str
        val desc = Try(repo.obj("description").str.split("\n").head
          .replace(",", ";")).getOrElse("")
        val lang = Try(repo.obj("primaryLanguage").obj("name").str).getOrElse("")
        val lastUpdated = Try(repo.obj("defaultBranchRef").obj("target").obj("history").obj("nodes").arr
          .head.obj("pushedDate").str).getOrElse("")
        val stars = repo.obj("stargazers").obj("totalCount").num.toInt
        val forks = repo.obj("forks").obj("totalCount").num.toInt
        val topics = repo.obj("repositoryTopics").obj("nodes").arr
          .map(_.obj("topic").obj("name").str).mkString(";")
        os.write.append(outputPath, s"$name,$desc,$lang,$lastUpdated,$stars,$forks,$topics\n")
      }
      val page = values.obj("pageInfo")
      hasNext = page.obj("hasNextPage").bool
      cursor = page.obj("endCursor").str
    }
  }
}
