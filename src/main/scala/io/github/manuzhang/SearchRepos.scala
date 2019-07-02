package io.github.manuzhang

import ujson.Obj

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, blocking}
import scala.util.{Failure, Success, Try}

object SearchRepos extends GitHubApp {

  override def run(conf: Conf): Unit = {

    val languages = List(
      "Scala", "Java", "Python", "JavaScript", "Go",
      "C++", "HTML", "Shell", "Jupyter Notebook", "C")

    implicit val executionContext = ExecutionContext.global

    languages.foreach { lang =>
      val f = Future {
        blocking {
          get("search/repositories",
            conf,
            preview = true,
            inputParams = Map("q" -> lang))
        }
      }.flatMap { resp =>
        val fs = resp.obj("items").arr.map { item =>
          val fullName = item.obj("full_name").str
          Future {
            blocking {
              get(s"repos/$fullName/readme", conf)
            }
          }.zip {
            Future {
              blocking {
                get(s"repos/$fullName/topics", conf, preview = true)
              }
            }
          }.map { case (readme, topics) =>
            Try {
              Repo(fullName, Try(readme.obj("content").str).getOrElse(""),
                Try(topics.obj("names").arr.map(_.str).toList)
                  .getOrElse(List.empty[String]))
            } match {
              case Success(kv) => kv
              case Failure(e) =>
                throw e
            }
          }
        }
        Future.sequence(fs)
      }

      val json = Await.result(f, Duration.Inf).collect {
        case repo: Repo if repo.valid =>
          repo.jsonObj
      }.render(indent = 2)

      os.write.over(os.pwd / s"result-$lang.json", json)
    }
  }

  case class Repo(fullName: String, readme: String, topics: List[String]) {

    def jsonObj: Obj = {
      ujson.Obj("name" -> fullName, "readme" -> readme, "topics" -> topics)
    }

    def valid: Boolean = {
      readme.nonEmpty && topics.nonEmpty
    }

  }
}
