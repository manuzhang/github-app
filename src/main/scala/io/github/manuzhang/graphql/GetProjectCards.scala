package io.github.manuzhang.graphql

import scala.concurrent.Await
import scala.concurrent.duration.Duration

object GetProjectCards extends GraphQlApp {

  override def run(): Unit = {
    val query =
      """
        |query {
        |  repository(owner: "manuzhang", name: "LOG") {
        |    project(number: 1) {
        |      columns(first: 4) {
        |        nodes {
        |          name
        |          updatedAt
        |          cards {
        |            nodes {
        |              content {
        |                ... on Issue {
        |                  number
        |                  title
        |                }
        |              }
        |            }
        |          }
        |        }
        |      }
        |    }
        |  }
        |}
        |""".stripMargin

    val response = Await.result(runV4PostAsync(query), Duration.Inf)
    val columns = response.obj("repository").obj("project").obj("columns").obj("nodes").arr
    columns.foreach {
      col => if (col.obj("name").str == "Today") {
        println(col.obj("updatedAt"))
        col.obj("cards").obj("nodes").arr.map { card =>
          val issue = card.obj("content")
          val number = issue.obj("number")
          val title = issue.obj("title")
          println(s"#$number, $title")
        }
      }
    }
  }
}
