enablePlugins(ScalaJSPlugin, ScalaJSWeb)

libraryDependencies ++= Seq(
  "org.scala-js" %%% "scalajs-dom" % "0.9.0",
  "io.circe" %%% "circe-parser" % "0.5.3"
)

persistLauncher := true
