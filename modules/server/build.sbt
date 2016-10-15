enablePlugins(PlayScala)

disablePlugins(PlayLayoutPlugin)

libraryDependencies ++= Seq(
  "play-circe" %% "play-circe" % "2.5-0.5.1",
  "com.vmunier" %% "scalajs-scripts" % "1.0.0"
)

pipelineStages in Assets += scalaJSPipeline
