organization in ThisBuild := "com.github.devnfun.grenadier"

version in ThisBuild := "0.1.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

libraryDependencies in ThisBuild ++= Seq(
  "org.specs2" %% "specs2-core" % "2.4.13" % "test"
)

resolvers in ThisBuild += "Bintary JCenter" at "http://jcenter.bintray.com"

lazy val server = (
  project in file(".")
    enablePlugins PlayScala
    disablePlugins PlayLayoutPlugin
    settings (
      scalaJSProjects += client,
      libraryDependencies ++= Seq(
        "play-circe" %% "play-circe" % "2.5-0.5.1",
        "com.vmunier" %% "scalajs-scripts" % "1.0.0"
      ),
      pipelineStages in Assets += scalaJSPipeline
    )
    dependsOn sharedJvm
    aggregate (client, sharedJvm, sharedJs)
)

lazy val client = (
  project in file("./modules/client")
    enablePlugins (ScalaJSPlugin, ScalaJSWeb)
    settings (
      libraryDependencies ++= Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.9.0",
        "io.circe" %%% "circe-parser" % "0.5.3"
      ),
      persistLauncher := true
    )
    dependsOn sharedJs
)

lazy val shared = crossProject crossType CrossType.Pure in file("./modules/shared") settings(
  libraryDependencies += "io.circe" %%% "circe-generic" % "0.5.3",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

lazy val sharedJs = shared.js

lazy val sharedJvm = shared.jvm
