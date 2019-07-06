package io.github.manuzhang

import ujson.Obj

object Utils {

  val languages = List(
    "Scala", "Java", "Python", "JavaScript", "Go",
    "C++", "HTML", "Shell", "Jupyter Notebook", "C")

  case class Repo(fullName: String, readme: String, topics: List[String]) {

    def jsonObj: Obj = {
      ujson.Obj("name" -> fullName, "readme" -> readme, "topics" -> topics)
    }

    def valid: Boolean = {
      readme.nonEmpty && topics.nonEmpty
    }
  }
}
