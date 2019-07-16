package io.github.manuzhang.graphql

import io.github.manuzhang.Utils
import io.github.manuzhang.Utils.Repo

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.Try

object SearchRepos extends GraphQlApp {

  def run(): Unit = {

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

    val fs = Utils.languages.map { lang =>
      runQuery(query, getVariables(lang)).map { response =>
        lang -> response.obj("search").obj("edges").arr.map { edge =>
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

    Await.result(Future.sequence(fs), Duration.Inf).foreach { case (lang, repos) =>
      val json = repos.collect {
        case repo: Repo if repo.valid =>
          repo.json
      }.render(indent = 2)

      os.write.over(os.pwd / s"graphql-$lang.json", json)
    }
  }

  private def getVariables(lang: String): String = {
    s"""
      {
        "queryStr": "language:$lang sort:stars-desc"
      }
    """.stripMargin
  }
}
