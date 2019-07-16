package io.github.manuzhang.graphql

import org.rogach.scallop.ScallopConf
import ujson.Value

import scala.concurrent.{blocking, ExecutionContext, Future}

trait GraphQlApp extends App {

  val endpoint = "https://api.github.com/graphql"

  private var authToken: String = _

  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val authToken = opt[String](required = true)
  }

  override def main(args: Array[String]): Unit = {
    val conf = new Conf(args)
    conf.verify()
    authToken = conf.authToken()
    run()
  }

  implicit val executionContext: ExecutionContext = ExecutionContext.global

  def run(): Unit

  def runQuery(query: String, variables: String = ""): Future[Value] = {
    Future {
      blocking {
        val response = requests.post(s"$endpoint",
          headers = Map("Authorization" -> s"bearer $authToken"),
          data = ujson.Obj("query" -> query, "variables" -> variables).toString())

        ujson.read(response.text()).obj("data")
      }
    }
  }
}
