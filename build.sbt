import sbt.Keys._

name := "properties"

val uscalaVersion = "0.2.2"
val specs2Version = "3.7.2"

val pomInfo = (
  <url>https://github.com/intenthq/scala-properties</url>
  <licenses>
    <license>
      <name>The MIT License (MIT)</name>
      <url>https://github.com/intenthq/scala-properties/blob/master/LICENSE</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:intenthq/scala-properties.git</url>
    <connection>scm:git:git@github.com:intenthq/scala-properties.git</connection>
  </scm>
  <developers>
    <developer>
      <id>janstepickle</id>
      <name>Chris Jansen</name>
    </developer>
  </developers>
)

lazy val commonSettings = Seq(
  version := "0.1.0",
  scalaVersion := "2.11.8",
  organization := "intenthq.properties",
  pomExtra := pomInfo,
  autoAPIMappings := true,
  publishArtifact in Test := false,
  pomIncludeRepository := { _ => false },
  licenses += ("MIT", url("https://github.com/intenthq/scala-properties/blob/master/LICENSE")),
  resolvers ++= Seq(Resolver.sonatypeRepo("releases"), "Bintray jcenter" at "https://jcenter.bintray.com/"),
  libraryDependencies ++= Seq(
    "org.uscala" %% "uscala-result" % uscalaVersion,
    "org.uscala" %% "uscala-result-specs2" % uscalaVersion % "test",
    "org.specs2" %% "specs2-core" % specs2Version % "test",
    "org.specs2" %% "specs2-scalacheck" % specs2Version % "test",
    "org.specs2" %% "specs2-junit" % specs2Version % "test"
  ),
  scalacOptions in Test ++= Seq(
    "-Yrangepos",
    "-Xlint",
    "-deprecation",
    "-Xfatal-warnings"
  ),
  scalacOptions ++= Seq(
    "-Xlint",
    "-Xcheckinit",
    "-Xfatal-warnings",
    "-unchecked",
    "-deprecation",
    "-feature",
    "-language:implicitConversions")
)

lazy val core = (project in file("core")).
  settings(name := "scala-properties-core").
  settings(commonSettings: _*)

lazy val vault = (project in file("vault")).
  settings(name := "scala-properties-vault").
  settings(libraryDependencies += "janstenpickle.vault" %% "vault-core" % "0.3.0").
  settings(commonSettings: _*).
  dependsOn(core % "compile->compile; test->test")

lazy val apache = (project in file("apache")).
  settings(name := "scala-properties-apache").
  settings(libraryDependencies += "commons-configuration" % "commons-configuration" % "1.6").
  settings(commonSettings: _*).
  dependsOn(core % "compile->compile; test->test")

lazy val hadoop = (project in file("hadoop")).
  settings(name := "scala-properties-hadoop").
  settings(libraryDependencies += "org.apache.hadoop" % "hadoop-common" % "2.7.2").
  settings(commonSettings: _*).
  dependsOn(core % "compile->compile; test->test")

