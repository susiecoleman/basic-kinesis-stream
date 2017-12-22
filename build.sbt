name := "basicKinesisStream"

version := "0.1"

scalaVersion := "2.11.11"

lazy val awsVersion = "1.11.77"

lazy val dependencies = Seq(
  "com.amazonaws"          % "aws-java-sdk-core"                 % "1.11.77",
  "com.amazonaws"          % "amazon-kinesis-client"             % "1.7.6",
  "org.scalatest"          %% "scalatest"                         % "3.0.4" % "test",
  "org.mockito"            % "mockito-core"                       % "2.13.0" % "test"
)

lazy val root = (project in file("."))
  .settings(
    name := name.value,
    libraryDependencies ++= dependencies
  )
        