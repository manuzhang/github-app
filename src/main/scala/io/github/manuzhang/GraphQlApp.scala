package io.github.manuzhang

import org.rogach.scallop.ScallopConf

import scala.concurrent.{Await, ExecutionContext, Future, blocking}
import Utils._

import scala.concurrent.duration.Duration
import scala.util.Try

object GraphQlApp extends App {

  val endpoint = "https://api.github.com/graphql"

  val query =
    """
      query($queryStr: String!) {
        search(query: $queryStr, type: REPOSITORY, first: 10) {
          edges {
            node {
              ... on Repository {
                nameWithOwner
                description
                stargazers {
                  totalCount
                }
                forks {
                  totalCount
                }
                repositoryTopics(first: 5) {
                  edges {
                    node {
                      ... on RepositoryTopic {
                        topic {
                          name
                        }
                      }
                    }
                  }
                }
                object(expression: "master:README.md") {
                  ... on Blob {
                    text
                  }
                }
              }
            }
          }
        }
      }
    """.stripMargin

  def getVariables(lang: String): String = {
    s"""
      {
        "queryStr": "language:$lang sort:stars-desc"
      }
    """.stripMargin
  }

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val authToken = opt[String](required = true)
  }

  val conf = new Conf(args)
  conf.verify()

  val authToken = conf.authToken()

  implicit val executionContext = ExecutionContext.global
  val fs = Utils.languages.map { lang =>
    Future {
      blocking {
        val response = requests.post(s"$endpoint",
          headers = Map("Authorization" -> s"bearer $authToken"),
          data = ujson.Obj("query" -> query, "variables" -> getVariables(lang)).toString())

        lang -> ujson.read(response.text()).obj("data").obj("search").obj("edges").arr.map { edge =>
          val node = edge.obj("node")
          Repo(fullName = node.obj("nameWithOwner").str,
            description = node.obj("description").str,
            readme = Try(node.obj("object").obj("text").str).getOrElse(""),
            topics = Try(node.obj("repositoryTopics").obj("edges").arr.map { edge =>
              edge.obj("node").obj("topic").obj("name").str
            }.toList).getOrElse(List.empty[String])
          )
        }.toList
      }
    }
  }

  Await.result(Future.sequence(fs), Duration.Inf).foreach { case (lang, repos) =>
    val json = repos.collect {
      case repo: Repo if repo.valid =>
        repo.json
    }.render(indent = 2)

    os.write.over(os.pwd / s"graphql-$lang.json", json)
  }
}
