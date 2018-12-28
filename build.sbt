import Dependencies._

lazy val root = (project in file("."))
  .settings(
    inThisBuild(List(
      organization := "com.github.gvolpe",
      scalaVersion := "2.12.8",
      version      := "0.1.0-SNAPSHOT",
      scalacOptions := Seq(
        "-deprecation",
        "-encoding",
        "UTF-8",
        "-feature",
        "-language:existentials",
        "-language:higherKinds",
        "-Ypartial-unification"
      )
    )),
    name := "Advanced Http4s",
    libraryDependencies ++= Seq(
      Libraries.catsEffect,
      Libraries.monix,
      Libraries.fs2Core,
      Libraries.http4sServer,
      Libraries.http4sClient,
      Libraries.http4sDsl,
      Libraries.http4sCirce,
      Libraries.http4sXml,
      Libraries.circeCore,
      Libraries.circeGeneric,
      Libraries.typesafeConfig,
      Libraries.logback,
      Libraries.scalaTest,
      Libraries.scalaCheck
    ),
    addCompilerPlugin("org.spire-math" % "kind-projector" % "0.9.9" cross CrossVersion.binary),
    // Decomment if it's too boring!
    //scalacOptions := scalacOptions.value.filter(_ != "-Xfatal-warnings"),
)
