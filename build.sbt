name := "github-stars"
version := "0.1.0"
scalaVersion := "2.12.11"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "upickle" % "0.7.5",
  "com.lihaoyi" %% "requests" % "0.1.8",
  "com.lihaoyi" %% "os-lib" % "0.2.7",
  "com.lihaoyi" %% "scalatags" % "0.11.1",
  "org.rogach" %% "scallop" % "3.3.1",
  "org.sangria-graphql" %% "sangria" % "2.0.0"
)

enablePlugins(GraphQLCodegenPlugin)
graphqlCodegenSchema := (resourceDirectory in Compile).value / "github-schema.graphql"
graphqlCodegenImports := Seq("io.github.manuzhang.graphql.GraphQlApp.DateTime")
