package io.github.manuzhang.graphql

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}

object GetAwesomeStreamingRepos extends GraphQlApp{

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

    val regex = "https://github.com/([^/]+)/([^/)]+)".r
    val repos = regex.findAllMatchIn(readme).collect {
      case regex(owner, name) if name != "awesome" =>
        owner -> name
    }.toList

    val queryRepo =
      """
     query($owner: String!, $name: String!) {
       repository(owner: $owner, name: $name) {
         description
         forks {
           totalCount
         }
         stargazers {
           totalCount
         }
         updatedAt
       }
     }
  """.stripMargin

    def getVariables(owner: String, name: String): String = {
      ujson.Obj("owner" -> owner, "name" -> name).toString()
    }

    // val repoStr = repos.reduce(_ + "\n" + _)
    // os.write.over(os.pwd / "awesome-streaming.txt", repoStr)

    println(s"Found ${repos.length} repos")

    val fs = repos.map { case (owner, name) =>
          println(s"Fetching data from repo $owner:$name")
      runQuery(queryRepo, getVariables(owner, name)).map { response =>
        val repo = response.obj("repository")
        repo.update("name", s"$owner/$name")
        repo
      }
    }

    val json = Await.result(Future.sequence(fs), Duration.Inf).render(indent = 2)
    os.write.over(os.pwd / "awesome-streaming-repos.json", json)
  }

}
