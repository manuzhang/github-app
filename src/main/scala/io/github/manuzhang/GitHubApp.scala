package io.github.manuzhang

import org.rogach.scallop.ScallopConf
import ujson.Value.Value

trait GitHubApp extends App {

  val endpoint = "https://api.github.com"


  class Conf(arguments: Seq[String]) extends ScallopConf(arguments) {
    val clientSecret = opt[String](required = true)
    val clientId = opt[String](required = true)
  }

  override def main(args: Array[String]): Unit = {
    val conf = new Conf(args)
    conf.verify()
    run(conf)
  }

  def run(conf: Conf): Unit


  def get(url: String,
      conf: Conf,
      preview: Boolean = false,
      inputParams: Map[String, String] = Map.empty[String, String],
      inputHeaders: Map[String, String] = Map.empty[String, String]
      ): Value = {
    ujson.read(requests.get(s"$endpoint/$url",
      params = Map("client_id" -> conf.clientId(),
        "client_secret" -> conf.clientSecret()) ++ inputParams,
      headers = getHeaders(inputHeaders, preview)
    ).text)
  }

  def getHeaders(inputHeaders: Map[String, String], preview: Boolean): Map[String, String] = {
    if (preview) {
      inputHeaders + ("Accept" -> "application/vnd.github.mercy-preview+json")
    } else {
      inputHeaders
    }
  }
}
