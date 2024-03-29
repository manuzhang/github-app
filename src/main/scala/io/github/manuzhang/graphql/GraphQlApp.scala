package io.github.manuzhang.graphql

import org.rogach.scallop.ScallopConf
import requests.Response
import ujson.Value

import scala.concurrent.{blocking, ExecutionContext, Future}

object GraphQlApp {
  type DateTime = String
}

trait GraphQlApp extends App {

  val endpointV3 = "https://api.github.com"
  val endpointV4 = "https://api.github.com/graphql"

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

  def runAsync(request: => Value): Future[Value] = {
    Future {
      blocking {
        request
      }
    }
  }

  def runV4Post(query: String, variables: String = ""): Value = {
    val resp = parseResponse(request(query, variables))
    resp.obj("data")
  }

  def runV4PostAsync(query: String, variables: String = ""): Future[Value] = {
    runAsync {
      runV4Post(query, variables)
    }
  }

  def request(query: String, variables: String = ""): Response = {
    requests.post(s"$endpointV4",
      headers = Map("Authorization" -> s"bearer $authToken"),
      data = ujson.Obj("query" -> query, "variables" -> variables).toString())
  }

  def parseResponse(response: Response): Value = {
    ujson.read(response.text)
  }
}
