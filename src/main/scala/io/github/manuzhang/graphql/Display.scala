package io.github.manuzhang.graphql

import scalatags.Text.all._
import upickle.default.{macroR, read}

object Display {

  def main(args: Array[String]) = {
    implicit val starReader = macroR[Stars]
    implicit val forkReader = macroR[Forks]
    implicit val repoReader = macroR[Repo]
    val repos = read[Seq[Repo]](os.read(os.pwd / "awesome-streaming-repos.json"))

    val blob = html(
      fontFamily := "Calibri, Candara, Segoe, 'Segoe UI', Optima, Arial, sans-serif",
      head(
      ),
      body(
        table(
          tr(
            th("name"),
            th("description"),
            th("updatedAt"),
            th("forks"),
            th("stars")
          ),
          repos.map { repo =>
            tr(
              td(repo.name),
              // https://github.com/lihaoyi/scalatags/issues/16
              td(Option(repo.description).getOrElse[String]("")),
              td(repo.updatedAt),
              td(repo.forks.totalCount),
              td(repo.forks.totalCount)
            )
          }
        )
      )
    )

    os.write.over(os.pwd / "awesome-streaming-repos.html", blob.render)
  }

  case class Repo(name: String, description: String, updatedAt: String,
      forks: Forks, stargazers: Stars)

  case class Forks(totalCount: Int)

  case class Stars(totalCount: Int)

}
