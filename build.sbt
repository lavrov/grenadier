organization in ThisBuild := "com.github.devnfun.grenadier"

version in ThisBuild := "0.1.1-SNAPSHOT"

scalaVersion in ThisBuild := "2.11.8"

libraryDependencies in ThisBuild ++= Seq(
  "org.specs2" %% "specs2-core" % "2.4.13" % "test"
)

resolvers in ThisBuild += "Bintary JCenter" at "http://jcenter.bintray.com"

lazy val grenadier = project in file(".") aggregate (client, server, sharedJs, sharedJvm)

lazy val client = project in file("./modules/client") dependsOn sharedJs

lazy val server = project in file("./modules/server") dependsOn (sharedJvm) settings(
  scalaJSProjects += client
)

lazy val shared = crossProject crossType CrossType.Pure in file("./modules/shared") settings(
  libraryDependencies += "io.circe" %%% "circe-generic" % "0.5.3",
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full)
)

lazy val sharedJs = shared.js

lazy val sharedJvm = shared.jvm
