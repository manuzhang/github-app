name := "github-stars"
version := "0.1.0"
scalaVersion := "2.13.6"

libraryDependencies ++= Seq(
  "com.lihaoyi" %% "upickle" % "1.4.0",
  "com.lihaoyi" %% "requests" % "0.6.9",
  "com.lihaoyi" %% "os-lib" % "0.7.8",
  "com.lihaoyi" %% "scalatags" % "0.7.0",
  "org.rogach" %% "scallop" % "3.3.1",
  "org.sangria-graphql" %% "sangria" % "2.0.0"
)

enablePlugins(GraphQLCodegenPlugin)
graphqlCodegenSchema := (resourceDirectory in Compile).value / "github-schema.graphql"
graphqlCodegenImports := Seq("io.github.manuzhang.graphql.GraphQlApp.DateTime")
