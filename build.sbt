name := "basicKinesisStream"

version := "0.1"

scalaVersion := "2.11.11"

lazy val awsVersion = "1.11.77"

lazy val dependencies = Seq(
  "com.amazonaws"          % "aws-java-sdk-core"                 % awsVersion,
  "com.amazonaws"          % "amazon-kinesis-client"             % "1.7.6",
  "io.circe"               %% "circe-parser"                     % "0.8.0",
  "io.circe"               %% "circe-generic"                    % "0.8.0",
  "com.beachape"           %% "enumeratum-circe"                 % "1.5.14"
)

lazy val root = (project in file("."))
  .settings(
    name := name.value,
    libraryDependencies ++= dependencies
  )
        