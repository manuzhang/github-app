package io.github.manuzhang

import upickle.default._
import ujson.Value

object Utils {

  val languages = List(
    "Scala", "Java", "Python", "JavaScript", "Go",
    "C++", "HTML", "Shell", "Jupyter Notebook", "C")

  case class Repo(fullName: String, description: String = "",
      readme: String = "", topics: List[String] = List.empty[String]) {

    def json: Value = {
      implicit val writer: Writer[Repo] = macroW[Repo]
      upickle.default.writeJs(this)
    }

    def valid: Boolean = {
      readme.nonEmpty && topics.nonEmpty
    }
  }

  def getFirstNode(obj: ujson.Value): Option[ujson.Value] = {
    obj("nodes").arr.headOption
  }
}
