package io.github.manuzhang.graphql

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object GetAwesomeStreamingRepos extends GraphQlApp {

  override def run():Unit = {

    val query =
      """
     query {
       repository(owner: "manuzhang", name: "awesome-streaming") {
         object(expression: "master:README.md") {
           ... on Blob {
             text
           }
         }
       }
     }
  """.stripMargin

    val response = Await.result(runQuery(query), Duration.Inf)
    val readme = response.obj("repository").obj("object").obj("text").str

    val fs = readme.split("\n").map(_.split(" - ")).collect {
      case parts if parts.length == 2 =>
        val link = parts(0)
        val desc = parts(1)

        val queryRepo =
          """
           query($owner: String!, $name: String!) {
             repository(owner: $owner, name: $name) {
               forks {
                 totalCount
               }
               stargazers {
                 totalCount
               }
               pushedAt
               isArchived
             }
           }
        """.stripMargin

        def getVariables(owner: String, name: String): String = {
          ujson.Obj("owner" -> owner, "name" -> name).toString()
        }

        val regex = "\\[([^\\[\\]]+)\\]\\(https://github.com/([^/]+)/([^/)]+)/?\\)".r
        regex.findFirstMatchIn(link).collect { case regex(displayName, owner, name) =>
          runQuery(queryRepo, getVariables(owner, name)).map { response =>
            val repo = response.obj("repository")
            ujson.Obj(
              "name" -> displayName,
              "link" -> s"https://github.com/$owner/$name",
              "description" -> desc,
              "stars" -> repo.obj("stargazers").obj("totalCount"),
              "forks" -> repo.obj("forks").obj("totalCount"),
              "lastUpdate" -> repo.obj("pushedAt"),
              "isArchived" -> repo.obj("isArchived")
            )
          }
        }
    }.filter(_.nonEmpty).map(_.get).toList

    val json = Await.result(Future.sequence(fs), Duration.Inf).render(indent = 2)
    os.write.over(os.pwd / "awesome-streaming-repos.json", json)
  }
}
