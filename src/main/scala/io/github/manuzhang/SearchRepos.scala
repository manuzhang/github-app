package io.github.manuzhang

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, ExecutionContext, Future, blocking}
import scala.util.{Failure, Success, Try}

import Utils._

object SearchRepos extends RestApp {

  override def run(conf: Conf): Unit = {
    implicit val executionContext = ExecutionContext.global

    val futures = languages.map { lang =>
      Future {
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
              case Success(repo) => repo
              case Failure(e) =>
                throw e
            }
          }
        }
        Future.sequence(fs)
      }
    }

    val reposLists = Await.result(Future.sequence(futures), Duration.Inf)
    reposLists.indices.foreach { i =>
      val json = reposLists(i).collect {
        case repo: Repo if repo.valid =>
          repo.jsonObj
      }.render(indent = 2)
      val lang = languages(i)
      os.write.over(os.pwd / s"rest-$lang.json", json)
    }
  }
}
