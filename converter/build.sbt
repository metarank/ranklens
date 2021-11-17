version := "0.1.0-SNAPSHOT"
scalaVersion := "2.13.7"
name := "converter"

lazy val circeVersion  = "0.14.1"
lazy val http4sVersion = "1.0.0-M29"

libraryDependencies ++= Seq(
  "io.circe"             %% "circe-core"           % circeVersion,
  "io.circe"             %% "circe-generic"        % circeVersion,
  "io.circe"             %% "circe-generic-extras" % circeVersion,
  "io.circe"             %% "circe-parser"         % circeVersion,
  "com.github.pathikrit" %% "better-files"         % "3.9.1",
  "org.http4s"           %% "http4s-dsl"           % http4sVersion,
  "org.http4s"           %% "http4s-blaze-server"  % http4sVersion,
  "org.http4s"           %% "http4s-blaze-client"  % http4sVersion,
  "org.http4s"           %% "http4s-circe"         % http4sVersion,
  "commons-io"            % "commons-io"           % "2.10.0"
)
